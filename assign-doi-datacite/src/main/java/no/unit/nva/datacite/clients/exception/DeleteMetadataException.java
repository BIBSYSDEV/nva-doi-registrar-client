package no.unit.nva.datacite.clients.exception;

import nva.commons.utils.JacocoGenerated;

@JacocoGenerated
public class DeleteMetadataException extends UpstreamApiException {

    public DeleteMetadataException(int statusCode) {
        super(statusCode);
    }

    public DeleteMetadataException(int statusCode, String message) {
        super(statusCode, message);
    }

    public DeleteMetadataException(int statusCode, Exception e) {
        super(statusCode, e);
    }

    public DeleteMetadataException(int statusCode, String message, Exception e) {
        super(statusCode, message, e);
    }
}
