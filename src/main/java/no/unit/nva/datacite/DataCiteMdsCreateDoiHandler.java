package no.unit.nva.datacite;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;



/**
 * Handler for requests to Lambda function.
 */
public class DataCiteMdsCreateDoiHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String QUERY_PARAMETERS_KEY = "queryStringParameters";
    public static final String QUERY_PARAMETER_DATACITE_XML_KEY = "dataciteXml";
    public static final String QUERY_PARAMETER_URL_KEY = "url";
    public static final String QUERY_PARAMETER_INSTITUTION_ID_KEY = "institutionId";

    public static final String ERROR_MISSING_QUERY_PARAMETERS =
            "Query parameters 'institutionId', 'dataciteXml' and 'url' are mandatory";
    public static final String ERROR_MISSING_QUERY_PARAMETER_DATACITE_XML =
            "Query parameter 'dataciteXml' is mandatory";
    public static final String ERROR_MISSING_QUERY_PARAMETER_URL =
            "Query parameter 'url' is mandatory";
    public static final String ERROR_MISSING_QUERY_PARAMETER_INSTITUTION_ID =
            "Query parameter 'institutionId' is mandatory";
    public static final String ERROR_RETRIEVING_DATACITE_MDS_CLIENT_CONFIGS =
            "Error retrieving DataCite MDS client configs";
    public static final String ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER =
            "Institution is not set up as a DataCite provider";
    public static final String ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA =
            "Error setting DOI url, error deleting metadata";
    public static final String ERROR_SETTING_DOI_METADATA = "Error setting DOI metadata";
    public static final String ERROR_SETTING_DOI_URL = "Error setting DOI url";
    public static final String ERROR_DELETING_DOI_METADATA = "Error deleting DOI metadata";

    public static final String PARENTHESES_START = "(";
    public static final String PARENTHESES_STOP = ")";
    public static final String WHITESPACE = " ";

    private final transient Map<String, DataCiteMdsClientConfig> dataCiteMdsClientConfigsMap =
            new ConcurrentHashMap<>();
    private transient SecretCache secretCache = new SecretCache();
    private transient DataCiteMdsConnection dataCiteMdsConnection;

    public DataCiteMdsCreateDoiHandler() {
    }

    public DataCiteMdsCreateDoiHandler(DataCiteMdsConnection dataCiteMdsConnection, SecretCache secretCache) {
        this.dataCiteMdsConnection = dataCiteMdsConnection;
        this.secretCache = secretCache;
    }

    /**
     * Main lambda function.
     *
     * @return a GatewayResponse
     */
    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(final Map<String, Object> input, Context context) {
        GatewayResponse gatewayResponse = new GatewayResponse();

        try {
            this.checkParameters(input);
        } catch (RuntimeException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }

        try {
            Config.getInstance().checkProperties();
            this.checkAndSetDataCiteMdsConfigs();
        } catch (RuntimeException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return gatewayResponse;
        }

        Map<String, String> queryParameters = (Map<String, String>) input.get(QUERY_PARAMETERS_KEY);
        String institutionId = queryParameters.get(QUERY_PARAMETER_INSTITUTION_ID_KEY);

        // Check if resource institution is present in datacite configs
        if (!dataCiteMdsClientConfigsMap.containsKey(institutionId)) {
            gatewayResponse.setErrorBody(ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER);
            gatewayResponse.setStatusCode(Response.Status.PAYMENT_REQUIRED.getStatusCode());
            return gatewayResponse;
        }

        // Create Datacite connection and perform doi creation
        DataCiteMdsClientConfig dataCiteMdsClientConfig = dataCiteMdsClientConfigsMap.get(institutionId);
        dataCiteMdsConnection.configure(dataCiteMdsClientConfig.getDataCiteMdsClientUrl(),
                dataCiteMdsClientConfig.getDataCiteMdsClientUsername(),
                dataCiteMdsClientConfig.getDataCiteMdsClientPassword());


        String dataciteXml = queryParameters.get(QUERY_PARAMETER_DATACITE_XML_KEY);
        String url = queryParameters.get(QUERY_PARAMETER_URL_KEY);
        return createDoi(gatewayResponse, dataCiteMdsClientConfig, url, dataciteXml);
    }

    private GatewayResponse createDoi(GatewayResponse gatewayResponse, DataCiteMdsClientConfig dataCiteMdsClientConfig,
                                      String url, String dataciteXml) {
        String createdDoi;
        try (CloseableHttpResponse createMetadataResponse =
                     dataCiteMdsConnection.postMetadata(dataCiteMdsClientConfig.getInstitutionPrefix(), dataciteXml)) {
            if (createMetadataResponse.getStatusLine().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
                gatewayResponse.setErrorBody(ERROR_SETTING_DOI_METADATA + WHITESPACE + PARENTHESES_START
                        + createMetadataResponse.getStatusLine().getStatusCode() + PARENTHESES_STOP);
                gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                return gatewayResponse;
            }
            String createMetadataResponseBody = EntityUtils.toString(createMetadataResponse.getEntity(),
                    StandardCharsets.UTF_8.name());
            createdDoi = StringUtils.substringBetween(createMetadataResponseBody, PARENTHESES_START, PARENTHESES_STOP);
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setErrorBody(ERROR_SETTING_DOI_METADATA);
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return gatewayResponse;
        }

        try (CloseableHttpResponse createDoiResponse = dataCiteMdsConnection.postDoi(createdDoi, url)) {
            if (createDoiResponse.getStatusLine().getStatusCode() == Response.Status.CREATED.getStatusCode()) {
                gatewayResponse.setBody(createdDoi);
                gatewayResponse.setStatusCode(Response.Status.CREATED.getStatusCode());
                return gatewayResponse;
            } else {
                System.out.println(ERROR_SETTING_DOI_URL + WHITESPACE + PARENTHESES_START
                        + createDoiResponse.getStatusLine().getStatusCode() + PARENTHESES_STOP);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println(e);
        }

        // Delete metadata - registering DOI url failed
        try (CloseableHttpResponse deleteDoiMetadata = dataCiteMdsConnection.deleteMetadata(createdDoi)) {
            if (deleteDoiMetadata.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode()) {
                gatewayResponse.setErrorBody(ERROR_SETTING_DOI_URL);
                gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                return gatewayResponse;
            } else {
                System.out.println(ERROR_DELETING_DOI_METADATA + WHITESPACE + PARENTHESES_START
                        + deleteDoiMetadata.getStatusLine().getStatusCode() + PARENTHESES_STOP);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println(e);
        }
        System.out.println(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA + WHITESPACE + PARENTHESES_START
                + createdDoi + PARENTHESES_STOP);
        gatewayResponse.setErrorBody(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA);
        gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return gatewayResponse;
    }


    @SuppressWarnings("unchecked")
    private void checkParameters(Map<String, Object> input) {
        if (Objects.isNull(input) || !input.containsKey(QUERY_PARAMETERS_KEY)
                || Objects.isNull(input.get(QUERY_PARAMETERS_KEY))
                || ((Map<String, Object>) input.get(QUERY_PARAMETERS_KEY)).isEmpty()) {
            throw new RuntimeException(ERROR_MISSING_QUERY_PARAMETERS);
        }
        Map<String, String> queryParameters = (Map<String, String>) input.get(QUERY_PARAMETERS_KEY);
        String dataciteXml = queryParameters.get(QUERY_PARAMETER_DATACITE_XML_KEY);
        if (StringUtils.isEmpty(dataciteXml)) {
            throw new RuntimeException(ERROR_MISSING_QUERY_PARAMETER_DATACITE_XML);
        }
        String url = queryParameters.get(QUERY_PARAMETER_URL_KEY);
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException(ERROR_MISSING_QUERY_PARAMETER_URL);
        }
        String institutionId = queryParameters.get(QUERY_PARAMETER_INSTITUTION_ID_KEY);
        if (StringUtils.isEmpty(institutionId)) {
            throw new RuntimeException(ERROR_MISSING_QUERY_PARAMETER_INSTITUTION_ID);
        }
    }

    private void checkAndSetDataCiteMdsConfigs() {
        String secretASJson = secretCache.getSecretString(Config.getInstance().getDataCiteMdsConfigs());
        if (StringUtils.isEmpty(secretASJson)) {
            throw new RuntimeException(ERROR_RETRIEVING_DATACITE_MDS_CLIENT_CONFIGS);
        }
        DataCiteMdsClientConfig[] dataCiteMdsClientConfigs = new Gson().fromJson(secretASJson,
                DataCiteMdsClientConfig[].class);
        if (dataCiteMdsClientConfigs != null) {
            for (DataCiteMdsClientConfig dataCiteMdsClientConfig : dataCiteMdsClientConfigs) {
                dataCiteMdsClientConfigsMap.put(dataCiteMdsClientConfig.getInstitution(), dataCiteMdsClientConfig);
            }
        }
    }

}
