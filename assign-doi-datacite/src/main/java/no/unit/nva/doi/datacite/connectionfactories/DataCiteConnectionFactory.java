package no.unit.nva.doi.datacite.connectionfactories;

import static no.unit.nva.doi.DataciteConfig.DATACITE_MDS_URI;
import static no.unit.nva.doi.DataciteConfig.DATACITE_REST_URI;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
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
 *
 */
public class DataCiteConnectionFactory {
    private final Builder httpBuilder;
    private final DataCiteConfigurationFactory configurationFactory;
    private final URI dataciteMdsHost;
    private final URI dataciteRestUri;

    /**
     * Creates a dataciteConnectionFactory.
     *
     * @param configurationFactory DataCiteConfiguration Factory
     */
    public DataCiteConnectionFactory(DataCiteConfigurationFactory configurationFactory) {
        this(HttpClient.newBuilder(), configurationFactory, DATACITE_MDS_URI, DATACITE_REST_URI);
    }

    /**
     * Constructor for testing.
     *
     * @param httpBuilder HttpClient to override security configuration
     */
    public DataCiteConnectionFactory(HttpClient.Builder httpBuilder,
                                     DataCiteConfigurationFactory configurationFactory,
                                     URI dataciteMdsHost,
                                     URI dataciteRestUri) {
        this.httpBuilder = httpBuilder;
        this.configurationFactory = configurationFactory;
        this.dataciteMdsHost = dataciteMdsHost;
        this.dataciteRestUri = dataciteRestUri;
    }

    /**
     * Get a authenteicated connection towards DataCite MDS API.
     *
     * @return DataCiteMdsConnection private connection for provided customerId
     * @throws NoCredentialsForCustomerRuntimeException if customer has no credentials configured.
     */
    public DataCiteMdsConnection getAuthenticatedMdsConnection() {
        HttpClient httpClient = getAuthenticatedHttpClientForDatacite();
        return new DataCiteMdsConnection(httpClient, dataciteMdsHost);
    }

    public DataCiteRestConnection getAuthenticatedRestConnection(URI customerId) {
        HttpClient httpClient = getAuthenticatedHttpClientForDatacite();
        DataCiteMdsClientSecretConfig clientConfigWithCredentials = configurationFactory.getCredentials(customerId);
        return new DataCiteRestConnection(dataciteRestUri, httpClient, clientConfigWithCredentials);
    }

    public HttpClient getAuthenticatedHttpClientForDatacite() {
        return createHttpClientWithAuthenticator();
    }

    private HttpClient createHttpClientWithAuthenticator() {
        return httpBuilder
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    }
}
