package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.ArrayList;
import no.unit.nva.datacite.commons.DataCiteMetadataResolver;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClientV2;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindableDoiEventHandler
    extends DestinationsEventBridgeEventHandler<DoiUpdateRequestEvent, Void> {

    public static final String MANDATORY_FIELD_ERROR_PREFIX = "Mandatory field is missing: ";
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG =
        "Received request to set landing page (make findable) for DOI {} to landing page {} for {}";
    private static final String SUCCESSFULLY_MADE_DOI_FINDABLE = "Successfully handled request for Doi {}";
    private static final Logger logger = LoggerFactory.getLogger(FindableDoiEventHandler.class);
    private final DoiClient doiClient;
    private final DataCiteMetadataResolver dataCiteMetadataResolver;

    @JacocoGenerated
    public FindableDoiEventHandler() {
        this(defaultDoiClient(), new DataCiteMetadataResolver());
    }

    public FindableDoiEventHandler(DoiClient doiClient, DataCiteMetadataResolver dataCiteMetadataResolver) {
        super(DoiUpdateRequestEvent.class);
        this.doiClient = doiClient;
        this.dataCiteMetadataResolver = dataCiteMetadataResolver;
    }

    @Override
    protected Void processInputPayload(DoiUpdateRequestEvent input,
                                       AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> event,
                                       Context context) {

        validateInput(input);
        try {
            var doi = getDoiFromEventOrDraftDoi(input);
            logger.info(RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG, doi.getUri(), input.getPublicationId(),
                        input.getCustomerId());
            String dataCiteXmlMetadata = dataCiteMetadataResolver.getDataCiteMetadataXml(input.getPublicationId());
            doiClient.updateMetadata(input.getCustomerId(), doi, dataCiteXmlMetadata);
            doiClient.setLandingPage(input.getCustomerId(), doi, input.getPublicationId());
            logger.info(SUCCESSFULLY_MADE_DOI_FINDABLE, doi.getUri());
            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private static void validateInput(DoiUpdateRequestEvent input) {
        var problems = new ArrayList<String>();
        if (isNull(input.getPublicationId())) {
            problems.add("publicationID");
        }
        if (isNull(input.getCustomerId())) {
            problems.add("customerID");
        }
        if (isNull(input.getDoi())) {
            problems.add("doi");
        }
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(MANDATORY_FIELD_ERROR_PREFIX + String.join(", ", problems));
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        return new DataCiteClientV2();
    }

    private Doi getDoiFromEventOrDraftDoi(DoiUpdateRequestEvent input) throws ClientException {
        return Doi.fromUri(input.getDoi());
    }
}
