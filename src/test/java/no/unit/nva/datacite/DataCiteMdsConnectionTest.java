package no.unit.nva.datacite;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataCiteMdsConnectionTest {

    public static final String DATACITE_MDS_OK_RESPONSE = "/dataciteMdsOkResponse.txt";
    public static final String DATACITE_MDS_POST_METADATA_RESPONSE = "/dataciteMdsPostMetadataResponse.txt";
    public static final String DATACITE_MDS_GET_DOI_RESPONSE = "/dataciteMdsGetDoiResponse.txt";

    public static final String DATACITE_XML_RESOURCE_EXAMPLE = "/dataciteXmlResourceExample.xml";
    public static final String MOCK_HOST = "nva-mock.unit.no";
    public static final String MOCK_LANDING_PAGE_URL = "https://nva-mock.unit.no/123456789";
    public static final String MOCK_DOI_PREFIX = "prefix";
    public static final String MOCK_DOI = "prefix/suffix";
    public static final String MOCK_DATACITE_XML = "mock-xml";
    public static final String MOCK_USER = "MOCK_USER";
    public static final String MOCK_PASSWORD = "MOCK_PASSWORD";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    CloseableHttpClient mockHttpClient;
    @Mock
    CloseableHttpResponse mockCloseableHttpResponse;
    @Mock
    HttpEntity mockEntity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void dataCiteMdsConnectionTest() {
        DataCiteMdsConnection dataCiteMdsConnection = new DataCiteMdsConnection(MOCK_HOST, MOCK_USER, MOCK_PASSWORD);
        assertNotNull(dataCiteMdsConnection);
    }

    @Test
    public void dataCiteMdsConnectionConfigureTest() {
        DataCiteMdsConnection dataCiteMdsConnection = new DataCiteMdsConnection(MOCK_HOST, MOCK_USER, MOCK_PASSWORD);
        dataCiteMdsConnection.configure(MOCK_HOST, MOCK_USER, MOCK_PASSWORD);
        assertNotNull(dataCiteMdsConnection);
    }

    @Test
    public void postMetadataTest() throws IOException, URISyntaxException {
        InputStream stream = DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_POST_METADATA_RESPONSE);
        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsConnection mockDataCiteMdsConnection = new DataCiteMdsConnection(mockHttpClient, MOCK_HOST);

        CloseableHttpResponse httpResponse = mockDataCiteMdsConnection.postMetadata(MOCK_DOI_PREFIX, MOCK_DATACITE_XML);

        assertNotNull(httpResponse);
        assertNotNull(EntityUtils.toString(httpResponse.getEntity()));

    }

    @Test
    public void getMetadataTest() throws IOException, URISyntaxException {
        InputStream stream = DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_XML_RESOURCE_EXAMPLE);

        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsConnection mockDataCiteMdsConnection = new DataCiteMdsConnection(mockHttpClient, MOCK_HOST);

        CloseableHttpResponse httpResponse = mockDataCiteMdsConnection.getMetadata(MOCK_DOI);

        assertNotNull(httpResponse);
        assertNotNull(EntityUtils.toString(httpResponse.getEntity()));
    }

    @Test
    public void deleteMetadataTest() throws IOException, URISyntaxException {
        InputStream stream = DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_OK_RESPONSE);

        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsConnection mockDataCiteMdsConnection = new DataCiteMdsConnection(mockHttpClient, MOCK_HOST);

        CloseableHttpResponse httpResponse = mockDataCiteMdsConnection.deleteMetadata(MOCK_DOI);

        assertNotNull(httpResponse);
        assertNotNull(EntityUtils.toString(httpResponse.getEntity()));
    }

    @Test
    public void getDoiTest() throws IOException, URISyntaxException {
        InputStream stream = DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_GET_DOI_RESPONSE);

        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsConnection mockDataCiteMdsConnection = new DataCiteMdsConnection(mockHttpClient, MOCK_HOST);

        CloseableHttpResponse httpResponse = mockDataCiteMdsConnection.getDoi(MOCK_DOI);

        assertNotNull(httpResponse);
        assertNotNull(EntityUtils.toString(httpResponse.getEntity()));
    }

    @Test
    public void postDoiTest() throws IOException, URISyntaxException {
        InputStream stream = DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_OK_RESPONSE);

        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsConnection mockDataCiteMdsConnection = new DataCiteMdsConnection(mockHttpClient, MOCK_HOST);

        CloseableHttpResponse httpResponse = mockDataCiteMdsConnection.postDoi(MOCK_DOI, MOCK_LANDING_PAGE_URL);

        assertNotNull(httpResponse);
        assertNotNull(EntityUtils.toString(httpResponse.getEntity()));
    }

    @Test
    public void deleteDoiTest() throws IOException, URISyntaxException {
        InputStream stream = DataCiteMdsConnectionTest.class.getResourceAsStream(DATACITE_MDS_OK_RESPONSE);

        mockCloseableHttpResponse.setEntity(mockEntity);
        when(mockEntity.getContent()).thenReturn(stream);
        when(mockCloseableHttpResponse.getEntity()).thenReturn(mockEntity);
        when(mockHttpClient.execute(any())).thenReturn(mockCloseableHttpResponse);

        DataCiteMdsConnection mockDataCiteMdsConnection = new DataCiteMdsConnection(mockHttpClient, MOCK_HOST);

        CloseableHttpResponse httpResponse = mockDataCiteMdsConnection.deleteDoi(MOCK_DOI);

        assertNotNull(httpResponse);
        assertNotNull(EntityUtils.toString(httpResponse.getEntity()));

    }
}