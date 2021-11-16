package no.unit.nva.datacite.handlers;

import static no.unit.nva.datacite.handlers.DeleteDraftDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.DeleteDraftDoiAppEnv.getCustomerSecretsSecretName;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URI;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.DoiClientFactory;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.ImmutableDoi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

public class DeleteDraftDoiHandler
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
    public DeleteDraftDoiHandler() {
        this(defaultDoiClient());
    }

    /**
     * Constructor for DeleteDraftDoiHandler.
     *
     * @param doiClient doiClient
     */
    public DeleteDraftDoiHandler(DoiClient doiClient) {
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

    private ImmutableDoi getDoi(ResourceDraftedForDeletionEvent input) {
        return ImmutableDoi.builder()
            .withDoi(input.getDoi())
            .build();
    }

    private void verifyDoiIsInDraftState(URI customerId, ImmutableDoi doi) {
        DoiStateDto doiState;
        try {
            doiState = doiClient.getDoi(customerId, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_GETTING_DOI_STATE, e);
        }

        if (!DRAFT.equalsIgnoreCase(doiState.getState())) {
            throw new RuntimeException(NOT_DRAFT_DOI_ERROR);
        }
    }

    private ResourceDraftedForDeletionEvent deleteDraftPublication(
        ResourceDraftedForDeletionEvent event,
        URI customerId,
        ImmutableDoi doi) {
        try {
            doiClient.deleteDraftDoi(customerId, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_DELETING_DRAFT_DOI, e);
        }
        return copyDeletePublicationWithoutDoi(event);
    }

    private ResourceDraftedForDeletionEvent copyDeletePublicationWithoutDoi(ResourceDraftedForDeletionEvent event) {
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

        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory connectionFactory = new DataCiteConnectionFactory(
            configFactory,
            DeleteDraftDoiAppEnv.getDataCiteMdsApiHost(),
            DeleteDraftDoiAppEnv.getDataCiteRestApiHost(),
            DeleteDraftDoiAppEnv.getDataCitePort());
        return DoiClientFactory.getClient(configFactory, connectionFactory);
    }
}
