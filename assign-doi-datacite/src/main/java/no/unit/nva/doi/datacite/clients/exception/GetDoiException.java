package no.unit.nva.doi.datacite.clients.exception;

import no.unit.nva.doi.models.Doi;
import nva.commons.utils.JacocoGenerated;

@JacocoGenerated
public class GetDoiException extends UpstreamApiException {

    private final Doi doi;

    public GetDoiException(Doi doi, int statusCode) {
        super(statusCode, String.format(ERROR_MESSAGE_FORMAT, doi.toIdentifier(), statusCode));
        this.doi = doi;
    }

    public Doi getDoi() {
        return doi;
    }
}
