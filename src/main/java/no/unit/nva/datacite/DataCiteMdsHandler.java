package no.unit.nva.datacite;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import no.unit.nva.datacite.model.generated.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for requests to Lambda function.
 */
public class DataCiteMdsHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String PATH_PARAMETER_IDENTIFIER_KEY = "identifier";

    public static final String ERROR_MISSING_PATH_PARAMETER_IDENTIFIER =
            "Missing path param '" + PATH_PARAMETER_IDENTIFIER_KEY + "'";
    public static final String ERROR_RETRIEVING_DATACITE_MDS_CLIENT_CONFIGS =
            "Error retrieving DataCite MDS client configs";
    public static final String ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER =
            "Institution is not set up as a DataCite provider";
    public static final String ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA =
            "Error setting DOI url, error deleting metadata";
    public static final String ERROR_SETTING_DOI_METADATA = "Error setting DOI metadata";
    public static final String ERROR_SETTING_DOI_URL = "Error setting DOI url";
    public static final String ERROR_CREATING_LANDING_PAGE_URL = "Error creating landing page url";

    public static final String HTTPS = "https";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String PARENTHESES_START = "(";
    public static final String PARENTHESES_STOP = ")";
    public static final String WHITESPACE = " ";

    private final transient Map<String, DataCiteMdsClientConfig> dataCiteMdsClientConfigsMap =
            new ConcurrentHashMap<>();
    private transient SecretCache secretCache = new SecretCache();
    private transient DataCiteMdsConnection dataCiteMdsConnection;

    public DataCiteMdsHandler() {

    }

    public DataCiteMdsHandler(DataCiteMdsConnection dataCiteMdsConnection, SecretCache secretCache) {
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

        // TODO: Retrieve resource metadata in datacite format directly
        //  or transform and validate metadata for datacite here
        // Resource resource;
        // String identifier;
        String institution = "unit"; // Institution from resource

        // Check if resource institution is present in datacite configs
        if (!dataCiteMdsClientConfigsMap.containsKey(institution)) {
            gatewayResponse.setErrorBody(ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER);
            gatewayResponse.setStatusCode(Response.Status.PAYMENT_REQUIRED.getStatusCode());
            return gatewayResponse;
        }

        // Create Datacite connection and perform doi creation
        DataCiteMdsClientConfig dataCiteMdsClientConfig = dataCiteMdsClientConfigsMap.get(institution);
        dataCiteMdsConnection.configure(dataCiteMdsClientConfig.getDataCiteMdsClientUrl(),
                dataCiteMdsClientConfig.getDataCiteMdsClientUsername(),
                dataCiteMdsClientConfig.getDataCiteMdsClientPassword());

        String identifier = ((Map<String, String>) input.get(PATH_PARAMETERS_KEY)).get(PATH_PARAMETER_IDENTIFIER_KEY);

        // To fully register a DOI there are two steps: post metadata and post doi (url)
        String url;
        try {
            url = createLandingPageUrl(identifier);
        } catch (URISyntaxException | MalformedURLException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(ERROR_CREATING_LANDING_PAGE_URL);
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return gatewayResponse;
        }

        Resource resource = new Resource(); // Resource fetched using identifier;

        return createDoi(gatewayResponse, dataCiteMdsClientConfig, url, resource);
    }

    private GatewayResponse createDoi(GatewayResponse gatewayResponse, DataCiteMdsClientConfig dataCiteMdsClientConfig,
                                      String url, Resource resource) {
        String createdDoi;
        try (CloseableHttpResponse createMetadataResponse =
                     dataCiteMdsConnection.postMetadata(dataCiteMdsClientConfig.getInstitutionPrefix(), resource)) {
            if (createMetadataResponse.getStatusLine().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
                gatewayResponse.setErrorBody(ERROR_SETTING_DOI_METADATA);
                gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                return gatewayResponse;
            }
            String createMetadataResponseBody = EntityUtils.toString(createMetadataResponse.getEntity(), CHARSET_UTF8);
            createdDoi = StringUtils.substringBetween(createMetadataResponseBody, PARENTHESES_START, PARENTHESES_STOP);
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setErrorBody(ERROR_SETTING_DOI_METADATA);
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return gatewayResponse;
        }

        try (CloseableHttpResponse createDoiResponse = dataCiteMdsConnection.postDoi(createdDoi, url)) {
            if (createDoiResponse.getStatusLine().getStatusCode() == Response.Status.CREATED.getStatusCode()) {
                // TODO: Does this handler persist created DOI onto resource?
                gatewayResponse.setBody(createdDoi);
                gatewayResponse.setStatusCode(Response.Status.CREATED.getStatusCode());
                return gatewayResponse;
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

    private String createLandingPageUrl(String identifier) throws URISyntaxException, MalformedURLException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getNvaHost())
                .setPathSegments(identifier)
                .build();

        return uri.toURL().toString();
    }


    @SuppressWarnings("unchecked")
    private void checkParameters(Map<String, Object> input) {
        Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
        if (Objects.isNull(pathParameters)) {
            throw new RuntimeException(ERROR_MISSING_PATH_PARAMETER_IDENTIFIER);
        }
        if (StringUtils.isEmpty(pathParameters.get(PATH_PARAMETER_IDENTIFIER_KEY))) {
            throw new RuntimeException(ERROR_MISSING_PATH_PARAMETER_IDENTIFIER);
        }
    }

    private void checkAndSetDataCiteMdsConfigs() {
        String secretASJson = secretCache.getSecretString(Config.getInstance().getDataCiteMdsConfigsSecretId());
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
