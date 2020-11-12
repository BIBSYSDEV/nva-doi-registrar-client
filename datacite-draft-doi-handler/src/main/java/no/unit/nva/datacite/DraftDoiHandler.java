package no.unit.nva.datacite;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.time.Instant;
import no.unit.nva.datacite.model.DoiUpdateDto;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Publication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraftDoiHandler extends EventHandler<Publication, DoiUpdateDto> {

    private TransformService transformService;
    private DoiClient doiClient;

    private static final Logger logger = LoggerFactory.getLogger(DraftDoiHandler.class);

    public  DraftDoiHandler() {
        this(defaultTransformService(), defaultDoiClient());
    }

    private static DoiClient defaultDoiClient() {
        return new DoiClient() {
            @Override
            public String createDoi(String customerId, String metadataDataciteXml) {
                return "http://example.doi";
            }

            @Override
            public void updateMetadata(String customerId, String doi, String metadataDataciteXml) {

            }

            @Override
            public void setLandingPage(String customerId, String doi, URI url) {

            }
        };
    }

    private static TransformService defaultTransformService() {
        return new DataciteTransformService();
    }

    public DraftDoiHandler(TransformService transformService, DoiClient doiClient) {
        super(DraftDoiHandler.class);
        this.transformService = transformService;
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateDto processInput(Publication input, AwsEventBridgeEvent<Publication> event, Context context) {
        String customerId = input.getInstitutionOwner().toString();
        logger.debug("Received request to create draft new DOI for {}", customerId);

        String dataciteXml = transformService.getXml(input);
        String doi = doiClient.createDoi(customerId, dataciteXml);
        logger.debug("Drafted new DOI: {}", doi);

        return createUpdateDoi(input, doi);
    }

    private DoiUpdateDto createUpdateDoi(Publication input, String doi) {
        return new DoiUpdateDto.Builder()
            .withDoi(doi)
            .withPublicationId(input.getId())
            .withModifiedDate(Instant.now())
            .build();
    }
}
