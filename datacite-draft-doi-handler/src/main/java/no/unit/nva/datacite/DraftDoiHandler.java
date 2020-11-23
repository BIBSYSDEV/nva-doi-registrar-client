package no.unit.nva.datacite;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.time.Instant;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraftDoiHandler extends EventHandler<Publication, DoiUpdateDto> {

    private static final Logger logger = LoggerFactory.getLogger(DraftDoiHandler.class);
    private final TransformService transformService;
    private final DoiClient doiClient;

    /**
     * Default constructor for DraftDoiHandler.
     */
    @JacocoGenerated
    public DraftDoiHandler() {
        this(defaultTransformService(), defaultDoiClient());
    }

    public DraftDoiHandler(TransformService transformService, DoiClient doiClient) {
        super(DraftDoiHandler.class);
        this.transformService = transformService;
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateDto processInput(Publication input, AwsEventBridgeEvent<Publication> event, Context context) {
        URI customerId = input.getInstitutionOwner();
        logger.debug("Received request to create draft new DOI for {}", customerId.toASCIIString());

        String dataciteXml = transformService.getXml(input);
        try {
            Doi doi = doiClient.createDoi(customerId, dataciteXml);
            logger.debug("Drafted new DOI: {}", doi);
            return createUpdateDoi(input, doi);
        } catch (ClientRuntimeException | ClientException e) {
            logger.error("Failed to create doi", e);
            throw new RuntimeException(e);
        }
    }

    private static DoiClient defaultDoiClient() {
        return null;
    }

    private static TransformService defaultTransformService() {
        return new DataciteTransformService();
    }

    private DoiUpdateDto createUpdateDoi(Publication input, Doi doi) {
        return new DoiUpdateDto.Builder()
            .withDoi(doi.toUri())
            .withPublicationId(input.getId())
            .withModifiedDate(Instant.now())
            .build();
    }
}
