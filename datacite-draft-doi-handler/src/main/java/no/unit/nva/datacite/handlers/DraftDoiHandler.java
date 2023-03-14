package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.datacite.handlers.DraftDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.DraftDoiAppEnv.getCustomerSecretsSecretName;
import static nva.commons.core.attempt.Try.attempt;
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
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import nva.commons.secrets.SecretsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraftDoiHandler extends DestinationsEventBridgeEventHandler<DoiUpdateRequestEvent, DoiUpdateEvent> {

    // exception messages
    public static final String PUBLICATION_ID_IS_MISSING_ERROR = "Publication ID is missing";
    public static final String CUSTOMER_ID_IS_MISSING_ERROR = "CustomerId is missing";

    // log messages
    public static final String RECEIVED_REQUEST_TO_CREATE_DRAFT_NEW_DOI_LOG =
        "Received request to create draft new DOI for {}";
    public static final String DRAFTED_NEW_DOI_LOG = "Drafted new DOI: {}";
    public static final String ERROR_DRAFTING_DOI_LOG = "Error drafting DOI ";
    private static final Logger logger = LoggerFactory.getLogger(DraftDoiHandler.class);
    public static final String PUBLICATION_HAS_A_DOI_ALREADY = "Publication has a DOI already";
    private final DoiClient doiClient;

    /**
     * Default constructor for DraftDoiHandler.
     */
    @JacocoGenerated
    public DraftDoiHandler() {
        this(defaultDoiClient());
    }

    /**
     * Constructor for DraftDoiHandler.
     *
     * @param doiClient doiClient
     */
    public DraftDoiHandler(DoiClient doiClient) {
        super(DoiUpdateRequestEvent.class);
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateEvent processInputPayload(DoiUpdateRequestEvent input,
                                                 AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> event,
                                                 Context context) {
        validateEvent(input);
        var publicationIdentifier = SortableIdentifier.fromUri(input.getPublicationId());
        return draftNewDoi(publicationIdentifier, input.getCustomerId());
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory connectionFactory = new DataCiteConnectionFactory(configFactory);
        return new DataCiteClient(configFactory, connectionFactory);
    }

    private void validateEvent(DoiUpdateRequestEvent input) {
        var problems = new ArrayList<String>();
        if (nonNull(input.getDoi())) {
            problems.add(PUBLICATION_HAS_A_DOI_ALREADY);
        }
        if (isNull(input.getCustomerId())) {
            problems.add(CUSTOMER_ID_IS_MISSING_ERROR);
        }
        if (isNull(input.getPublicationId())) {
            problems.add(PUBLICATION_ID_IS_MISSING_ERROR);
        }
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", problems));
        }
    }

    private DoiUpdateEvent draftNewDoi(SortableIdentifier publicationId, URI customerId) {
        logger.debug(RECEIVED_REQUEST_TO_CREATE_DRAFT_NEW_DOI_LOG, customerId);

        return attempt(() -> createNewDoi(publicationId, customerId))
                   .map(doiUpdateDto -> new DoiUpdateEvent(DoiUpdateEvent.DOI_UPDATED_EVENT_TOPIC, doiUpdateDto))
                   .orElseThrow(this::handleCreatingNewDoiError);
    }

    private <T> RuntimeException handleCreatingNewDoiError(Failure<T> fail) {
        return new RuntimeException(ERROR_DRAFTING_DOI_LOG, fail.getException());
    }

    private DoiUpdateDto createNewDoi(SortableIdentifier publicationId, URI customerId) throws ClientException {
        Doi doi = doiClient.createDoi(customerId);
        logger.debug(DRAFTED_NEW_DOI_LOG, doi);
        return createUpdateDoi(publicationId, doi);
    }

    private DoiUpdateDto createUpdateDoi(SortableIdentifier publicationId, Doi doi) {
        return new DoiUpdateDto.Builder()
                   .withDoi(doi.getUri())
                   .withPublicationId(publicationId)
                   .withModifiedDate(Instant.now())
                   .build();
    }
}
