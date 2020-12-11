package no.unit.nva.datacite.handlers;

import static nva.commons.utils.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.DoiClientFactory;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.DoiRequestStatus;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto;
import no.unit.nva.publication.doi.update.dto.DoiUpdateHolder;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.attempt.Failure;
import nva.commons.utils.aws.SecretsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraftDoiHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateHolder> {

    // exception messages
    public static final String PUBLICATION_IS_MISSING_ERROR = "Publication is missing";
    public static final String CUSTOMER_ID_IS_MISSING_ERROR = "CustomerId is missing";

    // log messages
    public static final String RECEIVED_REQUEST_TO_CREATE_DRAFT_NEW_DOI_LOG =
        "Received request to create draft new DOI for {}";
    public static final String DRAFTED_NEW_DOI_LOG = "Drafted new DOI: {}";
    public static final String ERROR_DRAFTING_DOI_LOG = "Error drafting DOI ";

    private static final Logger logger = LoggerFactory.getLogger(DraftDoiHandler.class);
    public static final String NOT_APPROVED_DOI_REQUEST_ERROR = "DoiRequest has not been approved for publication:";
    private final DoiClient doiClient;

    /**
     * Default constructor for DraftDoiHandler.
     *
     * @throws IOException IOException
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
        super(PublicationHolder.class);
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateHolder processInputPayload(PublicationHolder input,
                                                  AwsEventBridgeEvent<AwsEventBridgeDetail<PublicationHolder>> event,
                                                  Context context) {
        Publication publication = getPublication(input);
        if (doiIsRequested(publication)) {
            URI customerId = getCustomerId(publication);
            logger.debug(RECEIVED_REQUEST_TO_CREATE_DRAFT_NEW_DOI_LOG, customerId);

            return attempt(() -> createNewDoi(publication, customerId))
                .map(doiUpdateDto -> new DoiUpdateHolder(DoiUpdateHolder.DEFAULT_TYPE, doiUpdateDto))
                .orElseThrow(this::handleCreatingNewDoiError);
        }
        throw new IllegalStateException(NOT_APPROVED_DOI_REQUEST_ERROR + publication.getId().toString());
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), AppEnv.getCustomerSecretsSecretName(), AppEnv.getCustomerSecretsSecretKey());

        DataCiteConnectionFactory connectionFactory = new DataCiteConnectionFactory(
            configFactory,
            AppEnv.getDataCiteRestApiHost(),
            AppEnv.getDataCitePort());
        return DoiClientFactory.getClient(configFactory, connectionFactory);
    }

    private boolean doiIsRequested(Publication publication) {
        return DoiRequestStatus.REQUESTED.equals(publication.getDoiRequest().getStatus());
    }

    private <T> RuntimeException handleCreatingNewDoiError(Failure<T> fail) {
        return new RuntimeException(ERROR_DRAFTING_DOI_LOG, fail.getException());
    }

    private DoiUpdateDto createNewDoi(Publication publication, URI customerId) throws ClientException {
        Doi doi = doiClient.createDoi(customerId);
        logger.debug(DRAFTED_NEW_DOI_LOG, doi);
        return createUpdateDoi(publication, doi);
    }

    private DoiUpdateDto createUpdateDoi(Publication input, Doi doi) {
        return new DoiUpdateDto.Builder()
            .withDoi(doi.toUri())
            .withPublicationId(input.getId())
            .withModifiedDate(Instant.now())
            .build();
    }

    private URI getCustomerId(Publication publication) {
        return Optional
            .ofNullable(publication.getInstitutionOwner())
            .orElseThrow(() -> new IllegalArgumentException(CUSTOMER_ID_IS_MISSING_ERROR));
    }

    private Publication getPublication(PublicationHolder input) {
        return Optional
            .ofNullable(input.getItem())
            .orElseThrow(() -> new IllegalArgumentException(PUBLICATION_IS_MISSING_ERROR));
    }
}
