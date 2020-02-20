package no.unit.nva.datacite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import no.unit.nva.datacite.model.generated.Resource;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DataCiteMdsConnection {

    private static final String HTTPS = "https";
    private static final String DATACITE_PATH_DOI = "doi";
    private static final String DATACITE_PATH_METADATA = "metadata";
    private static final String CHARACTER_SLASH = "/";
    private static final String FORM_PARAM_DOI = "doi";
    private static final String FORM_PARAM_URL = "url";

    private transient CloseableHttpClient httpClient;
    private transient String host;

    /**
     * Constructor for testability reasons.
     *
     * @param httpClient HttpClient
     */
    public DataCiteMdsConnection(CloseableHttpClient httpClient, String host) {
        this.httpClient = httpClient;
        this.host = host;
    }

    /**
     *  Initialize DataCiteMdsConnection for provider.
     *
     * @param host DataCite MDS API host
     * @param user username
     * @param password password
     */
    public DataCiteMdsConnection(String host, String user, String password) {
        this.host = host;
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(user, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
    }

    /**
     *  Reconfigures DataCiteMdsConnection for another provider.
     *
     * @param host DataCite MDS API host
     * @param user username
     * @param password password
     */
    public void configure(String host, String user, String password) {
        this.host = host;
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(user, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
    }

    /**
     * This request stores a new version of metadata.
     *
     * @param doi      perfix/suffix
     * @param resource resource metadata
     * @return CloseableHttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public CloseableHttpResponse postMetadata(String doi, Resource resource) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPathSegments(DATACITE_PATH_METADATA, doi)
                .build();

        HttpPost httpPost = new HttpPost(uri);

        httpPost.addHeader("Content-Type", "application/xml; charset=UTF-8");
        String xml = new XmlMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .writeValueAsString(resource);
        httpPost.setEntity(new StringEntity(xml));

        return httpClient.execute(httpPost);

    }


    /**
     * This request requests the most recent version of metadata associated with a given DOI.
     *
     * @param doi perfix/suffix
     * @return CloseableHttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public CloseableHttpResponse getMetadata(String doi) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_METADATA + "/" + doi)
                .build();

        HttpGet httpGet = new HttpGet(uri);

        return httpClient.execute(httpGet);
    }

    /**
     * This request marks a dataset as inactive. To activate it again, add new metadata.
     *
     * @param doi perfix/suffix
     * @return CloseableHttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public CloseableHttpResponse deleteMetadata(String doi) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_METADATA + "/" + doi)
                .build();

        HttpDelete httpDelete = new HttpDelete(uri);

        return httpClient.execute(httpDelete);
    }

    /**
     * This requests the URL associated with a given DOI.
     *
     * @param doi perfix/suffix
     * @return CloseableHttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public CloseableHttpResponse getDoi(String doi) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_DOI + CHARACTER_SLASH + doi)
                .build();

        HttpGet httpGet = new HttpGet(uri);

        return httpClient.execute(httpGet);
    }

    /**
     * Deletes a DOI if DOI is in draft status.
     *
     * @param doi perfix/suffix
     * @return CloseableHttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public CloseableHttpResponse deleteDoi(String doi) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_DOI + CHARACTER_SLASH + doi)
                .build();

        HttpDelete httpDelete = new HttpDelete(uri);

        return httpClient.execute(httpDelete);
    }


    /**
     * Will register a new DOI if the specified DOI doesn’t exist. This method will attempt to update the
     * URL if you specify an existing DOI.
     *
     * @param doi perfix/suffix
     * @param url landing page url
     * @return CloseableHttpResponse
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public CloseableHttpResponse postDoi(String doi, String url) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(HTTPS)
                .setHost(host)
                .setPath(DATACITE_PATH_DOI)
                .build();

        HttpPost httpPost = new HttpPost(uri);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(FORM_PARAM_DOI, doi));
        params.add(new BasicNameValuePair(FORM_PARAM_URL, url));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        return httpClient.execute(httpPost);
    }

}
