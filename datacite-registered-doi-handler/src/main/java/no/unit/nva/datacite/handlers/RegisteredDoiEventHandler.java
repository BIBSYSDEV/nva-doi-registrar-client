package no.unit.nva.datacite.handlers;

import static org.zalando.problem.Status.GONE;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import no.unit.nva.datacite.commons.DataCiteMetadataResolver;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.datacite.commons.PublicationApiClientException;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClientV2;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisteredDoiEventHandler extends DestinationsEventBridgeEventHandler<DoiUpdateRequestEvent, Void> {
    private static final Logger logger = LoggerFactory.getLogger(RegisteredDoiEventHandler.class);
    private final DoiClient doiClient;
    private final DataCiteMetadataResolver dataCiteMetadataResolver;
    public static final String SHOULD_REMOVE_METADATA_LOG_MESSAGE =
        "Request for publication {} returned 410 Gone. Any Findable DOI associated with this URI will transition to "
        + "Registered DOI.";
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_REGISTERED_LOG =
        "Will attempt to transition DOI {} to Registered DOI (for publication {} and customer {})";

    private static final String SUCCESSFUL_DOI_REGISTERED =
        "Transition DOI {} to Registered DOI was successful (for publication {} and customer {})";

    @JacocoGenerated
    public RegisteredDoiEventHandler() {
        this(defaultDoiClient(), new DataCiteMetadataResolver());
    }

    public RegisteredDoiEventHandler(DoiClient doiClient, DataCiteMetadataResolver dataCiteMetadataResolver) {
        super(DoiUpdateRequestEvent.class);
        this.doiClient = doiClient;
        this.dataCiteMetadataResolver = dataCiteMetadataResolver;
    }

    @Override
    protected Void processInputPayload(DoiUpdateRequestEvent input,
                                       AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> event,
                                       Context context) {
        var doi  = Doi.fromUri(input.getDoi());
        logger.info(RECEIVED_REQUEST_TO_MAKE_DOI_REGISTERED_LOG, doi.getUri(), input.getPublicationId(),
                    input.getCustomerId());
        try {
            dataCiteMetadataResolver.getDataCiteMetadataXml(input.getPublicationId());
        } catch (PublicationApiClientException e) {
            conditionalDeleteDoi(input, e, doi);
            logger.info(SUCCESSFUL_DOI_REGISTERED, doi.getUri(),  input.getPublicationId(), input.getCustomerId());
        }
        return null;
    }

    private void conditionalDeleteDoi(DoiUpdateRequestEvent input, PublicationApiClientException e, Doi doi) {
        if (e.getStatus().getStatusCode() == GONE.getStatusCode()) {
            logger.info(SHOULD_REMOVE_METADATA_LOG_MESSAGE, input.getPublicationId());
            deleteMetadata(input.getCustomerId(), doi);
        } else {
            logger.error("Unknown error for publication id {}", input.getPublicationId(), e);
            throw e;
        }
    }

    private void deleteMetadata(URI customerId, Doi doi) {
        try {
            doiClient.deleteMetadata(customerId, doi);
        } catch (ClientException ex) {
            throw new RuntimeException(ex);
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        return new DataCiteClientV2();
    }
}
