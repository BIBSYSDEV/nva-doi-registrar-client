package no.unit.nva.doi.datacite.mdsclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Objects;
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.URIBuilder;

/**
 * DataCiteMdsConnect instance for handling the HTTP communication with DataCite MDS API.
 *
 * <p>The HttpClient provided should have a {@link java.net.Authenticator} associated to do pre-emptive
 * authentication towards the API server.
 *
 * <p>Use the {@link DataCiteMdsConnectionFactory#getAuthenticatedConnection(String)} to construct new instances.
 */
public class DataCiteMdsConnection {

    public static final String HTTPS = "https";
    public static final String DATACITE_PATH_DOI = "doi";
    public static final String DATACITE_PATH_METADATA = "metadata";

    public static final String CHARACTER_SLASH = "/";
    public static final String APPLICATION_XML_CHARSET_UTF_8 = "application/xml; charset=UTF-8";
    public static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain;charset=UTF-8";
    public static final String LANDING_PAGE_BODY_FORMAT = "doi=%s\nurl=%s";
    public static final String MISSING_DATACITE_XML_ARGUMENT =
        "Please specify DataCite XML encoded with UTF-8 as a string";
    public static final String MISSING_DOI_IDENTIFIER_ARGUMENT = "Please specify doi with 'prefix/suffix'";
    public static final String MISSING_LANDING_PAGE_ARGUMENT = "Please specify landing page URI as string";

    private final transient HttpClient httpClient;
    private final String host;
    private final int port;

    /**
     * Constructor for testability reasons.
     *
     * @param httpClient HttpClient
     */
    public DataCiteMdsConnection(HttpClient httpClient, String host, int port) {
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
    }

    /**
     * This request stores a new version of metadata.
     *
     * @param doi         prefix/suffix
     * @param dataCiteXml resource metadata as DataCite XML, encoded with UTF-8.
     * @return HttpResponse
     * @throws IOException          IOException
     * @throws URISyntaxException   URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> postMetadata(String doi, String dataCiteXml) throws IOException,
                                                                                    URISyntaxException,
                                                                                    InterruptedException {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        Objects.requireNonNull(dataCiteXml, MISSING_DATACITE_XML_ARGUMENT);
        URI uri = createApiEndpointBase()
            .setPath(createPathMetadataWithDoiIdentifier(doi))
            .build();

        HttpRequest request = postApplicationXmlWithBody(uri, dataCiteXml);

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * This request requests the most recent version of metadata associated with a given DOI.
     *
     * @param doi prefix/suffix
     * @return CloseableHttpResponse
     * @throws IOException          IOException
     * @throws URISyntaxException   URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> getMetadata(String doi) throws IOException, URISyntaxException, InterruptedException {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        URI uri = createApiEndpointBase()
            .setPath(createPathMetadataWithDoiIdentifier(doi))
            .build();

        HttpRequest request = getRequest(uri)
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * This request marks a dataset as inactive. To activate it again, add new metadata.
     *
     * @param doi prefix/suffix
     * @return HttpResponse
     * @throws IOException          IOException
     * @throws URISyntaxException   URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> deleteMetadata(String doi) throws IOException, URISyntaxException,
                                                                  InterruptedException {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        URI uri = createApiEndpointBase()
            .setPath(createPathMetadataWithDoiIdentifier(doi))
            .build();

        HttpRequest request = deleteRequest(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * This requests the URL associated with a given DOI.
     *
     * @param doi prefix/suffix
     * @return HttpResponse
     * @throws IOException          IOException
     * @throws URISyntaxException   URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> getDoi(String doi) throws IOException, URISyntaxException, InterruptedException {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        URI uri = createApiEndpointBase()
            .setPath(createPathDoiWithDoiIdentifier(doi))
            .build();

        HttpRequest request = getRequest(uri).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String createPathDoiWithDoiIdentifier(String doi) {
        return DATACITE_PATH_DOI + CHARACTER_SLASH + doi;
    }

    /**
     * Deletes a DOI if DOI is in draft status.
     *
     * @param doi prefix/suffix
     * @return HttpResponse
     * @throws IOException          IOException
     * @throws URISyntaxException   URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> deleteDoi(String doi) throws IOException, URISyntaxException, InterruptedException {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        URI uri = createApiEndpointBase()
            .setPath(createPathDoiWithDoiIdentifier(doi))
            .build();

        HttpRequest request = deleteRequest(uri)
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Will register a new DOI if the specified DOI doesn’t exist. This method will attempt to update the URL if you
     * specify an existing DOI.
     *
     * @param doi         prefix/suffix
     * @param landingPage landing page landingPage
     * @return HttpResponse
     * @throws IOException          IOException
     * @throws URISyntaxException   URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> registerUrl(String doi, String landingPage) throws IOException, URISyntaxException,
                                                                                   InterruptedException {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        Objects.requireNonNull(landingPage, MISSING_LANDING_PAGE_ARGUMENT);

        URI uri = createApiEndpointBase()
            .setPath(createPathDoiWithDoiIdentifier(doi))
            .build();

        String requestBody = createRequestBodyForRegisterUrl(doi, landingPage);

        HttpRequest request = putLandingPage(uri, requestBody).build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    private String createPathMetadataWithDoiIdentifier(String doi) {
        return DATACITE_PATH_METADATA + CHARACTER_SLASH + doi;
    }

    private Builder getRequest(URI uri) {
        return HttpRequest.newBuilder()
            .GET()
            .uri(uri);
    }

    private Builder deleteRequest(URI uri) {
        return HttpRequest.newBuilder()
            .DELETE()
            .uri(uri);
    }

    private Builder putLandingPage(URI uri, String requestBody) {
        return HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, DataCiteMdsConnection.TEXT_PLAIN_CHARSET_UTF_8);
    }

    private Builder postApplicationXml(URI uri) {
        return HttpRequest.newBuilder()
            .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML_CHARSET_UTF_8)
            .uri(uri);
    }

    private HttpRequest postApplicationXmlWithBody(URI uri, String dataciteXml) {
        return postApplicationXml(uri)
            .POST(HttpRequest.BodyPublishers.ofString(dataciteXml))
            .build();
    }

    private String createRequestBodyForRegisterUrl(String doi, String landingPage) throws JsonProcessingException {
        return String.format(LANDING_PAGE_BODY_FORMAT, doi, landingPage);
    }

    private URIBuilder createApiEndpointBase() {
        return new URIBuilder()
            .setScheme(HTTPS)
            .setHost(host)
            .setPort(port);
    }
}
