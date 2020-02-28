package no.unit.nva.datacite;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import no.unit.nva.datacite.model.generated.*;
import no.unit.nva.model.Publication;
import no.unit.nva.model.Publisher;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ServiceException;
import org.zalando.problem.ProblemModule;


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
    public static final String ERROR_DELETING_DOI_METADATA = "Error deleting DOI metadata";
    public static final String ERROR_CREATING_LANDING_PAGE_URL = "Error creating landing page url";
    public static final String ERROR_CREATING_DATACITE_XML = "Error creating Datacite XML";

    public static final String HTTPS = "https";
    public static final String PARENTHESES_START = "(";
    public static final String PARENTHESES_STOP = ")";
    public static final String WHITESPACE = " ";

    private final transient Map<String, DataCiteMdsClientConfig> dataCiteMdsClientConfigsMap =
            new ConcurrentHashMap<>();
    private transient SecretCache secretCache = new SecretCache();
    private transient DataCiteMdsConnection dataCiteMdsConnection;

    private transient PublicationConverter publicationConverter = new PublicationConverter();

    private final transient ObjectMapper objectMapper = createObjectMapper();
    private final transient ObjectMapper xmlMapper = createXmlMapper();

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

        String identifier = ((Map<String, String>) input.get(PATH_PARAMETERS_KEY)).get(PATH_PARAMETER_IDENTIFIER_KEY);

        Publication publication = retrievePublication(identifier);

        String institution = publication.getPublisher().getIdentifier().toString(); // Institution from resource

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

        Resource resource = publicationConverter.toResource(publication);
        String dataciteXml;
        try {
            dataciteXml = new XmlMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                    .writeValueAsString(resource);
        } catch (JsonProcessingException e) {
            gatewayResponse.setErrorBody(ERROR_CREATING_DATACITE_XML);
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return gatewayResponse;
        }

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
                // TODO: Does this handler persist created DOI onto resource?
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

    private String createLandingPageUrl(String identifier) throws URISyntaxException, MalformedURLException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(Config.getInstance().getNvaFrontendHost())
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

    protected Publication retrievePublication(String identifier) {

//        InvokeRequest invokeRequest = new InvokeRequest()
//                .withInvocationType(InvocationType.RequestResponse)
//                .withFunctionName(Config.getInstance().getNvaFetchResourceFunctionName())
//                .withPayload("{\"httpMethod\" : \"GET\",\"pathParameters\" : {\"identifier\" : \"" + identifier +
//                        "\"}}");
//
//        try {
//            AWSLambda awsLambda = AWSLambdaClientBuilder.defaultClient();
//            InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
//
//            if (invokeResult.getStatusCode() == Response.Status.OK.getStatusCode()) {
//
//                try {
//                    String result = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
//                    JsonNode event = objectMapper.readTree(result);
//                    Iterator<JsonNode> elements = event.get("body").get("Items").elements();
//                    String json = objectMapper.writeValueAsString(elements.next());
//
//                    Publication publication = objectMapper.readValue(json, Publication.class);
//                    return publication;
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        } catch (ServiceException e) {
//            System.out.println(e);
//        }
        return null;
    }

    protected String createDataciteResourceXml(Publication publication) throws MalformedURLException, JsonProcessingException {
        Resource resource = publicationConverter.toResource(publication);
        return xmlMapper.writeValueAsString(resource);
    }

    /**
     * Create ObjectMapper.
     *
     * @return objectMapper
     */
    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new ProblemModule())
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    private ObjectMapper createXmlMapper() {
        return new XmlMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }
}
