package no.unit.nva.datacite.mdsclient;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import no.unit.nva.datacite.config.PasswordAuthenticationFactory;

/**
 * Datacite MDS API Connection factory
 *
 * <p>Configures a {@link DataCiteMdsConnection} with associated {@link Authenticator} to communicate with Datacite
 * MDS API.
 *
 * <p>Our {@link PasswordAuthentication} will only provide credentials for valid endpoints for the {@link HttpClient}
 * during server challenge (pre-emptive authentication).
 *
 * <p>Use {@link #getAuthenticatedConnection(String)} to construct a new authenticated API connection.
 *
 * @see #createNvaCustomerAuthenticator(String)
 */
public class DataciteMdsConnectionFactory {

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
    public DataciteMdsConnectionFactory(PasswordAuthenticationFactory authenticationFactory,
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
    public DataciteMdsConnectionFactory(HttpClient.Builder httpBuilder,
                                        PasswordAuthenticationFactory authenticationFactory,
                                        String mdsHostname,
                                        int mdsPort) {
        this.authenticationFactory = authenticationFactory;
        this.mdsHostname = mdsHostname;
        this.mdsPort = mdsPort;
        this.httpBuilder = httpBuilder;
    }

    public DataCiteMdsConnection getAuthenticatedConnection(String customerId) {
        return new DataCiteMdsConnection(httpBuilder
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(2))
            .authenticator(createNvaCustomerAuthenticator(customerId))
            .build(), mdsHostname, mdsPort);
    }

    private Authenticator createNvaCustomerAuthenticator(String customerId) {
        return new Authenticator() {

            @Override
            public PasswordAuthentication requestPasswordAuthenticationInstance(String host, InetAddress addr,
                                                                                int port, String protocol,
                                                                                String prompt, String scheme,
                                                                                URL url, RequestorType reqType) {
                if (host.equalsIgnoreCase(mdsHostname) && port == mdsPort) {
                    return super.requestPasswordAuthenticationInstance(host, addr, port, protocol, prompt, scheme,
                        url,
                        reqType);
                }
                return DO_NOT_SEND_CREDENTIALS;
            }

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authenticationFactory
                    .getCredentials(customerId)
                    .orElseThrow(NoCredentialsForCustomerException::new);
            }
        };
    }
}
