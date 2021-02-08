package no.unit.nva.doi.datacite.mdsclient;

import static no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection.MISSING_DATACITE_XML_ARGUMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataCiteMdsConnectionTest {

    public static final String DATACITE_MDS_OK_RESPONSE = "dataciteMdsOkResponse.txt";
    public static final String DATACITE_MDS_POST_METADATA_RESPONSE = "dataciteMdsPostMetadataResponse.txt";
    public static final String DATACITE_MDS_GET_DOI_RESPONSE = "dataciteMdsGetDoiResponse.txt";
    public static final String DATACITE_XML_RESOURCE_EXAMPLE = "dataciteXmlResourceExample.xml";

    public static final String MOCK_HOST = "nva-mock.unit.no";
    public static final String MOCK_LANDING_PAGE_URL = "https://nva-mock.unit.no/123456789";
    public static final String MOCK_DOI_PREFIX = "prefix";
    public static final String MOCK_DOI = "prefix/suffix";
    public static final String MOCK_DATACITE_XML = "mock-xml";
    public static final String NO_METADATA = null;
    private static final int MOCK_PORT = 8888;
    @Mock
    HttpClient httpClient;

    @Mock
    HttpResponse<String> httpResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getMetadataSuccessfullyReturnsBodyInResponse()
        throws IOException, URISyntaxException, InterruptedException {
        String body = IoUtils.stringFromResources(Path.of(DATACITE_XML_RESOURCE_EXAMPLE));
        stubHttpClientWithHttpResponse(body);

        DataCiteMdsConnection dataCiteMdsConnection = createDataCiteMdsConnection();

        HttpResponse<String> httpResponse = dataCiteMdsConnection.getMetadata(MOCK_DOI);

        assertResponseContainsBody(httpResponse);
    }

    @Test
    public void deleteMetadataSuccessfullyReturnsBodyInResponse()
        throws IOException, URISyntaxException, InterruptedException {
        String body = IoUtils.stringFromResources(Path.of(DATACITE_MDS_OK_RESPONSE));
        stubHttpClientWithHttpResponse(body);

        DataCiteMdsConnection mockDataCiteMdsConnection = createDataCiteMdsConnection();

        HttpResponse<String> httpResponse = mockDataCiteMdsConnection.deleteMetadata(MOCK_DOI);

        assertResponseContainsBody(httpResponse);
    }

    @Test
    public void getDoiSuccessfullyReturnsBodyInResponse() throws IOException, URISyntaxException, InterruptedException {
        String body = IoUtils.stringFromResources(Path.of(DATACITE_MDS_GET_DOI_RESPONSE));

        stubHttpClientWithHttpResponse(body);

        DataCiteMdsConnection dataCiteMdsConnection = createDataCiteMdsConnection();

        HttpResponse<String> httpResponse = dataCiteMdsConnection.getDoi(MOCK_DOI);

        assertResponseContainsBody(httpResponse);
    }

    @Test
    public void postDoiSuccessfullyReturnsBodyInResponse()
        throws IOException, URISyntaxException, InterruptedException {
        String body = IoUtils.stringFromResources(Path.of(DATACITE_MDS_OK_RESPONSE));
        stubHttpClientWithHttpResponse(body);

        DataCiteMdsConnection mockDataCiteMdsConnection = createDataCiteMdsConnection();

        HttpResponse<String> httpResponse = mockDataCiteMdsConnection.registerUrl(MOCK_DOI, MOCK_LANDING_PAGE_URL);

        assertResponseContainsBody(httpResponse);
    }

    @Test
    public void postDoiWithoutBodyThrowsNullPointerException()
        throws IOException, InterruptedException {
        stubHttpClientWithHttpResponse(null);

        DataCiteMdsConnection mockDataCiteMdsConnection = createDataCiteMdsConnection();

        NullPointerException actualException = assertThrows(NullPointerException.class,
            () -> mockDataCiteMdsConnection.postMetadata(MOCK_DOI, NO_METADATA));
        assertThat(actualException.getMessage(), is(equalTo(MISSING_DATACITE_XML_ARGUMENT)));
    }

    @Test
    public void deleteDoiSuccessfullyReturnsBodyInResponse()
        throws IOException, URISyntaxException, InterruptedException {
        String body = IoUtils.stringFromResources(Path.of(DATACITE_MDS_OK_RESPONSE));
        stubHttpClientWithHttpResponse(body);

        DataCiteMdsConnection dataCiteMdsConnection = createDataCiteMdsConnection();

        HttpResponse<String> httpResponse = dataCiteMdsConnection.deleteDoi(MOCK_DOI);

        assertResponseContainsBody(httpResponse);
    }

    @Test
    void constructorReturnsInstanceWithoutException() {
        createDataCiteMdsConnection();
    }

    @Test
    void postMetadataSuccessfullyReturnsBodyInResponse() throws IOException, URISyntaxException, InterruptedException {
        String body = IoUtils.stringFromResources(Path.of(DATACITE_MDS_POST_METADATA_RESPONSE));
        stubHttpClientWithHttpResponse(body);

        DataCiteMdsConnection dataCiteMdsConnection = createDataCiteMdsConnection();

        HttpResponse<String> httpResponse = dataCiteMdsConnection.postMetadata(MOCK_DOI_PREFIX, MOCK_DATACITE_XML);

        assertResponseContainsBody(httpResponse);
    }

    private void assertResponseContainsBody(HttpResponse<String> httpResponse) {
        assertNotNull(httpResponse);
        assertNotNull(httpResponse.body());
    }

    private DataCiteMdsConnection createDataCiteMdsConnection() {
        return new DataCiteMdsConnection(httpClient, MOCK_HOST, MOCK_PORT);
    }

    private void stubHttpClientWithHttpResponse(String body) throws IOException, InterruptedException {
        when(httpResponse.body()).thenReturn(body);
        when(httpClient.send(any(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(httpResponse);
    }
}