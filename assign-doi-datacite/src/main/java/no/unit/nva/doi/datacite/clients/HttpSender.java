package no.unit.nva.doi.datacite.clients;

import static nva.commons.core.attempt.Try.attempt;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Set;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import nva.commons.core.attempt.Failure;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSender {

    public static final String REQUEST_RESPONDED_WITH_RESPONSE_MESSAGE = "Request responded with: ";
    private static final Set<Integer> EXPECTED_HTTP_CODES = Set.of(HttpStatus.SC_CREATED,
                                                                   HttpStatus.SC_OK,
                                                                   HttpStatus.SC_ACCEPTED,
                                                                   HttpStatus.SC_NO_CONTENT);
    private final Logger logger = LoggerFactory.getLogger(HttpSender.class);
    private final HttpClient httpClient;

    public HttpSender(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse<String> sendRequest(HttpRequest request) throws ClientException {
        var response = attempt(() -> httpClient.send(request, BodyHandlers.ofString()))
                           .orElseThrow(this::handleFailure);
        if (isNotSuccessful(response)) {
            logger.error(REQUEST_RESPONDED_WITH_RESPONSE_MESSAGE + response.body());
            throw new ClientException(response.toString());
        }
        return response;
    }

    protected static boolean isNotSuccessful(HttpResponse<String> response) {
        return !EXPECTED_HTTP_CODES.contains(response.statusCode());
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected ClientException handleFailure(Failure<HttpResponse<String>> fail) {
        logger.error("Exception : {}", fail.getException());
        return new ClientException(fail.getException());
    }
}
