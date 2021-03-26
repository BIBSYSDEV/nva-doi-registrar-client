package no.unit.nva.doi.datacite.clients.exception;

import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class CreateDoiException extends UpstreamApiException {

    private final String doiPrefix;
    public static final String CREATE_DOI_ERROR_MESSAGE = "Drafting doi failed: Doi prefix:%s, "
        + "Status code: %s, "
        + "Details: %s";

    public CreateDoiException(String doiPrefix, int statusCode, String errorMessage) {
        super(statusCode, formatMessage(doiPrefix, statusCode, errorMessage));
        this.doiPrefix = doiPrefix;
    }

    private static String formatMessage(String doiPrefix, int statusCode, String errorMessage) {
        return String.format(CREATE_DOI_ERROR_MESSAGE, doiPrefix, statusCode, errorMessage);
    }

    public String getDoiPrefix() {
        return doiPrefix;
    }
}
