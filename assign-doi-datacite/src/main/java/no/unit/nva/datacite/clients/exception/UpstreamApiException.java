package no.unit.nva.datacite.clients.exception;

import nva.commons.utils.JacocoGenerated;

/**
 * Exception thrown where upstream API response is non successful.
 */
@JacocoGenerated
public class UpstreamApiException extends ClientException {

    private final int statusCode;

    public UpstreamApiException(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public UpstreamApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public UpstreamApiException(int statusCode, Exception e) {
        super(e);
        this.statusCode = statusCode;
    }

    public UpstreamApiException(int statusCode, String message, Exception e) {
        super(message, e);
        this.statusCode = statusCode;
    }

    /**
     * Retrieve http status code from upstream API.
     *
     * @return http status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}
