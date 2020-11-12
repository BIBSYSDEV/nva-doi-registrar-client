package no.unit.nva.datacite.clients.exception;

import nva.commons.utils.JacocoGenerated;

@JacocoGenerated
public class UpdateMetadataException extends UpstreamApiException {

    public UpdateMetadataException(int statusCode) {
        super(statusCode);
    }

    public UpdateMetadataException(int statusCode, String message) {
        super(statusCode, message);
    }

    public UpdateMetadataException(int statusCode, Exception e) {
        super(statusCode, e);
    }

    public UpdateMetadataException(int statusCode, String message, Exception e) {
        super(statusCode, message, e);
    }
}
