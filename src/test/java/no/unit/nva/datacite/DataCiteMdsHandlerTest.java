package no.unit.nva.datacite;

import com.amazonaws.secretsmanager.caching.SecretCache;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static no.unit.nva.datacite.DataCiteMdsConnectionTest.DATACITE_MDS_POST_METADATA_RESPONSE;
import static no.unit.nva.datacite.DataCiteMdsHandler.PARENTHESES_START;
import static no.unit.nva.datacite.DataCiteMdsHandler.PARENTHESES_STOP;
import static no.unit.nva.datacite.DataCiteMdsHandler.WHITESPACE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DataCiteMdsHandlerTest {

    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String PATH_PARAM_IDENTIFIER_KEY = "identifier";
    public static final String MOCK_IDENTIFIER = "mock-identifier";
    public static final String MOCK_SECRET_ID_ENV_VAR = "mock-secret-id";
    public static final String MOCK_HOST_ENV_VAR = "nva-mock.unit.no";
    public static final String MOCK_INVALID_HOST = "%nva-mock.unit.no";
    public static final String MOCK_CREATED_DOI = "prefix/suffix";
    public static final String MOCK_SECRET_UNKNOWN_INSTITUTION = "[{\"institution\": \"institution\","
            + "\"institutionPrefix\": \"institutionPrefix\"," + "\"dataCiteMdsClient_url\": \"dataCiteMdsClient_url\","
            + "\"dataCiteMdsClient_username\": \"dataCiteMdsClient_username\",\"dataCiteMdsClient_password\": "
            + "\"dataCiteMdsClient_password\"}]";
    public static final String MOCK_SECRET_KNOWN_INSTITUTION = "[{\"institution\": \"unit\","
            + "\"institutionPrefix\": \"institutionPrefix\"," + "\"dataCiteMdsClient_url\": \"dataCiteMdsClient_url\","
            + "\"dataCiteMdsClient_username\": \"dataCiteMdsClient_username\",\"dataCiteMdsClient_password\": "
            + "\"dataCiteMdsClient_password\"}]";

    /**
     * Initialize mocks and Config.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final Config config = Config.getInstance();
        config.setNvaHost(MOCK_HOST_ENV_VAR);
        config.setDataCiteMdsConfigsSecretId(MOCK_SECRET_ID_ENV_VAR);
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    DataCiteMdsConnection mockDataCiteMdsConnection;

    @Mock
    SecretCache mockSecretCache;

    @Test
    public void exists() {
        new DataCiteMdsHandler();
    }

    @Test
    public void testSuccessfulRequest() throws IOException, URISyntaxException {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(postMetadataResponseStream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenReturn(mockCloseableHttpResponse);

        CloseableHttpResponse mockCloseableHttpResponse2 = mock(CloseableHttpResponse.class);
        HttpEntity mockEntity2 = mock(HttpEntity.class);
        mockCloseableHttpResponse2.setEntity(mockEntity2);
        StatusLine mockStatusLine2 = mock(StatusLine.class);
        when(mockStatusLine2.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockCloseableHttpResponse2.getStatusLine()).thenReturn(mockStatusLine2);
        when(mockDataCiteMdsConnection.postDoi(any(), any())).thenReturn(mockCloseableHttpResponse2);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.CREATED.getStatusCode());
        expectedResponse.setBody(MOCK_CREATED_DOI);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestCauseDataciteConfigsNotFound() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn("");

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_RETRIEVING_DATACITE_MDS_CLIENT_CONFIGS);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseDataciteConfigNotFound() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_UNKNOWN_INSTITUTION);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.PAYMENT_REQUIRED.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseMissingPathParameters() {
        Map<String, Object> requestEvent = new HashMap<>();

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_MISSING_PATH_PARAMETER_IDENTIFIER);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseEmptyPathParameters() {
        HashMap<String, String> pathParams = new HashMap<>();
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_MISSING_PATH_PARAMETER_IDENTIFIER);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingPathParameterIdentifier() {
        HashMap<String, String> pathParams = new HashMap<>();
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_MISSING_PATH_PARAMETER_IDENTIFIER);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseInvalidNvaHostInEnvironment() {
        Config.getInstance().setNvaHost(MOCK_INVALID_HOST);

        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_CREATING_LANDING_PAGE_URL);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestErrorSettingDoiMetadata() throws IOException, URISyntaxException {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_SETTING_DOI_METADATA + WHITESPACE + PARENTHESES_START
                + Response.Status.UNAUTHORIZED.getStatusCode() + PARENTHESES_STOP);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenThrow(new IOException("MOCK IOException"));

        gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_SETTING_DOI_METADATA);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestErrorSettingDoiUrl() throws IOException, URISyntaxException {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(postMetadataResponseStream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenReturn(mockCloseableHttpResponse);

        CloseableHttpResponse mockCloseableHttpResponse2 = mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine2 = mock(StatusLine.class);
        when(mockStatusLine2.getStatusCode()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        when(mockCloseableHttpResponse2.getStatusLine()).thenReturn(mockStatusLine2);
        when(mockDataCiteMdsConnection.postDoi(any(), any())).thenReturn(mockCloseableHttpResponse2);

        CloseableHttpResponse mockCloseableHttpResponse3 = mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine3 = mock(StatusLine.class);
        when(mockStatusLine3.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockCloseableHttpResponse3.getStatusLine()).thenReturn(mockStatusLine3);
        when(mockDataCiteMdsConnection.deleteMetadata(any())).thenReturn(mockCloseableHttpResponse3);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_SETTING_DOI_URL);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestErrorSettingDoiUrlErrorAndDeletingMetadataError() throws IOException,
            URISyntaxException {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(postMetadataResponseStream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenReturn(mockCloseableHttpResponse);

        when(mockDataCiteMdsConnection.postDoi(any(), any())).thenThrow(new IOException("MOCK IOException"));

        CloseableHttpResponse mockCloseableHttpResponse2 = mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine2 = mock(StatusLine.class);
        when(mockStatusLine2.getStatusCode()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        when(mockCloseableHttpResponse2.getStatusLine()).thenReturn(mockStatusLine2);
        when(mockDataCiteMdsConnection.deleteMetadata(any())).thenReturn(mockCloseableHttpResponse2);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestErrorSettingDoiUrlErrorAndExceptionDeletingMetadata() throws IOException,
            URISyntaxException {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        InputStream postMetadataResponseStream =
                DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(postMetadataResponseStream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenReturn(mockCloseableHttpResponse);

        when(mockDataCiteMdsConnection.postDoi(any(), any())).thenThrow(new IOException("MOCK IOException"));
        when(mockDataCiteMdsConnection.deleteMetadata(any())).thenThrow(new IOException("MOCK IOException"));

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

}