package no.unit.nva.doi.datacite.clients.exception;

import no.unit.nva.doi.models.Doi;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class DeleteDraftDoiException extends UpstreamApiException {

    private final Doi doi;

    public DeleteDraftDoiException(Doi doi, int statusCode) {
        super(statusCode, String.format(ERROR_MESSAGE_FORMAT, doi.toIdentifier(), statusCode));
        this.doi = doi;
    }

    @JacocoGenerated
    public Doi getDoi() {
        return doi;
    }
}
