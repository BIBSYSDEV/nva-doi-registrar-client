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
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.PARENTHESES_START;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.PARENTHESES_STOP;
import static no.unit.nva.datacite.DataCiteMdsCreateDoiHandler.WHITESPACE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DataCiteMdsCreateDoiHandlerTest {

    public static final String QUERY_PARAMETERS_KEY = "queryParameters";
    public static final String QUERY_PARAMETER_DATACITE_XML_KEY = "dataciteXml";
    public static final String QUERY_PARAMETER_URL_KEY = "url";
    public static final String QUERY_PARAMETER_INSTITUTION_ID_KEY = "institutionId";
    public static final String MOCK_DATACITE_XML = "mock-datacite-xml";
    public static final String MOCK_URL = "mock-url";
    public static final String MOCK_KNOWN_INSTITUTION_ID = "mock-known-institution-id";
    public static final String MOCK_UNKNOWN_INSTITUTION_ID = "mock-unknown-institution-id";
    public static final String MOCK_SECRET_ID_ENV_VAR = "mock-secret-id";
    public static final String MOCK_CREATED_DOI = "prefix/suffix";
    public static final String MOCK_SECRET_UNKNOWN_INSTITUTION = "[{\"institution\": \"institution\","
            + "\"institutionPrefix\": \"institutionPrefix\"," + "\"dataCiteMdsClientUrl\": \"dataCiteMdsClientUrl\","
            + "\"dataCiteMdsClientUsername\": \"dataCiteMdsClientUsername\",\"dataCiteMdsClientPassword\": "
            + "\"dataCiteMdsClientPassword\"}]";
    public static final String MOCK_SECRET_KNOWN_INSTITUTION = "[{\"institution\": \"mock-known-institution-id\","
            + "\"institutionPrefix\": \"institutionPrefix\"," + "\"dataCiteMdsClientUrl\": \"dataCiteMdsClientUrl\","
            + "\"dataCiteMdsClientUsername\": \"dataCiteMdsClientUsername\",\"dataCiteMdsClientPassword\": "
            + "\"dataCiteMdsClientPassword\"}]";

    /**
     * Initialize mocks and Config.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final Config config = Config.getInstance();
        config.setDataCiteMdsConfigs(MOCK_SECRET_ID_ENV_VAR);
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    DataCiteMdsConnection mockDataCiteMdsConnection;

    @Mock
    SecretCache mockSecretCache;

    @Test
    public void exists() {
        new DataCiteMdsCreateDoiHandler();
    }

    @Test
    public void testSuccessfulRequest() throws IOException, URISyntaxException {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

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

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.CREATED.getStatusCode());
        expectedResponse.setBody(MOCK_CREATED_DOI);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestCauseDataciteConfigsNotFound() {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

        when(mockSecretCache.getSecretString(any())).thenReturn("");

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_RETRIEVING_DATACITE_MDS_CLIENT_CONFIGS);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseDataciteConfigNotFound() {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_UNKNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_UNKNOWN_INSTITUTION);

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.PAYMENT_REQUIRED.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_INSTITUTION_IS_NOT_SET_UP_AS_DATACITE_PROVIDER);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseMissingQueryParameters() {
        Map<String, Object> requestEvent = new HashMap<>();

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_MISSING_QUERY_PARAMETERS);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseEmptyQueryParameters() {
        HashMap<String, String> queryParameters = new HashMap<>();
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_MISSING_QUERY_PARAMETERS);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestCauseMissingQueryParameterUrl() {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_MISSING_QUERY_PARAMETER_URL);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseMissingPathParameterDataciteXml() {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_MISSING_QUERY_PARAMETER_DATACITE_XML);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }

    @Test
    public void testFailingRequestCauseMissingPathParameterInstitutionId() {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);
        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_MISSING_QUERY_PARAMETER_INSTITUTION_ID);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

    }



    @Test
    public void testFailingRequestErrorSettingDoiMetadata() throws IOException, URISyntaxException {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

        when(mockSecretCache.getSecretString(any())).thenReturn(MOCK_SECRET_KNOWN_INSTITUTION);

        CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(Response.Status.UNAUTHORIZED.getStatusCode());
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_METADATA + WHITESPACE
                + PARENTHESES_START + Response.Status.UNAUTHORIZED.getStatusCode() + PARENTHESES_STOP);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());

        when(mockDataCiteMdsConnection.postMetadata(any(), any())).thenThrow(new IOException("MOCK IOException"));

        gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_METADATA);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestErrorSettingDoiUrl() throws IOException, URISyntaxException {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

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

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_URL);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestErrorSettingDoiUrlErrorAndDeletingMetadataError() throws IOException,
            URISyntaxException {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

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

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }

    @Test
    public void testFailingRequestErrorSettingDoiUrlErrorAndExceptionDeletingMetadata() throws IOException,
            URISyntaxException {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_PARAMETER_URL_KEY, MOCK_URL);
        queryParameters.put(QUERY_PARAMETER_INSTITUTION_ID_KEY, MOCK_KNOWN_INSTITUTION_ID);
        queryParameters.put(QUERY_PARAMETER_DATACITE_XML_KEY, MOCK_DATACITE_XML);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(QUERY_PARAMETERS_KEY, queryParameters);

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

        DataCiteMdsCreateDoiHandler mockDataCiteMdsCreateDoiHandler =
                new DataCiteMdsCreateDoiHandler(mockDataCiteMdsConnection, mockSecretCache);

        GatewayResponse gatewayResponse = mockDataCiteMdsCreateDoiHandler.handleRequest(requestEvent, null);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsCreateDoiHandler.ERROR_SETTING_DOI_URL_COULD_NOT_DELETE_METADATA);

        assertEquals(expectedResponse.getStatusCode(), gatewayResponse.getStatusCode());
        assertEquals(expectedResponse.getBody(), gatewayResponse.getBody());
    }


}