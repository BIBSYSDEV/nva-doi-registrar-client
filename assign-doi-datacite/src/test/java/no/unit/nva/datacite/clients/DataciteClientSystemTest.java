package no.unit.nva.datacite.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.unit.nva.datacite.mdsclient.DataCiteMdsConnection.APPLICATION_XML_CHARSET_UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
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
import no.unit.nva.datacite.clients.exception.ClientException;
import no.unit.nva.datacite.clients.models.Doi;
import no.unit.nva.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.datacite.config.DataciteConfigurationFactoryForSystemTests;
import no.unit.nva.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.datacite.mdsclient.DataciteMdsConnectionFactory;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataciteClientSystemTest extends DataciteClientTestBase {

    private static final String EXAMPLE_CUSTOMER_ID = "https://example.net/customer/id/4512";
    private static final char FORWARD_SLASH = '/';
    private static final String metadataPathPrefix =
        FORWARD_SLASH + DataCiteMdsConnection.DATACITE_PATH_METADATA;
    private static final URI EXAMPLE_LANDING_PAGE = URI.create("https://example.net/nva/publication/203124124");
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserName";
    private static final String EXAMPLE_MDS_PASSWORD = "examplePassword";
    private static final String HTTP_RESPONSE_OK = "OK";
    private final String doiPath = FORWARD_SLASH + DataCiteMdsConnection.DATACITE_PATH_DOI;

    private String mdsHost;
    private DataCiteMdsClientSecretConfig validSecretConfig;
    private int mdsPort;
    private DataciteConfigurationFactory configurationFactory;
    private PasswordAuthenticationFactory authenticationFactory;
    private DataciteClient sut;
    private DataciteMdsConnectionFactory mdsConnectionFactory;
    private WireMockServer wireMockServer;

    void startProxyToWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicHttpsPort());
        wireMockServer.start();

        mdsPort = wireMockServer.httpsPort();
        mdsHost = "localhost";
        validSecretConfig = new DataCiteMdsClientSecretConfig(EXAMPLE_CUSTOMER_ID,
            INSTITUTION_PREFIX, mdsHost, EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);
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

        configurationFactory = new DataciteConfigurationFactoryForSystemTests(
            Map.of(EXAMPLE_CUSTOMER_ID, validSecretConfig));
        authenticationFactory = new PasswordAuthenticationFactory(configurationFactory);

        var httpClientBuilder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMinutes(1))
            .sslContext(createInsecureSslContextTrustingEverything());

        mdsConnectionFactory = new DataciteMdsConnectionFactory(httpClientBuilder,
            authenticationFactory,
            mdsHost,
            mdsPort);
        sut = new DataciteClient(configurationFactory, mdsConnectionFactory);
    }

    @Test
    void createDoiWithPrefixAndMetadataForCustomerReturnsDoiOnSuccess() throws ClientException {
        var expectedCreatedServerDoi = createDoi(DEMO_PREFIX, UUID.randomUUID().toString());
        stubCreateDoiResponse(expectedCreatedServerDoi);

        Doi actual = sut.createDoi(EXAMPLE_CUSTOMER_ID, getValidMetadataPayload());
        assertThat(actual, is(instanceOf(Doi.class)));
        assertThat(actual.prefix(), is(equalTo(expectedCreatedServerDoi.prefix())));
        assertThat(actual.suffix(), is(equalTo(expectedCreatedServerDoi.suffix())));

        verifyCreateDoiResponse(actual);
    }

    @Test
    void updateMetadataForCustomerSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForUpdatingMetadata = metadataPathPrefix + FORWARD_SLASH + doi.toIdentifier();
        stubUpdateMetadataResponse(expectedPathForUpdatingMetadata);

        sut.updateMetadata(EXAMPLE_CUSTOMER_ID, doi, getValidMetadataPayload());

        verifyUpdateMetadataResponse(expectedPathForUpdatingMetadata);
    }

    @Test
    void setLandingPageForCustomerSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();

        stubSetLandingPageResponse();

        sut.setLandingPage(EXAMPLE_CUSTOMER_ID, doi, EXAMPLE_LANDING_PAGE);

        verifySetLandingResponse(doi);
    }

    @Test
    void deleteMetadataForCustomerDoiSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingMetadata = metadataPathPrefix + FORWARD_SLASH + doi.toIdentifier();
        stubDeleteMetadataResponse(expectedPathForDeletingMetadata);

        sut.deleteMetadata(EXAMPLE_CUSTOMER_ID, doi);

        verifyDeleteMetadataResponse(expectedPathForDeletingMetadata);
    }

    @Test
    void deleteDraftDoiForCustomerWhereDoiIsDraftStateSuccessfully() throws ClientException {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingDoiInDraftStatus = doiPath + FORWARD_SLASH + doi.toIdentifier();
        stubDeleteDraftApiResponse(expectedPathForDeletingDoiInDraftStatus);

        sut.deleteDraftDoi(EXAMPLE_CUSTOMER_ID, doi);

        verifyDeleteDoiResponse(expectedPathForDeletingDoiInDraftStatus);
    }

    @Test
    void deleteDraftDoiForCustomerWhereDoiIsFindableThrowsApiExceptionAsClientException() {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingDoiInDraftStatus = doiPath + FORWARD_SLASH + doi.toIdentifier();
        stubDeleteDraftApiResponse(expectedPathForDeletingDoiInDraftStatus, DoiStateStatus.FINDABLE);

        assertThrows(ClientException.class, () -> sut.deleteDraftDoi(EXAMPLE_CUSTOMER_ID, doi));
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
        verify(putRequestedFor(urlEqualTo(doiPath))
            .withBasicAuth(getExpectedAuthenticatedCredentials())
            .withHeader(HttpHeaders.CONTENT_TYPE,
                WireMock.equalTo(ContentType.APPLICATION_FORM_URLENCODED.getMimeType()))

            .withRequestBody(matchingJsonPath("$.[?(@.url == '" + EXAMPLE_LANDING_PAGE + "')]"))
            .withRequestBody(matchingJsonPath("$.[?(@.doi == '" + requestedDoi.toIdentifier() + "')]"))
            .withHeader("Content-Type", WireMock.equalTo(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())));
    }

    private void stubSetLandingPageResponse() {
        stubFor(put(urlEqualTo(doiPath))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
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
            // TODO: Validate that trying to delete a DOI that is not in draft state, gives us a 403 Forbidden.
            stubFor(delete(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
                .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_FORBIDDEN)));
        }
    }

    private void verifyUpdateMetadataResponse(String expectedPath) {
        verify(postRequestedFor(urlEqualTo(expectedPath))
            .withBasicAuth(getExpectedAuthenticatedCredentials())
            .withRequestBody(WireMock.equalTo(getValidMetadataPayload()))
            .withHeader("Content-Type", WireMock.equalTo(APPLICATION_XML_CHARSET_UTF_8)));
    }

    private void stubUpdateMetadataResponse(String expectedPathForUpdatingMetadata) {
        stubFor(post(urlEqualTo(expectedPathForUpdatingMetadata))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_OK)
                .withBody(HTTP_RESPONSE_OK)));
    }

    private void verifyCreateDoiResponse(Doi actual) {
        verifyUpdateMetadataResponse(metadataPathPrefix + FORWARD_SLASH + actual.prefix());
    }

    private void stubCreateDoiResponse(Doi expectedCreatedServerDoi) {
        stubFor(post(urlEqualTo(metadataPathPrefix + FORWARD_SLASH + DEMO_PREFIX))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_CREATED)
                .withBody(successfullyCreateMetadataResponse(expectedCreatedServerDoi))));
    }

    private void stubRequireAuthenticationForAllApiCalls() {
        // All unauthenticated request will be responded from the server to ask the client to authenticate itself.
        stubFor(any(WireMock.anyUrl())
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_UNAUTHORIZED)
                .withHeader("WWW-Authenticate", "Basic realm=\"" + mdsHost + "\"")));
    }

    private String successfullyCreateMetadataResponse(Doi newDoi) {
        return String.format("OK (%s)", newDoi.toIdentifier());
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
            Assertions.fail(
                "Failed to configure the trust everything rule for the http client, which is required to connect to "
                    + "wiremock server and local signed SSL certificate for now.");
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