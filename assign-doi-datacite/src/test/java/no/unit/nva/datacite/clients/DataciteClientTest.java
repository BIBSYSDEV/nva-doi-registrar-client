package no.unit.nva.datacite.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.Socket;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import no.unit.nva.datacite.clients.exception.ClientException;
import no.unit.nva.datacite.clients.models.Doi;
import no.unit.nva.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.datacite.config.DataciteConfigurationFactoryForDataciteClientTest;
import no.unit.nva.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.datacite.mdsclient.DataciteMdsConnectionFactory;
import no.unit.nva.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;
import nva.commons.utils.IoUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataciteClientTest {

    public static final String EXAMPLE_CUSTOMER_ID = "https://example.net/customer/id/4512";
    public static final String BASEPATH_MDS_API = "/datacite/mds/api";
    public static final String DEMO_PREFIX = "10.5072";
    public static final String INSTITUTION_PREFIX = DEMO_PREFIX;
    public static final char FORWARD_SLASH = '/';
    public static final String updateMetadataPath =
        FORWARD_SLASH + DataCiteMdsConnection.DATACITE_PATH_METADATA + FORWARD_SLASH + INSTITUTION_PREFIX;
    public static final String EXAMPLE_MDS_USERNAME = "exampleUserName";
    public static final String EXAMPLE_MDS_PASSWORD = "examplePassword";
    public String mdsHost;
    public DataCiteMdsClientSecretConfig validSecretConfig;
    private int mdsPort;
    private DataCiteMdsClientConfig validConfig;

    private SecretCache secretCache;
    private DataciteConfigurationFactory configurationFactory;
    private PasswordAuthenticationFactory authenticationFactory;
    private DataciteClient sut;
    private DataciteMdsConnectionFactory mdsConnectionFactory;
    private WireMockServer wireMockServer;
    private int port;
    private String postMetadataResponse;

    void startProxyToWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicHttpsPort());
        wireMockServer.start();
        port = wireMockServer.httpsPort();

        mdsPort = port;
        mdsHost = "localhost";
        validSecretConfig = new DataCiteMdsClientSecretConfig(EXAMPLE_CUSTOMER_ID,
            INSTITUTION_PREFIX, mdsHost, EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);
        validConfig = validSecretConfig;
    }

    @AfterEach
    void stopProxyToWireMock() {
        wireMockServer.stop();
        wireMockServer = null;
    }

    @BeforeEach
    void setUp() {
        startProxyToWireMock();
        postMetadataResponse = IoUtils.stringFromResources(Path.of("dataciteMdsPostMetadataResponse.txt"));

        // All unauthenticated request will be responded from the server to ask the client to authenticate itself.
        stubFor(post(urlEqualTo(updateMetadataPath))
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_UNAUTHORIZED)
                .withHeader("WWW-Authenticate", "Basic realm=\"" + mdsHost + "\"")));
        stubFor(post(urlEqualTo(updateMetadataPath))
            .withBasicAuth(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD)
            .willReturn(aResponse()
                .withStatus(HttpStatus.SC_CREATED)
                .withBody(postMetadataResponse)));

        /*
        HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(dataciteXml))
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, "application/xml; charset=UTF-8")
            .build();*/

        configurationFactory = new DataciteConfigurationFactoryForDataciteClientTest(
            Map.of(EXAMPLE_CUSTOMER_ID, validSecretConfig));
        authenticationFactory = new PasswordAuthenticationFactory(configurationFactory);

        var httpClientBuilder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMinutes(1))
            .sslContext(createInsecureSslContextTrustingEverything());

        mdsConnectionFactory = new DataciteMdsConnectionFactory(httpClientBuilder, authenticationFactory,
            mdsHost, mdsPort);
        sut = new DataciteClient(configurationFactory, mdsConnectionFactory);
    }

    @Test
    void testCreateDoiWithPrefixReturnsDoiIdentifier() throws ClientException {
        Doi actual = sut.createDoi(EXAMPLE_CUSTOMER_ID, getValidMetadataPayload());
        assertThat(actual, is(instanceOf(Doi.class)));
        assertThat(actual.prefix(), is(equalTo("prefix")));
        assertThat(actual.suffix(), is(equalTo("suffix")));

        verify(postRequestedFor(urlEqualTo(updateMetadataPath))
            .withBasicAuth(getExpectedAuthenticatedCredentials())
            .withRequestBody(WireMock.equalTo(getValidMetadataPayload()))
            .withHeader("Content-Type", WireMock.equalTo("application/xml; charset=UTF-8")));
    }

    @Test
    void updateMetadata() {

    }

    @Test
    void setLandingPage() {
    }

    @Test
    void deleteMetadata() {
    }

    @Test
    void deleteDraftDoi() {
    }

    private BasicCredentials getExpectedAuthenticatedCredentials() {
        return new BasicCredentials(EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);
    }

    private SSLContext createInsecureSslContextTrustingEverything() {
        try {
            var insecureSslContext = SSLContext.getInstance("SSL");
            insecureSslContext.init(null, new X509ExtendedTrustManager[]{createTrustEverythingManager()},
                new java.security.SecureRandom());
            return insecureSslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            Assertions.fail(
                "Failed to configure the trust everything rule for the http client, which is required to connect to "
                    + "wiremock server and local signed SSL certificate.");
            return null;
        }
    }

    private X509ExtendedTrustManager createTrustEverythingManager() {
        return new X509ExtendedTrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket)
                throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket)
                throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine)
                throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine)
                throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] x509Certificates, final String authType)
                throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
                throws CertificateException {
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