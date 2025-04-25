package no.unit.nva.datacite.events;

import java.net.URI;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.State;
import no.unit.nva.doi.models.Doi;

public class DoiManager {

    private final DoiClient doiClient;

    public DoiManager(final DoiClient doiClient) {
        this.doiClient = doiClient;
    }

    public void deleteDoiIfOnlyDrafted(URI customerId, Doi doi) throws ClientException {
        var doiState = doiClient.getDoi(customerId, doi);
        if (State.DRAFT.equals(doiState.getState())) {
            doiClient.deleteDraftDoi(customerId, doi);
        }
    }
}
