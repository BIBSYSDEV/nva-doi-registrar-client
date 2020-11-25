package no.unit.nva.doi.datacite.restclient;

import static nva.commons.utils.attempt.Try.attempt;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Base64;
import no.unit.nva.doi.datacite.clients.DraftDoiDto;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import org.apache.http.client.utils.URIBuilder;

public class DataCiteRestConnection {

    public static final String HTTPS = "https";
    public static final String DOIS_PATH = "dois";
    public static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ERROR_CONFIG_WITHOUT_SECRETS =
        "Creating a doi requires a client configuration with secrets (%s)";
    private static final String AUTHORIZATION_HEADER = "Authotization";

    private final HttpClient httpClient;
    private final String host;
    private final int port;

    /**
     * A DataCite connection for the RestApi.
     *
     * @param httpClient the httpclient to be used.
     * @param host       the host address without scheme and path
     * @param port       the port (for https 443)
     */
    public DataCiteRestConnection(HttpClient httpClient, String host, int port) {
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
    }

    /**
     * This request stores a new version of metadata.
     *
     * @return HttpResponse
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> createDoi(DataCiteMdsClientConfig config)
        throws IOException, InterruptedException {

        DraftDoiDto bodyObject = DraftDoiDto.fromPrefix(config.getCustomerDoiPrefix());
        String bodyJson = bodyObject.toJson();
        URI apiEndpointBase = createApiEndpointBase();
        String authString = createAuthenticationString(config);
        HttpRequest postRequest = HttpRequest.newBuilder()
            .uri(apiEndpointBase)
            .POST(BodyPublishers.ofString(bodyJson))
            .header(CONTENT_TYPE, JSON_API_CONTENT_TYPE)
            // TODO: remove when DataCite REST-API prompts for Authentication
            .headers(AUTHORIZATION_HEADER, authString)
            .build();
        return httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    private String createAuthenticationString(DataCiteMdsClientConfig config) {
        DataCiteMdsClientSecretConfig configWithSecrets = convertConfigToConfigWithSecrets(config);
        return basicAuth(
            configWithSecrets.getDataCiteMdsClientUsername(),
            configWithSecrets.getDataCiteMdsClientPassword()
        );
    }

    private DataCiteMdsClientSecretConfig convertConfigToConfigWithSecrets(DataCiteMdsClientConfig config) {

        if (config instanceof DataCiteMdsClientSecretConfig) {
            return (DataCiteMdsClientSecretConfig) config;
        }
        String className = DataCiteMdsClientSecretConfig.class.getName();
        throw new IllegalStateException(String.format(ERROR_CONFIG_WITHOUT_SECRETS, className));
    }

    private URI createApiEndpointBase() {
        return attempt(this::buildUri).orElseThrow();
    }

    private URI buildUri() throws URISyntaxException {
        return new URIBuilder()
            .setScheme(HTTPS)
            .setHost(host)
            .setPort(port)
            .setPath(DOIS_PATH)
            .build();
    }
}
