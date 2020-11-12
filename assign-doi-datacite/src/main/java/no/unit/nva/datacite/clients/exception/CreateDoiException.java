package no.unit.nva.datacite.clients.exception;

import nva.commons.utils.JacocoGenerated;

@JacocoGenerated
public class CreateDoiException extends UpstreamApiException {

    private final String doiPrefix;

    public CreateDoiException(String doiPrefix, int statusCode) {
        super(statusCode, String.format(ERROR_MESSAGE_FORMAT, doiPrefix, statusCode));
        this.doiPrefix = doiPrefix;
    }

    public String getDoiPrefix() {
        return doiPrefix;
    }
}
