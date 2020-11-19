package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import javax.xml.bind.JAXBException;
import no.unit.nva.doi.model.DoiUpdateDto;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import no.unit.nva.transformer.Transformer;
import no.unit.nva.transformer.dto.DynamoRecordDto;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraftDoiHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateDto> {

    public static final String PUBLICATION_IS_MISSING_ERROR = "Publication is missing";
    public static final String CUSTOMER_ID_IS_MISSING_ERROR = "CustomerId is missing";
    public static final String TRANSFORMING_PUBLICATION_ERROR = "Error transforming Publication to DataCite XML";
    private TemporaryDoiClient doiClient;

    private static final Logger logger = LoggerFactory.getLogger(DraftDoiHandler.class);

    /**
     * Default constructor for DraftDoiHandler.
     */
    @JacocoGenerated
    public  DraftDoiHandler() {
        this(defaultDoiClient());
    }

    @JacocoGenerated
    private static TemporaryDoiClient defaultDoiClient() {
        // TODO: replace with real DataCite client
        return new TemporaryDoiClient() {
            @JacocoGenerated
            @Override
            public URI createDoi(String customerId, String metadataDataCiteXml) {
                return URI.create("http://example.doi");
            }

            @JacocoGenerated
            @Override
            public void updateMetadata(String customerId, String doi, String metadataDataCiteXml) {

            }

            @JacocoGenerated
            @Override
            public void setLandingPage(String customerId, String doi, URI url) {

            }
        };
    }

    /**
     * Constructor for DraftDoiHandler.
     *
     * @param doiClient doiClient
     */
    public DraftDoiHandler(TemporaryDoiClient doiClient) {
        super(PublicationHolder.class);
        this.doiClient = doiClient;
    }

    private DoiUpdateDto createUpdateDoi(Publication input, URI doi) {
        return new DoiUpdateDto.Builder()
            .withDoi(doi)
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
        logger.debug("Received request to create draft new DOI for {}", customerId);

        String dataCiteXml = getDataCiteXml(publication);
        URI doi = doiClient.createDoi(customerId.toString(), dataCiteXml);
        logger.debug("Drafted new DOI: {}", doi);

        return createUpdateDoi(publication, doi);
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
        DynamoRecordDto dynamoRecordDto = DynamoRecordDtoMapper.fromPublication(publication);
        try {
            return new Transformer(dynamoRecordDto).asXml();
        } catch (JAXBException e) {
            throw new RuntimeException(TRANSFORMING_PUBLICATION_ERROR, e);
        }
    }
}
