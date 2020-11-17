package no.unit.nva.doi;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;

import no.unit.nva.doi.exception.DataCiteException;
import no.unit.nva.doi.exception.InstitutionIdUnknownException;
import no.unit.nva.doi.exception.MissingParametersException;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;

import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import static nva.commons.utils.JsonUtils.objectMapper;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;


public class DataCiteMdsCreateDoiHandler extends ApiGatewayHandler<CreateDoiRequest, CreateDoiResponse> {

    public static final String ERROR_MISSING_REQUEST_JSON_BODY =
            "Request JSON body not present";
    public static final String ERROR_MISSING_JSON_ATTRIBUTE_VALUE_DATACITE_XML =
            "JSON attribute 'dataciteXml' is mandatory";
    public static final String ERROR_MISSING_JSON_ATTRIBUTE_VALUE_URL =
            "JSON attribute 'url' is mandatory";
    public static final String ERROR_MISSING_JSON_ATTRIBUTE_VALUE_INSTITUTION_ID =
            "JSON attribute 'institutionId' is mandatory";
    public static final String ERROR_RETRIEVING_DATACITE_MDS_CLIENT_CONFIGS =
            "Error retrieving DataCite MDS client configs";
    public static final String ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER =
            "Provided Institution ID is not set up as a DataCite provider";
    public static final String ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA =
            "Error setting DOI url, error deleting metadata";
    public static final String ERROR_SETTING_DOI_METADATA = "Error setting DOI metadata";
    public static final String ERROR_SETTING_DOI_URL = "Error setting DOI url";
    public static final String ERROR_DELETING_DOI_METADATA = "Error deleting DOI metadata";

    public static final String CHARACTER_PARENTHESES_START = "(";
    public static final String CHARACTER_PARENTHESES_STOP = ")";
    public static final String CHARACTER_WHITESPACE = " ";

    private final Map<String, DataCiteMdsClientConfig> dataCiteMdsClientConfigsMap;
    private final SecretCache secretCache;
    private final DataCiteMdsConnection dataCiteMdsConnection;

    private  static final String ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS = "DATACITE_MDS_CONFIGS";

    /**
     * Default constructor for DataCiteMdsCreateDoiHandler.
     */
    @JacocoGenerated
    public DataCiteMdsCreateDoiHandler() {
        this(new Environment(), new DataCiteMdsConnection(), new SecretCache());
    }

    /**
     * Constructor for DataCiteMdsCreateDoiHandler.
     *
     * @param environment        environment
     * @param dataCiteMdsConnection dataCiteMdsConnection
     * @param secretCache secretCache
     */
    public DataCiteMdsCreateDoiHandler(Environment environment,
                                       DataCiteMdsConnection dataCiteMdsConnection,
                                       SecretCache secretCache) {
        super(CreateDoiRequest.class, environment, LoggerFactory.getLogger(DataCiteMdsCreateDoiHandler.class));

        this.dataCiteMdsClientConfigsMap = new ConcurrentHashMap<>();
        this.dataCiteMdsConnection = dataCiteMdsConnection;
        this.secretCache = secretCache;

        setDataCiteMdsConfigsFromSecretsManager(environment);
    }

    private void validateInput(CreateDoiRequest input) throws InstitutionIdUnknownException,
            MissingParametersException {
        if (Objects.isNull(input)) {
            throw new MissingParametersException(ERROR_MISSING_REQUEST_JSON_BODY);
        }
        if (StringUtils.isEmpty(input.getDataciteXml())) {
            throw new MissingParametersException(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_DATACITE_XML);
        }
        if (StringUtils.isEmpty(input.getUrl())) {
            throw new MissingParametersException(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_URL);
        }
        if (StringUtils.isEmpty(input.getInstitutionId())) {
            throw new MissingParametersException(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_INSTITUTION_ID);
        }
        if (!dataCiteMdsClientConfigsMap.containsKey(input.getInstitutionId())) {
            throw new InstitutionIdUnknownException(ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER);
        }
    }

