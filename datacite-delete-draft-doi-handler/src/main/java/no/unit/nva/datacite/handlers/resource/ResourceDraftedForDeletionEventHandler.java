package no.unit.nva.datacite.handlers.resource;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URI;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClientV2;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.datacite.restclient.models.State;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.core.JacocoGenerated;


public class ResourceDraftedForDeletionEventHandler
    extends DestinationsEventBridgeEventHandler<ResourceDraftedForDeletionEvent, ResourceDraftedForDeletionEvent> {

    public static final String DRAFT = "draft";
    public static final URI NO_DOI = null;
    public static final String ERROR_GETTING_DOI_STATE = "Error getting DOI state";
    public static final String ERROR_DELETING_DRAFT_DOI = "Error deleting draft DOI";
    public static final String EXPECTED_EVENT_WITH_DOI = "Expected event with DOI";
    public static final String NOT_DRAFT_DOI_ERROR = "DOI state is not draft, aborting deletion.";
    public static final String DELETED_DRAFT_DOI_EVENT_TOPIC = "DoiRegistrarService.Doi.DeletedDraft";
    private final DoiClient doiClient;

    /**
     * Default constructor for DeleteDraftDoiHandler.
     *
     * @throws IOException IOException
     */
    @JacocoGenerated
    public ResourceDraftedForDeletionEventHandler() {
        this(defaultDoiClient());
    }

    /**
     * Constructor for DeleteDraftDoiHandler.
     *
     * @param doiClient doiClient
     */
    public ResourceDraftedForDeletionEventHandler(DoiClient doiClient) {
        super(ResourceDraftedForDeletionEvent.class);
        this.doiClient = doiClient;
    }

    @Override
    protected ResourceDraftedForDeletionEvent processInputPayload(
        ResourceDraftedForDeletionEvent input,
        AwsEventBridgeEvent<AwsEventBridgeDetail<ResourceDraftedForDeletionEvent>> event,
        Context context) {

        verifyEventHasDoi(input);

        var customerId = input.getCustomerId();
        var doi = getDoi(input);
        verifyDoiIsInDraftState(customerId, doi);
        return deleteDraftPublication(input, customerId, doi);
    }

    private void verifyEventHasDoi(ResourceDraftedForDeletionEvent event) {
        if (!event.hasDoi()) {
            throw new RuntimeException(EXPECTED_EVENT_WITH_DOI);
        }
    }

    private Doi getDoi(ResourceDraftedForDeletionEvent input) {
        return Doi.fromUri(input.getDoi());
    }

    private void verifyDoiIsInDraftState(URI customerId, Doi doi) {
        DoiStateDto doiState;
        try {
            doiState = doiClient.getDoi(customerId, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_GETTING_DOI_STATE, e);
        }

        if (!State.DRAFT.equals(doiState.getState())) {
            throw new RuntimeException(NOT_DRAFT_DOI_ERROR);
        }
    }

    private ResourceDraftedForDeletionEvent deleteDraftPublication(
        ResourceDraftedForDeletionEvent event,
        URI customerId,
        Doi doi) {
        try {
            doiClient.deleteDraftDoi(customerId, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_DELETING_DRAFT_DOI, e);
        }
        return copyDeletePublicationWithoutDoi(event);
    }

    private ResourceDraftedForDeletionEvent copyDeletePublicationWithoutDoi(ResourceDraftedForDeletionEvent event) {
        //Is this event necessary?
        return new ResourceDraftedForDeletionEvent(
            DELETED_DRAFT_DOI_EVENT_TOPIC,
            event.getIdentifier(),
            event.getStatus(),
            NO_DOI,
            event.getCustomerId()
        );
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        return new DataCiteClientV2();
    }
}
