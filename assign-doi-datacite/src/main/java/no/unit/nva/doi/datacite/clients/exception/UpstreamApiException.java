package no.unit.nva.doi.datacite.clients.exception;

import nva.commons.utils.JacocoGenerated;

/**
 * Exception thrown where upstream API response is non successful.
 */
@JacocoGenerated
public class UpstreamApiException extends ClientException {

    protected static final String ERROR_MESSAGE_FORMAT = "%s (%s)";

    private final int statusCode;

    public UpstreamApiException(int statusCode, String message) {
        super(message);
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