    private void setDataCiteMdsConfigsFromSecretsManager(Environment environment) {
        try {
            String secretASJson =
                    secretCache.getSecretString(environment.readEnv(ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS));
            DataCiteMdsClientConfig[] dataCiteMdsClientConfigs = objectMapper.readValue(secretASJson,
                    DataCiteMdsClientConfig[].class);
            if (dataCiteMdsClientConfigs != null) {
                for (DataCiteMdsClientConfig dataCiteMdsClientConfig : dataCiteMdsClientConfigs) {
                    dataCiteMdsClientConfigsMap.put(dataCiteMdsClientConfig.getInstitution(), dataCiteMdsClientConfig);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_RETRIEVING_DATACITE_MDS_CLIENT_CONFIGS);
        }
    }

    @Override
    protected CreateDoiResponse processInput(CreateDoiRequest input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateInput(input);

        // Configure DataCite connection for institution
        DataCiteMdsClientConfig dataCiteMdsClientConfig = dataCiteMdsClientConfigsMap.get(input.getInstitutionId());
        dataCiteMdsConnection.configure(dataCiteMdsClientConfig.getDataCiteMdsClientUrl(),
                    dataCiteMdsClientConfig.getDataCiteMdsClientUsername(),
                    dataCiteMdsClientConfig.getDataCiteMdsClientPassword());

        return createDoi(dataCiteMdsClientConfig.getInstitutionPrefix(), input.getUrl(), input.getDataciteXml());
    }

    @Override
    protected Integer getSuccessStatusCode(CreateDoiRequest input, CreateDoiResponse output) {
        return SC_CREATED;
    }

    private CreateDoiResponse createDoi(String institutionPrefix, String url, String dataciteXml)
            throws ApiGatewayException {

        // Register DOI metadata and retrieve generated DOI
        String createdDoi;
        try {
            HttpResponse<String> createMetadataResponse =
                    dataCiteMdsConnection.postMetadata(institutionPrefix, dataciteXml);
            if (createMetadataResponse.statusCode() != SC_CREATED) {
                throw new DataCiteException(ERROR_SETTING_DOI_METADATA + CHARACTER_WHITESPACE
                        + CHARACTER_PARENTHESES_START
                        + createMetadataResponse.statusCode()
                        + CHARACTER_PARENTHESES_STOP);
            }
            String createMetadataResponseBody = createMetadataResponse.body();
            createdDoi = StringUtils.substringBetween(createMetadataResponseBody, CHARACTER_PARENTHESES_START,
                    CHARACTER_PARENTHESES_STOP);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new DataCiteException(ERROR_SETTING_DOI_METADATA);
        }

        // Set DOI URL (Landing Page)
        try {
            HttpResponse<String> createDoiResponse = dataCiteMdsConnection.postDoi(createdDoi, url);
            if (createDoiResponse.statusCode() == SC_CREATED) {
                return new CreateDoiResponse(createdDoi);
            } else {
                logger.warn(ERROR_SETTING_DOI_URL
                        + CHARACTER_WHITESPACE
                        + CHARACTER_PARENTHESES_START
                        + createDoiResponse.statusCode()
                        + CHARACTER_PARENTHESES_STOP);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage());
        }

        // Registering DOI URL has failed, delete metadata
        try {
            HttpResponse<String> deleteDoiMetadata = dataCiteMdsConnection.deleteMetadata(createdDoi);
            if (deleteDoiMetadata.statusCode() == SC_OK) {
                throw new DataCiteException(ERROR_SETTING_DOI_URL);
            } else {
                logger.error(ERROR_DELETING_DOI_METADATA
                        + CHARACTER_WHITESPACE
                        + CHARACTER_PARENTHESES_START
                        + deleteDoiMetadata.statusCode()
                        + CHARACTER_PARENTHESES_STOP);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage());
        }
        logger.error(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA + CHARACTER_WHITESPACE
                + CHARACTER_PARENTHESES_START
                + createdDoi + CHARACTER_PARENTHESES_STOP);
        throw new DataCiteException(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA);
    }
}
