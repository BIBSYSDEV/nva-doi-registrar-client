package no.unit.nva.datacite;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class DataCiteMdsHandlerTest {

    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String BODY_KEY = "body";
    public static final String PATH_PARAM_IDENTIFIER_KEY = "identifier";
    public static final String MOCK_IDENTIFIER = "mock-identifier";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    DataCiteMdsConnection mockDataCiteMdsConnection;

    @Mock
    CloseableHttpClient mockHttpClient;
    @Mock
    CloseableHttpResponse mockCloseableHttpResponse;
    @Mock
    HttpEntity mockEntity;

    @Test
    public void testFailingRequestCauseDataciteConfigsNotFound() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        assertEquals(gatewayResponse.getStatusCode(), expectedResponse.getStatusCode());
    }

    @Test
    public void testFailingRequestCauseDataciteConfigNotFound() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    public void testFailingRequestCauseMissingPathParameters() {
        Map<String, Object> requestEvent = new HashMap<>();

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.MISSING_PATH_PARAMETER_IDENTIFIER);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        assertEquals(gatewayResponse.getStatusCode(), expectedResponse.getStatusCode());

    }

    @Test
    public void testFailingRequestCauseEmptyPathParameters() {
        HashMap<String, String> pathParams = new HashMap<>();
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.MISSING_PATH_PARAMETER_IDENTIFIER);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        assertEquals(gatewayResponse.getStatusCode(), expectedResponse.getStatusCode());
    }

    @Test
    public void testFailingRequestCauseMissingPathParameterIdentifier() {
        HashMap<String, String> pathParams = new HashMap<>();
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
        expectedResponse.setErrorBody(DataCiteMdsHandler.MISSING_PATH_PARAMETER_IDENTIFIER);

        DataCiteMdsHandler mockDataCiteMdsHandler = new DataCiteMdsHandler(mockDataCiteMdsConnection);
        GatewayResponse gatewayResponse = mockDataCiteMdsHandler.handleRequest(requestEvent, null);

        assertEquals(gatewayResponse.getStatusCode(), expectedResponse.getStatusCode());

    }

    @Test
    public void testFailingRequestErrorSettingDoiMetadata() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    public void testFailingRequestErrorSettingDoiUrl() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    public void testFailingRequestErrorSettingDoiUrlErrorDeletingMetadata() {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put(PATH_PARAM_IDENTIFIER_KEY, MOCK_IDENTIFIER);
        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put(PATH_PARAMETERS_KEY, pathParams);

        GatewayResponse expectedResponse = new GatewayResponse();
        expectedResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());


    }

}