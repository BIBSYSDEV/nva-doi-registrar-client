package no.unit.nva.datacite;

import static java.util.Collections.singletonMap;
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
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_PAYMENT_REQUIRED;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;


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
    private ByteArrayOutputStream output;
    private DataCiteMdsCreateDoiHandler dataCiteMdsCreateDoiHandler;

    /**
     * Initialize mocks.
     */
    @BeforeEach
    public void setUp() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        context = mock(Context.class);

        output = new ByteArrayOutputStream();

        dataCiteMdsConnection = mock(DataCiteMdsConnection.class);
        secretCache = mock(SecretCache.class);
        when(secretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);
    }

    @Test
    @DisplayName("handler Returns Created Response With Created DOI In Body When Valid Input")
    public void handlerReturnsCreatedResponseWithCreatedDoiInBodyWhenValidInput() throws IOException,
            URISyntaxException, InterruptedException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        HttpResponse<String> httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        HttpResponse<String> httpResponsePostDoi = createHttpResponse(null, SC_CREATED);
        when(dataCiteMdsConnection.postDoi(any(), any())).thenReturn(httpResponsePostDoi);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<CreateDoiResponse> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_CREATED, gatewayResponse.getStatusCode());
        CreateDoiResponse response = objectMapper.readValue(gatewayResponse.getBody(), CreateDoiResponse.class);
        assertEquals(response.getDoi(), MOCK_CREATED_DOI);
    }

    @Test
    @DisplayName("handler Returns Payment Required Response When Provided InstitutionID Is Not Present In DataCite "
            + "Config")
    public void handlerReturnsPaymentRequiredWhenInstitutionIdDataciteConfigNotFound() throws IOException {

        when(secretCache.getSecretString(any())).thenReturn(MOCK_SECRET_UNKNOWN_INSTITUTION);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, MOCK_UNKNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_PAYMENT_REQUIRED, gatewayResponse.getStatusCode());

        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);
        assertThat(problem.getDetail(), containsString(ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER));
        assertThat(problem.getTitle(), containsString(Status.PAYMENT_REQUIRED.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.PAYMENT_REQUIRED));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing JSON In Body")
    public void handlerReturnsBadRequestWhenMissingJsonInBody() throws IOException {

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithNullBodyAndHeaders();
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_REQUEST_JSON_BODY));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Empty Attribute Values In Body Json")
    public void handlerReturnsBadRequestWhenEmptyAttributeValuesInBodyJson() throws IOException {

        when(secretCache.getSecretString(any())).thenReturn(MOCK_SECRET_UNKNOWN_INSTITUTION);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(null, null, null);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Value In Attribute 'url'")
    public void handlerReturnsBadRequestWhenMissingValueInUrlAttribute() throws IOException {

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(null, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_URL));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Value In Attribute 'dataciteXml'")
    public void handlerReturnsBadRequestWhenMissingValueInDataciteXmlAttribute() throws IOException {

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, null);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_DATACITE_XML));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }

    @Test
    @DisplayName("handler Returns Bad Request Response When Missing Value In Attribute 'institutionId'")
    public void handlerReturnsBadRequestWhenMissingValueInInstitutionIdAttribute() throws IOException {

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, null, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_MISSING_JSON_ATTRIBUTE_VALUE_INSTITUTION_ID));
        assertThat(problem.getTitle(), containsString(Status.BAD_REQUEST.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.BAD_REQUEST));
    }


    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Metadata Fails")
    public void handlerReturnsInternalServerErrorWhenSettingDoiMetadataFails() throws IOException, URISyntaxException,
            InterruptedException {

        HttpResponse<String> httpResponsePostMetadata = createHttpResponse(null, SC_UNAUTHORIZED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_INTERNAL_SERVER_ERROR, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_METADATA + CHARACTER_WHITESPACE
                + CHARACTER_PARENTHESES_START + SC_UNAUTHORIZED
                + CHARACTER_PARENTHESES_STOP));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));

        when(dataCiteMdsConnection.postMetadata(any(), any())).thenThrow(new IOException(""));

        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
        assertEquals(SC_INTERNAL_SERVER_ERROR, gatewayResponse.getStatusCode());
        problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_METADATA));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Url Fails")
    public void handlerReturnsInternalServerErrorWhenSettingDoiUrlFails() throws IOException, URISyntaxException,
            InterruptedException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        HttpResponse<String> httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        HttpResponse<String> httpResponsePostDoi = createHttpResponse(null, SC_UNAUTHORIZED);
        when(dataCiteMdsConnection.postDoi(any(), any())).thenReturn(httpResponsePostDoi);

        HttpResponse<String> httpResponseDeleteMetadata = createHttpResponse(null, SC_OK);
        when(dataCiteMdsConnection.deleteMetadata(any())).thenReturn(httpResponseDeleteMetadata);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_INTERNAL_SERVER_ERROR, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_URL));
    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Url And Deleting Metadata Fails")
    public void handlerReturnsInternalServerErrorWhenSettingDoiUrlAndDeletingMetadataFails() throws IOException,
            URISyntaxException, InterruptedException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        HttpResponse<String> httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);
        when(dataCiteMdsConnection.postDoi(any(), any())).thenThrow(new IOException(""));

        HttpResponse<String> httpResponseDeleteMetadata = createHttpResponse(null, SC_UNAUTHORIZED);
        when(dataCiteMdsConnection.deleteMetadata(any())).thenReturn(httpResponseDeleteMetadata);

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_INTERNAL_SERVER_ERROR, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA));

    }

    @Test
    @DisplayName("handler Returns Internal Server Error Response When Setting Doi Url And IOException Deleting "
            + "Metadata")
    public void handlerReturnsInternalServerErrorWhenSettingDoiUrlAndIOExceptionDeletingMetadata() throws IOException,
            URISyntaxException, InterruptedException {

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        HttpResponse<String> httpResponsePostMetadata = createHttpResponse(postMetadataResponseStream, SC_CREATED);
        when(dataCiteMdsConnection.postMetadata(any(), any())).thenReturn(httpResponsePostMetadata);

        when(dataCiteMdsConnection.postDoi(any(), any())).thenThrow(new IOException(""));
        when(dataCiteMdsConnection.deleteMetadata(any())).thenThrow(new IOException(""));

        dataCiteMdsCreateDoiHandler = new DataCiteMdsCreateDoiHandler(environment, dataCiteMdsConnection, secretCache);

        InputStream input = requestWithBodyAndHeaders(MOCK_URL, MOCK_KNOWN_INSTITUTION_ID, MOCK_DATACITE_XML);
        dataCiteMdsCreateDoiHandler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(SC_INTERNAL_SERVER_ERROR, gatewayResponse.getStatusCode());
        Problem problem = objectMapper.readValue(gatewayResponse.getBody(), Problem.class);

        assertThat(problem.getStatus(), is(Status.INTERNAL_SERVER_ERROR));
        assertThat(problem.getTitle(), containsString(Status.INTERNAL_SERVER_ERROR.getReasonPhrase()));
        assertThat(problem.getDetail(), containsString(ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA));
    }

    private HttpResponse<String> createHttpResponse(InputStream inputStream, int httpStatus) {

        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        if (inputStream != null) {
            String body = IoUtils.streamToString(inputStream);
            when(httpResponse.body()).thenReturn(body);
        }
        when(httpResponse.statusCode()).thenReturn(httpStatus);

        return httpResponse;
    }

    private InputStream requestWithBodyAndHeaders(String url, String institutionId, String dataciteXml)
            throws JsonProcessingException {

        CreateDoiRequest requestBody = new CreateDoiRequest(url, institutionId, dataciteXml);
        return new HandlerRequestBuilder<CreateDoiRequest>(objectMapper)
            .withBody(requestBody)
            .withHeaders(singletonMap(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
            .build();
    }

    private InputStream requestWithNullBodyAndHeaders() throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(objectMapper).withBody(null).build();
    }

}