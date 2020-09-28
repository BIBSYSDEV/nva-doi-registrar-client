package no.unit.nva.datacite;

import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;

import static nva.commons.utils.JsonUtils.objectMapper;

public class DataCiteMdsConnection {

    public static final String HTTPS = "https";
    public static final String DATACITE_PATH_DOI = "doi";
    public static final String DATACITE_PATH_METADATA = "metadata";
    public static final String FORM_PARAM_DOI = "doi";
    public static final String FORM_PARAM_URL = "url";

    public static final String CHARACTER_SLASH = "/";

    private final transient HttpClient httpClient;
    private transient String host;
    private transient String user;
    private transient String password;

    /**
     * Constructor for testability reasons.
     *
     * @param httpClient HttpClient
     */
    public DataCiteMdsConnection(HttpClient httpClient, String host) {
        this.httpClient = httpClient;
        this.host = host;
    }

    /**
     *  Initialize DataCiteMdsConnection.
     */
    public DataCiteMdsConnection() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     *  Configures DataCiteMdsConnection for provider.
     *
     * @param host DataCite MDS API host
     * @param user username
     * @param password password
     */
    public void configure(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    /**
     * This request stores a new version of metadata.
     *
     * @param doi      prefix/suffix
     * @param dataciteXml resource metadata as Datacite XML
     * @return HttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> postMetadata(String doi, String dataciteXml) throws IOException,
            URISyntaxException, InterruptedException {

        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_METADATA + CHARACTER_SLASH + doi)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(dataciteXml))
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, basicAuth(user, password))
                .header(HttpHeaders.CONTENT_TYPE, "application/xml; charset=UTF-8")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    }


    /**
     * This request requests the most recent version of metadata associated with a given DOI.
     *
     * @param doi prefix/suffix
     * @return CloseableHttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> getMetadata(String doi) throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_METADATA + CHARACTER_SLASH + doi)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, basicAuth(user, password))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * This request marks a dataset as inactive. To activate it again, add new metadata.
     *
     * @param doi prefix/suffix
     * @return HttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> deleteMetadata(String doi) throws IOException, URISyntaxException,
            InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_METADATA + CHARACTER_SLASH + doi)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, basicAuth(user, password))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    }

    /**
     * This requests the URL associated with a given DOI.
     *
     * @param doi prefix/suffix
     * @return HttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> getDoi(String doi) throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_DOI + CHARACTER_SLASH + doi)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, basicAuth(user, password))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Deletes a DOI if DOI is in draft status.
     *
     * @param doi prefix/suffix
     * @return HttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> deleteDoi(String doi) throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_DOI + CHARACTER_SLASH + doi)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, basicAuth(user, password))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }


    /**
     * Will register a new DOI if the specified DOI doesn’t exist. This method will attempt to update the
     * URL if you specify an existing DOI.
     *
     * @param doi prefix/suffix
     * @param url landing page url
     * @return HttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     * @throws InterruptedException InterruptedException
     */
    public HttpResponse<String> postDoi(String doi, String url) throws IOException, URISyntaxException,
            InterruptedException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_DOI)
                .build();

        HashMap<String, String> formParams = new HashMap<>();
        formParams.put(FORM_PARAM_DOI, doi);
        formParams.put(FORM_PARAM_URL, url);

        String requestBody = objectMapper.writeValueAsString(formParams);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, basicAuth(user, password))
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

}
