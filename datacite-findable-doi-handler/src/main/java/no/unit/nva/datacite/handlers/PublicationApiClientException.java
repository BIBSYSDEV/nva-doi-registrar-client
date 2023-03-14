package no.unit.nva.datacite.handlers;

public class PublicationApiClientException extends RuntimeException {

    public PublicationApiClientException(String message) {
        super(message);
    }

    public PublicationApiClientException(Exception exception) {
        super(exception);
    }
}
