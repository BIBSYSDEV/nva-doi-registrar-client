package no.unit.nva.datacite.commons;

import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import nva.commons.core.useragent.UserAgent;

public class DataCiteMetadataResolver {

    private static final String ACCEPT = "Accept";
    private static final String APPLICATION_VND_DATACITE_DATACITE_XML = "application/vnd.datacite.datacite+xml";
    public static final String PUBLICATION_API_ERROR_MESSAGE = "Publication api answered with status: ";
    private final HttpClient httpClient;

    public DataCiteMetadataResolver(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @JacocoGenerated
    public DataCiteMetadataResolver() {
        this(HttpClient.newBuilder().build());
    }

    public String getDataCiteMetadataXml(URI publicationID) {
        return attempt(() -> createRequest(publicationID))
                   .map(this::getPublicationApiResponse)
                   .map(this::getBodyFromResponse)
                   .orElseThrow(this::handleFailure);
    }

    private PublicationApiClientException handleFailure(Failure<String> failure) {
        return new PublicationApiClientException(failure.getException());
    }

    private String getBodyFromResponse(HttpResponse<String> response) {
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new PublicationApiClientException(PUBLICATION_API_ERROR_MESSAGE + response.statusCode(),
                                                    response.statusCode());
        }
        return response.body();
    }

    private HttpResponse<String> getPublicationApiResponse(HttpRequest httpRequest)
        throws IOException, InterruptedException {
        return httpClient.send(httpRequest, BodyHandlers.ofString());
    }

    private HttpRequest createRequest(URI publicationID) {
        return HttpRequest.newBuilder()
                   .uri(publicationID)
                   .GET()
                   .header(ACCEPT, APPLICATION_VND_DATACITE_DATACITE_XML)
                   .build();
    }
}
