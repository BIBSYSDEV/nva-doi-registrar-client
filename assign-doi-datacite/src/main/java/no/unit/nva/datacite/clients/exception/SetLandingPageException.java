package no.unit.nva.datacite.clients.exception;

import no.unit.nva.datacite.clients.models.Doi;
import nva.commons.utils.JacocoGenerated;

@JacocoGenerated
public class SetLandingPageException extends UpstreamApiException {

    private final Doi doi;

    public SetLandingPageException(Doi doi, int statusCode) {
        super(statusCode, String.format(ERROR_MESSAGE_FORMAT, doi.toIdentifier(), statusCode));
        this.doi = doi;
    }

    public Doi getDoi() {
        return doi;
    }
}
