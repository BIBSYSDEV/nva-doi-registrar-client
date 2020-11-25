package no.unit.nva.doi.datacite.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection.APPLICATION_XML_CHARSET_UTF_8;
import static no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection.LANDING_PAGE_BODY_FORMAT;
import static no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection.TEXT_PLAIN_CHARSET_UTF_8;
import static no.unit.nva.doi.datacite.restclient.DataCiteRestConnection.JSON_API_CONTENT_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.CreateDoiException;
import no.unit.nva.doi.datacite.clients.exception.DeleteDraftDoiException;
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactoryForSystemTests;
import no.unit.nva.doi.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteConnectionFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;

import no.unit.nva.doi.models.Doi;
import nva.commons.utils.Environment;
import nva.commons.utils.IoUtils;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class DataCiteClientSystemTest extends DataciteClientTestBase {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTPS_SCHEME = "https://";
    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String TEST_CONFIGURATION_TRUST_MANAGER_FAILURE =
        "Failed to configure the trust everything rule for the http client, which is required to connect to "
            + "wiremock server and local signed SSL certificate for now.";
    public static final String DOIS_PATH_PREFIX = "/dois";
    public static final String DATACITE_RESTP_API = "https://api.test.datacite.org/dois";
    private static final URI EXAMPLE_CUSTOMER_ID = URI.create("https://example.net/customer/id/4512");
    private static final char FORWARD_SLASH = '/';
    private static final String metadataPathPrefix =
        FORWARD_SLASH + DataCiteMdsConnection.DATACITE_PATH_METADATA;
    private static final URI EXAMPLE_LANDING_PAGE = URI.create("https://example.net/nva/publication/203124124");
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserName";
    private static final String EXAMPLE_MDS_PASSWORD = "examplePassword";
    private static final String HTTP_RESPONSE_OK = "OK";
    private static final char COLON = ':';
    private static final String doiPath = FORWARD_SLASH + DataCiteMdsConnection.DATACITE_PATH_DOI;
    private String mdsHost;
    private DataCiteMdsClientSecretConfig validSecretConfig;
    private int mdsPort;
    private DataCiteConfigurationFactory configurationFactory;
    private PasswordAuthenticationFactory authenticationFactory;
    private DataCiteClient doiClient;
    private DataCiteConnectionFactory mdsConnectionFactory;
    private WireMockServer wireMockServer;

    void startProxyToWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicHttpsPort());
        wireMockServer.start();

        mdsPort = wireMockServer.httpsPort();
        mdsHost = "localhost";
        var dataCiteMdsClientUrl = URI.create(HTTPS_SCHEME + mdsHost + COLON + mdsPort);
        validSecretConfig = new DataCiteMdsClientSecretConfig(EXAMPLE_CUSTOMER_ID,
            INSTITUTION_PREFIX,
            dataCiteMdsClientUrl,
            EXAMPLE_MDS_USERNAME,
            EXAMPLE_MDS_PASSWORD);
    }

    @AfterEach
    void stopProxyToWireMock() {
        wireMockServer.resetAll();
        wireMockServer.stop();
        wireMockServer = null;
    }

    @BeforeEach
    void setUp() {
        startProxyToWireMock();
        stubRequireAuthenticationForAllApiCalls();

        configurationFactory = new DataCiteConfigurationFactoryForSystemTests(
            Map.of(EXAMPLE_CUSTOMER_ID, validSecretConfig));
        authenticationFactory = new PasswordAuthenticationFactory(configurationFactory);

        var httpClientBuilder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMinutes(1))
            .sslContext(createInsecureSslContextTrustingEverything());

        mdsConnectionFactory = new DataCiteConnectionFactory(httpClientBuilder,
            authenticationFactory,
            mdsHost,
            mdsPort);
        doiClient = new DataCiteClient(configurationFactory, mdsConnectionFactory);
    }

    @Test
    @Tag("online")
    void createDoiTest() throws ClientException {
        DataCiteConfigurationFactory configFactory = mockConfigFactory();

        var passwordFactory = new PasswordAuthenticationFactory(configFactory);
        URI targetUri = URI.create(DATACITE_RESTP_API);

        var connectionFactory = new DataCiteConnectionFactory(passwordFactory, targetUri.getHost(), -1);
        var doiClient = new DataCiteClient(configFactory, connectionFactory);
        Doi doi = doiClient.createDoi(EXAMPLE_CUSTOMER_ID);
        assertThat(doi, is(not(nullValue())));
    }

    @Test
    void createDoiWithPrefixForCustomerReturnsDoiOnSuccess() throws ClientException {
        String randomSuffix = UUID.randomUUID().toString();
        DraftDoiDto draftDoiDto = DraftDoiDto.create(DEMO_PREFIX, randomSuffix);
        var expectedCreatedServerDoi = createDoi(DEMO_PREFIX, randomSuffix);

        stubCreateDoiResponse(draftDoiDto);

        Doi actual = doiClient.createDoi(EXAMPLE_CUSTOMER_ID);
        assertThat(actual, is(instanceOf(Doi.class)));
        assertThat(actual.getPrefix(), is(equalTo(expectedCreatedServerDoi.getPrefix())));
        assertThat(actual.getSuffix(), is(equalTo(expectedCreatedServerDoi.getSuffix())));

        verifyCreateDoiResponse(actual.getPrefix());
    }

    @Test
    void createDoiLogsResponseFromDataCiteWhenRequestFails() {
        TestAppender logAppender = LogUtils.getTestingAppender(DataCiteClient.class);
        String expectedResponseMessage = "ExpectedResponseMessage";
        stubCreateFailedResponse(expectedResponseMessage);
        Executable action = () -> doiClient.createDoi(EXAMPLE_CUSTOMER_ID);

        CreateDoiException exception = assertThrows(CreateDoiException.class, action);
        assertThat(exception.getMessage(), containsString(expectedResponseMessage));
        assertThat(logAppender.getMessages(), containsString(expectedResponseMessage));
    }

    @Test
    void createDoiIncludesResponseFromDataciteInExceptionWhenRequestFails() {
        TestAppender logAppender = LogUtils.getTestingAppender(DataCiteClient.class);
        String expectedResponseMessage = "ExpectedResponseMessage";
        stubCreateFailedResponse(expectedResponseMessage);
        Executable action = () -> doiClient.createDoi(EXAMPLE_CUSTOMER_ID);

        CreateDoiException exception = assertThrows(CreateDoiException.class, action);
        assertThat(exception.getMessage(), containsString(expectedResponseMessage));
        assertThat(logAppender.getMessages(), containsString(expectedResponseMessage));
    }

    @Test
    void updateMetadataForCustomerSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForUpdatingMetadata = createMetadataDoiIdentifierPath(doi);
        stubUpdateMetadataResponse(expectedPathForUpdatingMetadata);

        doiClient.updateMetadata(EXAMPLE_CUSTOMER_ID, doi, getValidMetadataPayload());

        verifyUpdateMetadataResponse(expectedPathForUpdatingMetadata);
    }

    @Test
    void setLandingPageForCustomerSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();

        stubSetLandingPageResponse(doi);

        doiClient.setLandingPage(EXAMPLE_CUSTOMER_ID, doi, EXAMPLE_LANDING_PAGE);

        verifySetLandingResponse(doi);
    }

    @Test
    void deleteMetadataForCustomerDoiSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingMetadata = createMetadataDoiIdentifierPath(doi);
        stubDeleteMetadataResponse(expectedPathForDeletingMetadata);

        doiClient.deleteMetadata(EXAMPLE_CUSTOMER_ID, doi);

        verifyDeleteMetadataResponse(expectedPathForDeletingMetadata);
    }

    @Test
    void deleteDraftDoiForCustomerWhereDoiIsDraftStateSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingDoiInDraftStatus = createDoiIdentifierPath(doi);
        stubDeleteDraftApiResponse(expectedPathForDeletingDoiInDraftStatus);

        doiClient.deleteDraftDoi(EXAMPLE_CUSTOMER_ID, doi);

        verifyDeleteDoiResponse(expectedPathForDeletingDoiInDraftStatus);
    }

    @Test
    void deleteDraftDoiForCustomerWhereDoiIsFindableThrowsApiExceptionAsClientException() {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingDoiInDraftStatus = createDoiIdentifierPath(doi);
        stubDeleteDraftApiResponse(expectedPathForDeletingDoiInDraftStatus, DoiStateStatus.FINDABLE);

        var actualException = assertThrows(DeleteDraftDoiException.class,
            () -> doiClient.deleteDraftDoi(EXAMPLE_CUSTOMER_ID, doi));
        assertThat(actualException, isA(ClientException.class));
        assertThat(actualException.getMessage(), containsString(doi.toIdentifier()));
        assertThat(actualException.getMessage(), containsString(String.valueOf(HttpStatus.SC_METHOD_NOT_ALLOWED)));
    }

    /*This method is used only for the online test*/
    private DataCiteConfigurationFactory mockConfigFactory() {

        URI dataciteApi = URI.create(DATACITE_RESTP_API);
        String testDataciteAccountUsername = "TESTTO.NVA";
        String testDataciteAccountpassword = new Environment().readEnv("TESTTO_NVA_PASSWORD");

        String unitDoiPrefix = "10.16903";

        var config = new DataCiteMdsClientSecretConfig(EXAMPLE_CUSTOMER_ID,
            unitDoiPrefix,
            dataciteApi,
            testDataciteAccountpassword,
            testDataciteAccountpassword);

        return new DataCiteConfigurationFactoryForSystemTests(Map.of(EXAMPLE_CUSTOMER_ID, config));
    }

    private String createMetadataDoiIdentifierPath(Doi doi) {
        return metadataPathPrefix + FORWARD_SLASH + doi.toIdentifier();
    }

    private void verifyDeleteMetadataResponse(String expectedPathForDeletingMetadata) {
        verify(deleteRequestedFor(urlEqualTo(expectedPathForDeletingMetadata))
            .withBasicAuth(getExpectedAuthenticatedCredentials()));
    }

    private void verifyDeleteDoiResponse(String expectedPathForDeletingDoiInDraftStatus) {
        verify(deleteRequestedFor(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
            .withBasicAuth(getExpectedAuthenticatedCredentials()));
    }

    private void stubDeleteMetadataResponse(String expectedPathForDeletingMetadata) {
        stubFor(delete(urlEqualTo(expectedPathForDeletingMetadata))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withBody(HTTP_RESPONSE_OK)));
    }

    private void verifySetLandingResponse(Doi requestedDoi) {
        verify(putRequestedFor(urlEqualTo(createDoiIdentifierPath(requestedDoi)))
            .withBasicAuth(getExpectedAuthenticatedCredentials())
            .withHeader(HttpHeaders.CONTENT_TYPE,
                WireMock.equalTo(TEXT_PLAIN_CHARSET_UTF_8))
            .withRequestBody(WireMock.equalTo(
                String.format(LANDING_PAGE_BODY_FORMAT, requestedDoi.toIdentifier(), EXAMPLE_LANDING_PAGE)))
            .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo(TEXT_PLAIN_CHARSET_UTF_8)));
    }

    private String createDoiIdentifierPath(Doi requestedDoi) {
        return doiPath + FORWARD_SLASH + requestedDoi.toIdentifier();
    }

    private void stubSetLandingPageResponse(Doi requestedDoi) {
        stubFor(put(urlEqualTo(createDoiIdentifierPath(requestedDoi)))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withHeader(HEADER_CONTENT_TYPE, TEXT_PLAIN_CHARSET_UTF_8)
                .withStatus(HttpStatus.SC_CREATED)
                .withBody(HTTP_RESPONSE_OK)));
    }

    private void stubDeleteDraftApiResponse(String expectedPathForDeletingDoiInDraftStatus) {
        stubDeleteDraftApiResponse(expectedPathForDeletingDoiInDraftStatus, DoiStateStatus.DRAFT);
    }

    private void stubDeleteDraftApiResponse(String expectedPathForDeletingDoiInDraftStatus,
                                            DoiStateStatus doiStateStatus) {
        if (doiStateStatus == DoiStateStatus.DRAFT) {
            stubFor(delete(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
                .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withBody(HTTP_RESPONSE_OK)));
        } else {
            stubFor(delete(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
                .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_METHOD_NOT_ALLOWED)));
        }
    }

    private void verifyUpdateMetadataResponse(String expectedPath) {
        verify(postRequestedFor(urlEqualTo(expectedPath))
            .withBasicAuth(getExpectedAuthenticatedCredentials())
            .withRequestBody(WireMock.equalTo(getValidMetadataPayload()))
            .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo(APPLICATION_XML_CHARSET_UTF_8)));
    }

    private void verifyCreateDoiResponse(String doiPrefix) {
        verify(postRequestedFor(urlEqualTo(DOIS_PATH_PREFIX))
            .withBasicAuth(getExpectedAuthenticatedCredentials())
            .withRequestBody(WireMock.containing(doiPrefix))
            .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo(JSON_API_CONTENT_TYPE)));
    }

    private void stubUpdateMetadataResponse(String expectedPathForUpdatingMetadata) {
        stubFor(post(urlEqualTo(expectedPathForUpdatingMetadata))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withBody(HTTP_RESPONSE_OK)));
    }

    private void stubCreateDoiResponse(DraftDoiDto expectedResponseBody) {

        stubFor(post(urlEqualTo(DOIS_PATH_PREFIX))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_CREATED)
                .withBody(expectedResponseBody.toJson())));
    }

    private void stubCreateFailedResponse(String expectedBody) {

        stubFor(post(urlEqualTo(DOIS_PATH_PREFIX))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_FORBIDDEN)
                .withBody(expectedBody)));
    }

    private void stubRequireAuthenticationForAllApiCalls() {
        // All unauthenticated request will be responded from the server to ask the client to authenticate itself.
        stubFor(any(WireMock.anyUrl())
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_UNAUTHORIZED)
                .withHeader(HEADER_WWW_AUTHENTICATE, createRealm())));
    }

    private String createRealm() {
        return "Basic realm=\"" + mdsHost + "\"";
    }

    private BasicCredentials getExpectedAuthenticatedCredentials() {
        return new BasicCredentials(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);
    }

    private SSLContext createInsecureSslContextTrustingEverything() {
        // TODO Add wiremock's generated self signed certificate to the trust store during tests.
        try {
            var insecureSslContext = SSLContext.getInstance("SSL");
            insecureSslContext.init(null, new X509ExtendedTrustManager[]{createTrustEverythingManager()},
                new java.security.SecureRandom());
            return insecureSslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assertions.fail(TEST_CONFIGURATION_TRUST_MANAGER_FAILURE);
            return null;
        }
    }

    @SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
    private X509ExtendedTrustManager createTrustEverythingManager() {

        return new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private String getValidMetadataPayload() {
        return IoUtils.stringFromResources(Path.of("dataciteXmlResourceExample.xml"));
    }
}