package no.unit.nva.datacite;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.testutils.TestContext;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.datacite.DataCiteMdsConnectionTest.DATACITE_MDS_POST_METADATA_RESPONSE;

import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.CHARACTER_PARENTHESES_START;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.CHARACTER_PARENTHESES_STOP;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.CHARACTER_WHITESPACE;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_MISSING_JSON_ATTRIBUTE_VALUE_DATACITE_XML;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_MISSING_JSON_ATTRIBUTE_VALUE_INSTITUTION_ID;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_MISSING_JSON_ATTRIBUTE_VALUE_URL;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_MISSING_REQUEST_JSON_BODY;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_METADATA;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_URL;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA;
import static nva.commons.handlers.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DataCiteMdsCreateDoiHandlerTest {

    public static final String MOCK_DATACITE_XML = "mock-datacite-xml";
    public static final String MOCK_URL = "mock-url";
    public static final String MOCK_KNOWN_INSTITUTION_ID = "mock-known-institution-id";
    public static final String MOCK_UNKNOWN_INSTITUTION_ID = "mock-unknown-institution-id";
    public static final String MOCK_CREATED_DOI = "prefix/suffix";
    public static final String MOCK_SECRET_UNKNOWN_INSTITUTION = "[{\"institution\": \"institution\","
            + "\"institutionPrefix\": \"institutionPrefix\"," + "\"dataCiteMdsClientUrl\": \"dataCiteMdsClientUrl\","
            + "\"dataCiteMdsClientUsername\": \"dataCiteMdsClientUsername\",\"dataCiteMdsClientPassword\": "
            + "\"dataCiteMdsClientPassword\"}]";
    public static final String MOCK_SECRET_KNOWN_INSTITUTION = "[{\"institution\": \"mock-known-institution-id\","
            + "\"institutionPrefix\": \"institutionPrefix\"," + "\"dataCiteMdsClientUrl\": \"dataCiteMdsClientUrl\","
            + "\"dataCiteMdsClientUsername\": \"dataCiteMdsClientUsername\",\"dataCiteMdsClientPassword\": "
            + "\"dataCiteMdsClientPassword\"}]";

    private Environment environment;
    private Context context;
    private DataCiteMdsConnection dataCiteMdsConnection;
    private SecretCache secretCache;
    private OutputStream output;
    private DataCiteMdsCreateDoiHandler dataCiteMdsCreateDoiHandler;

    /**
     * Initialize mocks.
     */
    @BeforeEach
    public void setUp() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        context = new TestContext();

        output = new ByteArrayOutputStream();

        dataCiteMdsConnection = mock(DataCiteMdsConnection.class);
        secretCache = mock(SecretCache.class);
        when(secretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);
    }

    @Test
    @DisplayName("handler Default Constructor Throws Exception When Envs Are Not Set")
    public void defaultConstructorThrowsExceptionWhenEnvsAreNotSet() {
        assertThrows(IllegalStateException.class, DataCiteMdsCreateDoiHandler::new);
    }


    @Test
    @DisplayName("handler Returns Created Response With Created DOI In Body On Valid Input")
    public void handlerReturnsCreatedResponseWithCreatedDoiInBodyOnValidInput() throws IOException, URISyntaxException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        CloseableHttpResponse httpResponsePostDoi = createHttpResponse(null, SC_CREATED);
        when(dataCiteMdsConnection.postDoi(any(), any())).thenReturn(httpResponsePostDoi);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<CreateDoiResponse> response = objectMapper.readValue(output.toString(), GatewayResponse.class);

        assertEquals(response.getStatusCode(), SC_CREATED);
        assertEquals(response.getBodyObject(CreateDoiResponse.class).getDoi(), MOCK_CREATED_DOI);
    }

    @Test
    @DisplayName("handler Returns Payment Required Response When Provided InstitutionID Is Not Present In DataCite "
            + "Config")
    public void handlerReturnsPaymentRequiredWhenInstitutionIdDataciteConfigNotFound() throws IOException {

        when(secretCache.getSecretString(any())).thenReturn(MOCK_SECRET_UNKNOWN_INSTITUTION);

        DataCiteMdsCreateDoiHandler dataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithHeaders(MOCK_URL, MOCK_UNKNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);

        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), containsString(ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER));
        assertThat(problem.getTitle(), containsString(Status.PAYMENT_REQUIRED.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.PAYMENT_REQUIRED));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing JSON In Body")
    public void handlerReturnsBadRequestWhenMissingJsonInBody() throws IOException {

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithoutBodyAndHeaders();
        mockDataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);

        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_REQUEST_JSON_BODY));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Empty Attribute Values In Body Json")
    public void handlerReturnsBadRequestWhenEmptyAttributeValuesInBodyJson() throws IOException {

        when(secretCache.getSecretString(any())).thenReturn(MOCK_SECRET_UNKNOWN_INSTITUTION);

        InputStream input = requestWithHeaders(null, null, null);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);

        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Value In Attribute 'url'")
    public void handlerReturnsBadRequestWhenMissingValueInUrlAttribute() throws IOException {

        InputStream input = requestWithHeaders(null, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);

        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_URL));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Value In Attribute 'dataciteXml'")
    public void handlerReturnsBadRequestWhenMissingValueInDataciteXmlAttribute() throws IOException {

        InputStream input = requestWithHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, null);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);

        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_DATACITE_XML));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Value In Attribute 'institutionId'")
    public void handlerReturnsBadRequestWhenMissingValueInInstitutionIdAttribute() throws IOException {

        InputStream input = requestWithHeaders(MOCK_URL, null, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);

        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_INSTITUTION_ID));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }


    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Metadata Fails")
    public void handlerReturnsInternalServerErrorWhenSettingDoiMetadataFails() throws IOException, URISyntaxException {

        CloseableHttpResponse httpResponsePostMetadata = createHttpResponse(null, SC_UNAUTHORIZED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        InputStream input = requestWithHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);

        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_METADATA + CHARACTER_WHITESPACE
                + CHARACTER_PARENTHESES_START + SC_UNAUTHORIZED
                + CHARACTER_PARENTHESES_STOP));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));

        when(dataCiteMdsConnection.postMetadata(any(), any())).thenThrow(new IOException(""));

        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);
        gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
        problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_METADATA));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Url Fails")
    public void handlerReturnsInternalServerErrorWhenSettingDoiUrlFails() throws IOException, URISyntaxException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        CloseableHttpResponse httpResponsePostDoi = createHttpResponse(null, SC_UNAUTHORIZED);
        when(dataCiteMdsConnection.postDoi(any(), any())).thenReturn(httpResponsePostDoi);

        CloseableHttpResponse httpResponseDeleteMetadata = createHttpResponse(null, SC_OK);
        when(dataCiteMdsConnection.deleteMetadata(any())).thenReturn(httpResponseDeleteMetadata);

        InputStream input = requestWithHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);
        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_URL));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Url And Deleting Metadata Fails")
    public void handlerReturnsInternalServerErrorWhenSettingDoiUrlAndDeletingMetadataFails() throws IOException,
            URISyntaxException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        when(dataCiteMdsConnection.postDoi(any(), any())).thenThrow(new IOException(""));

        CloseableHttpResponse httpResponseDeleteMetadata = createHttpResponse(null, SC_UNAUTHORIZED);
        when(dataCiteMdsConnection.deleteMetadata(any())).thenReturn(httpResponseDeleteMetadata);

        InputStream input = requestWithHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);
        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA));

    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Url And IOException Deleting "
            + "Metadata")
    public void handlerReturnsInternalServerErrorWhenSettingDoiUrlAndIOExceptionDeletingMetadata() throws IOException,
            URISyntaxException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        when(dataCiteMdsConnection.postDoi(any(), any())).thenThrow(new IOException(""));
        when(dataCiteMdsConnection.deleteMetadata(any())).thenThrow(new IOException(""));

        InputStream input = requestWithHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);
        GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
        Problem problem = (Problem) gatewayResponse.getBodyObject(Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA));
    }

    private CloseableHttpResponse createHttpResponse(InputStream inputStream, int httpStatus) throws IOException {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        httpResponse.setEntity(entity);
        when(entity.getContent()).thenReturn(inputStream);
        when(httpResponse.getEntity()).thenReturn(entity);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(httpStatus);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        return httpResponse;
    }

    private InputStream jsonNodeToInputStream(JsonNode request) throws JsonProcessingException {
        String requestString = objectMapper.writeValueAsString(request);
        return IoUtils.stringToStream(requestString);
    }

    private InputStream requestWithHeaders(String url, String institutionId, String dataciteXml)
            throws JsonProcessingException {
        ObjectNode request = objectMapper.createObjectNode();
        ObjectNode node = createBody(url, institutionId, dataciteXml);
        request.set("body", node);
        request.set("headers", createHeaders());
        return jsonNodeToInputStream(request);
    }

    private InputStream requestWithoutBodyAndHeaders() throws JsonProcessingException {
        ObjectNode request = objectMapper.createObjectNode();
        return jsonNodeToInputStream(request);
    }

    private JsonNode createHeaders() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        return objectMapper.convertValue(headers, JsonNode.class);
    }

    private ObjectNode createBody(String url, String institutionId, String dataciteXml) {
        CreateDoiRequest createDoiRequest = new CreateDoiRequest(url, institutionId, dataciteXml);
        return objectMapper.convertValue(createDoiRequest, ObjectNode.class);
    }

}