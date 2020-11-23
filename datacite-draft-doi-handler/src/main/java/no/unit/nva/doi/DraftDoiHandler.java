package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import javax.xml.bind.JAXBException;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.models.Doi;
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnectionFactory;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto;
import no.unit.nva.transformer.Transformer;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
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

    private final DoiClient doiClient;

    private static final Logger logger = LoggerFactory.getLogger(DraftDoiHandler.class);

    /**
     * Default constructor for DraftDoiHandler.
     *
     * @throws IOException  IOException
     */
    @JacocoGenerated
    public  DraftDoiHandler() throws IOException {
        this(defaultDoiClient());
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        String dataCiteConfigJson = AppEnv.getDataCiteConfig();
        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(dataCiteConfigJson);
        DataCiteMdsConnectionFactory connectionFactory = new DataCiteMdsConnectionFactory(
            new PasswordAuthenticationFactory(configFactory),
            AppEnv.getDataCiteHost(),
            AppEnv.getDataCitePort());
        return DoiClientFactory.getClient(configFactory, connectionFactory);
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

    private DoiUpdateDto createUpdateDoi(Publication input, Doi doi) {
        return new DoiUpdateDto.Builder()
            .withDoi(doi.toId())
            .withPublicationId(input.getId())
            .withModifiedDate(Instant.now())
            .build();
    }

    @Override
    protected DoiUpdateDto processInputPayload(PublicationHolder input,
                                               AwsEventBridgeEvent<AwsEventBridgeDetail<PublicationHolder>> event,
                                               Context context) {
        Publication publication = getPublication(input);
        URI customerId = getCustomerId(publication);
        logger.debug(RECEIVED_REQUEST_TO_CREATE_DRAFT_NEW_DOI_LOG, customerId);

        String dataCiteXml = getDataCiteXml(publication);
        try {
            Doi doi = doiClient.createDoi(customerId, dataCiteXml);
            logger.debug(DRAFTED_NEW_DOI_LOG, doi);
            return createUpdateDoi(publication, doi);
        } catch (ClientException e) {
            throw new RuntimeException(ERROR_DRAFTING_DOI_LOG, e);
        }

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

    private String getDataCiteXml(Publication publication) {
        DataCiteMetadataDto dataCiteMetadataDto = DataCiteMetadataDtoMapper.fromPublication(publication);
        try {
            return new Transformer(dataCiteMetadataDto).asXml();
        } catch (JAXBException e) {
            throw new RuntimeException(TRANSFORMING_PUBLICATION_ERROR, e);
        }
    }
}
