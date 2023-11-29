package no.unit.nva.datacite.commons;

import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class PublicationApiClientException extends RuntimeException implements Problem {

    private final Status statusType;

    @Override
    public String getTitle() {
        return this.getMessage();
    }

    @Override
    public Status getStatus() {
        return statusType;
    }

    public PublicationApiClientException(String message, int statusType) {
        super(message);
        this.statusType = Status.valueOf(statusType);
    }

    public PublicationApiClientException(Exception exception) {
        super(exception);

        if (exception instanceof PublicationApiClientException publicationApiClientException) {
            this.statusType = publicationApiClientException.getStatus();
        } else {
            this.statusType = Status.INTERNAL_SERVER_ERROR;
        }
    }
}
