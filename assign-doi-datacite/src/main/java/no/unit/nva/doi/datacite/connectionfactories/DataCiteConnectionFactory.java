package no.unit.nva.doi.datacite.connectionfactories;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.doi.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import no.unit.nva.doi.datacite.restclient.DataCiteRestConnection;

/**
 * DataCite MDS API Connection factory
 *
 * <p>Configures a {@link DataCiteMdsConnection} with associated {@link Authenticator} to communicate with DataCite
 * MDS API.
 *
 * <p>Our {@link PasswordAuthentication} will only provide credentials for valid endpoints for the {@link HttpClient}
 * during server challenge (pre-emptive authentication).
 *
 * <p>Use {@link #getAuthenticatedMdsConnection(URI)}}
 * or {@link #getAuthenticatedRestConnection(URI)} to construct a new authenticated API connection.
 *
 * @see #createNvaCustomerAuthenticator(URI)
 */
public class DataCiteConnectionFactory {

    public static final PasswordAuthentication DO_NOT_SEND_CREDENTIALS = null;
    private final PasswordAuthenticationFactory authenticationFactory;
    private final String mdsApiHostName;
    private final String restApiHostName;
    private final int apiPort;
    private final Builder httpBuilder;
    private final DataCiteConfigurationFactory configurationFactory;

    /**
     * Creates a dataciteConnectionFactory.
     *
     * @param configurationFactory DataCiteConfiguration Factory
     * @param mdsApiHostName             the MDS API hostname
     * @param restApiHostName             the REST API hostname
     * @param apiPort              the API port
     */
    public DataCiteConnectionFactory(DataCiteConfigurationFactory configurationFactory,
                                     String mdsApiHostName,
                                     String restApiHostName,
                                     int apiPort) {
        this(HttpClient.newBuilder(),
            configurationFactory,
                mdsApiHostName, restApiHostName, apiPort);
    }

    /**
     * Constructor for testing.
     *
     * @param httpBuilder HttpClient to override security configuration
     * @param mdsApiHostName             the MDS API hostname
     * @param restApiHostName             the REST API hostname
     * @param apiPort     MDS API port
     */
    public DataCiteConnectionFactory(HttpClient.Builder httpBuilder,
                                     DataCiteConfigurationFactory configurationFactory,
                                     String mdsApiHostName,
                                     String restApiHostName,
                                     int apiPort) {
        this.authenticationFactory = new PasswordAuthenticationFactory(configurationFactory);
        this.mdsApiHostName = mdsApiHostName;
        this.restApiHostName = restApiHostName;
        this.apiPort = apiPort;
        this.httpBuilder = httpBuilder;
        this.configurationFactory = configurationFactory;
    }

    /**
     * Get a authenteicated connection towards DataCite MDS API.
     *
     * @param customerId NVA customer id
     * @return DataCiteMdsConnection private connection for provided customerId
     * @throws NoCredentialsForCustomerRuntimeException if customer has no credentials configured.
     */
    public DataCiteMdsConnection getAuthenticatedMdsConnection(URI customerId) {
        HttpClient httpClient = getAuthenticatedHttpClientForDatacite(customerId);
        return new DataCiteMdsConnection(httpClient, mdsApiHostName, apiPort);
    }

    public DataCiteRestConnection getAuthenticatedRestConnection(URI customerId) {
        HttpClient httpClient = getAuthenticatedHttpClientForDatacite(customerId);
        DataCiteMdsClientSecretConfig clientConfigWithCredentials = configurationFactory.getCredentials(customerId);
        return new DataCiteRestConnection(httpClient, restApiHostName, apiPort, clientConfigWithCredentials);
    }

    public HttpClient getAuthenticatedHttpClientForDatacite(URI customerId) {
        Authenticator nvaCustomerAuthenticator = createNvaCustomerAuthenticator(customerId);
        return createHttpClientWithAuthenticator(nvaCustomerAuthenticator);
    }

    private HttpClient createHttpClientWithAuthenticator(Authenticator nvaCustomerAuthenticator) {
        return httpBuilder
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(2))
            .authenticator(nvaCustomerAuthenticator)
            .build();
    }

    private Authenticator createNvaCustomerAuthenticator(URI customerId) {
        return new Authenticator() {

            @Override
            public PasswordAuthentication requestPasswordAuthenticationInstance(String host,
                                                                                InetAddress addr,
                                                                                int port,
                                                                                String protocol,
                                                                                String prompt,
                                                                                String scheme,
                                                                                URL url,
                                                                                RequestorType reqType) {

                if (isCommunicatingTowardsConfiguredDataCiteApi(host, port)) {
                    return super.requestPasswordAuthenticationInstance(host,
                        addr,
                        port,
                        protocol,
                        prompt,
                        scheme,
                        url,
                        reqType);
                }
                return DO_NOT_SEND_CREDENTIALS;
            }

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authenticationFactory.getCredentials(customerId);
            }

            private boolean isCommunicatingTowardsConfiguredDataCiteApi(String host, int port) {
                return host.equalsIgnoreCase(mdsApiHostName) && port == apiPort;
            }
        };
    }
}
