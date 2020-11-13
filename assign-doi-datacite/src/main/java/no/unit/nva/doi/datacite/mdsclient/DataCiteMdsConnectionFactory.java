package no.unit.nva.doi.datacite.mdsclient;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import no.unit.nva.doi.datacite.config.PasswordAuthenticationFactory;

/**
 * DataCite MDS API Connection factory
 *
 * <p>Configures a {@link DataCiteMdsConnection} with associated {@link Authenticator} to communicate with DataCite
 * MDS API.
 *
 * <p>Our {@link PasswordAuthentication} will only provide credentials for valid endpoints for the {@link HttpClient}
 * during server challenge (pre-emptive authentication).
 *
 * <p>Use {@link #getAuthenticatedConnection(String)} to construct a new authenticated API connection.
 *
 * @see #createNvaCustomerAuthenticator(String)
 */
public class DataCiteMdsConnectionFactory {

    public static final PasswordAuthentication DO_NOT_SEND_CREDENTIALS = null;
    private final PasswordAuthenticationFactory authenticationFactory;
    private final String mdsHostname;
    private final int mdsPort;
    private final Builder httpBuilder;

    /**
     * Default constructor.
     *
     * @param authenticationFactory Authentication factory which is used for 401 challenge responses.
     * @param mdsHostname           MDS API hostname
     * @param mdsPort               MDS API port
     */
    public DataCiteMdsConnectionFactory(PasswordAuthenticationFactory authenticationFactory,
                                        String mdsHostname,
                                        int mdsPort) {
        this(HttpClient.newBuilder(), authenticationFactory, mdsHostname, mdsPort);
    }

    /**
     * Constructor for testing.
     *
     * @param httpBuilder           HttpClient to override security configuration
     * @param authenticationFactory Authentication factory which is used for 401 challenge responses.
     * @param mdsHostname           MDS API hostname
     * @param mdsPort               MDS API port
     */
    public DataCiteMdsConnectionFactory(HttpClient.Builder httpBuilder,
                                        PasswordAuthenticationFactory authenticationFactory,
                                        String mdsHostname,
                                        int mdsPort) {
        this.authenticationFactory = authenticationFactory;
        this.mdsHostname = mdsHostname;
        this.mdsPort = mdsPort;
        this.httpBuilder = httpBuilder;
    }

    /**
     * Get a authenteicated connection towards DataCite MDS API.
     *
     * @param customerId NVA customer id
     * @return DataCiteMdsConnection private connection for provided customerId
     * @throws NoCredentialsForCustomerRuntimeException if customer has no credentials configured.
     */
    public DataCiteMdsConnection getAuthenticatedConnection(String customerId) {
        Authenticator nvaCustomerAuthenticator = createNvaCustomerAuthenticator(customerId);
        HttpClient httpClient = createHttpClientWithAuthenticator(nvaCustomerAuthenticator);
        return new DataCiteMdsConnection(httpClient, mdsHostname, mdsPort);
    }

    private HttpClient createHttpClientWithAuthenticator(Authenticator nvaCustomerAuthenticator) {
        return httpBuilder
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(2))
            .authenticator(nvaCustomerAuthenticator)
            .build();
    }

    private Authenticator createNvaCustomerAuthenticator(String customerId) {
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
                return host.equalsIgnoreCase(mdsHostname) && port == mdsPort;
            }
        };
    }
}
