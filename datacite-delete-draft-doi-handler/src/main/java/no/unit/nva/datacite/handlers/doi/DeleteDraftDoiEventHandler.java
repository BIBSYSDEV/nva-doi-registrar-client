package no.unit.nva.datacite.handlers.doi;

import static java.util.Objects.isNull;
import static no.unit.nva.datacite.handlers.resource.DeleteDraftDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.resource.DeleteDraftDoiAppEnv.getCustomerSecretsSecretName;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import no.unit.nva.datacite.commons.DoiUpdateDto;
import no.unit.nva.datacite.commons.DoiUpdateEvent;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

public class DeleteDraftDoiEventHandler extends DestinationsEventBridgeEventHandler
                                                    <DoiUpdateRequestEvent, DoiUpdateEvent> {

    protected static final String PUBLICATION_HAS_NO_PUBLISHER = "Publication has no publisher";
    protected static final String EXPECTED_EVENT_WITH_DOI = "Expected event with DOI";
    protected static final String ERROR_GETTING_DOI_STATE = "Error getting DOI state";
    protected static final String ERROR_DELETING_DRAFT_DOI = "Error deleting draft DOI";
    protected static final String NOT_DRAFT_DOI_ERROR = "DOI state is not draft, aborting deletion.";
    private static final String DOI_STATE_DRAFT = "draft";
    private final DoiClient doiClient;

    @JacocoGenerated
    public DeleteDraftDoiEventHandler() {
        this(defaultDoiClient());
    }

    public DeleteDraftDoiEventHandler(DoiClient doiClient) {
        super(DoiUpdateRequestEvent.class);
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateEvent processInputPayload(DoiUpdateRequestEvent input,
                                                 AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> event,

                                                 Context context) {
        validateInput(input);
        var doi = Doi.fromUri(input.getDoi());
        var customerId = input.getCustomerId();
        var publicationIdentifier = SortableIdentifier.fromUri(input.getPublicationId());
        verifyDoiIsInDraftState(customerId, doi);

        return new DoiUpdateEvent(DoiUpdateEvent.DOI_UPDATED_EVENT_TOPIC,
                                  deleteDraftDoi(customerId, publicationIdentifier, doi));
    }

    private static void validateInput(DoiUpdateRequestEvent input) {
        var problems = new ArrayList<String>();
        if (isNull(input.getCustomerId())) {
            problems.add("No customer in event.");
        }
        if (isNull(input.getDoi())) {
            problems.add("No doi in event.");
        }
        if (isNull(input.getPublicationId())) {
            problems.add("No publication Id in event.");
        }
        if (!problems.isEmpty()) {
            throw new RuntimeException(String.join("\n", problems));
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory connectionFactory = new DataCiteConnectionFactory(configFactory);
        return new DataCiteClient(configFactory, connectionFactory);
    }

    private void verifyDoiIsInDraftState(URI customerId, Doi doi) {
        DoiStateDto doiState;
        try {
            doiState = doiClient.getDoi(customerId, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_GETTING_DOI_STATE, e);
        }

        if (!DOI_STATE_DRAFT.equalsIgnoreCase(doiState.getState())) {
            throw new RuntimeException(NOT_DRAFT_DOI_ERROR);
        }
    }

    private DoiUpdateDto deleteDraftDoi(URI customerId,
                                        SortableIdentifier publicationIdentifier,
                                        Doi doi) {
        try {
            doiClient.deleteDraftDoi(customerId, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_DELETING_DRAFT_DOI, e);
        }

        return new DoiUpdateDto.Builder()
                   .withDoi(null)
                   .withPublicationId(publicationIdentifier)
                   .withModifiedDate(Instant.now())
                   .build();
    }
}
