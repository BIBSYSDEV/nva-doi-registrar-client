package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteConnectionFactory;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraftDoiHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateDto> {

    // exception messages
    public static final String PUBLICATION_IS_MISSING_ERROR = "Publication is missing";
    public static final String CUSTOMER_ID_IS_MISSING_ERROR = "CustomerId is missing";
    public static final String TRANSFORMING_PUBLICATION_ERROR = "Error transforming Publication to DataCite XML";

    // log messages
    public static final String RECEIVED_REQUEST_TO_CREATE_DRAFT_NEW_DOI_LOG =
        "Received request to create draft new DOI for {}";
    public static final String DRAFTED_NEW_DOI_LOG = "Drafted new DOI: {}";
    public static final String ERROR_DRAFTING_DOI_LOG = "Error drafting DOI ";
    private static final Logger logger = LoggerFactory.getLogger(DraftDoiHandler.class);
    private final DoiClient doiClient;

    /**
     * Default constructor for DraftDoiHandler.
     *
     * @throws IOException IOException
     */
    @JacocoGenerated
    public DraftDoiHandler() throws IOException {
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
    protected DoiUpdateDto processInputPayload(PublicationHolder input,
                                               AwsEventBridgeEvent<AwsEventBridgeDetail<PublicationHolder>> event,
                                               Context context) {
        Publication publication = getPublication(input);
        URI customerId = getCustomerId(publication);
        logger.debug(RECEIVED_REQUEST_TO_CREATE_DRAFT_NEW_DOI_LOG, customerId);

        try {
            Doi doi = doiClient.createDoi(customerId);
            logger.debug(DRAFTED_NEW_DOI_LOG, doi.toUri());
            return createUpdateDoi(publication, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_DRAFTING_DOI_LOG, e);
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        String dataCiteConfigJson = AppEnv.getDataCiteConfig();
        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(dataCiteConfigJson);
        DataCiteConnectionFactory connectionFactory = new DataCiteConnectionFactory(
            new PasswordAuthenticationFactory(configFactory),
            AppEnv.getDataCiteHost(),
            AppEnv.getDataCitePort());
        return DoiClientFactory.getClient(configFactory, connectionFactory);
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
