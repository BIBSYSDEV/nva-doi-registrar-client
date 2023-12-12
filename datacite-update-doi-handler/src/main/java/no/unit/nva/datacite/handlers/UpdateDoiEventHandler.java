package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import static org.zalando.problem.Status.GONE;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.util.ArrayList;
import no.unit.nva.datacite.commons.DataCiteMetadataResolver;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.datacite.commons.PublicationApiClientException;
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

public class UpdateDoiEventHandler
    extends DestinationsEventBridgeEventHandler<DoiUpdateRequestEvent, Void> {

    public static final String MANDATORY_FIELD_ERROR_PREFIX = "Mandatory field is missing: ";
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG =
        "Received request to set landing page (make findable) for DOI {} to landing page {} for {}";
    private static final String SUCCESSFULLY_MADE_DOI_FINDABLE = "Successfully handled request for Doi {}";
    public static final String SHOULD_REMOVE_METADATA_LOG_MESSAGE =
        "Request for publication {} returned 410 Gone. Any Findable DOI associated with this URI will transition to "
        + "Registered DOI.";
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_REGISTERED_LOG =
        "Will attempt to transition DOI {} to Registered DOI (for publication {} and customer {})";
    private static final String SUCCESSFUL_DOI_REGISTERED =
        "Transition DOI {} to Registered DOI was successful (for publication {} and customer {})";
    private static final Logger logger = LoggerFactory.getLogger(UpdateDoiEventHandler.class);
    private final DoiClient doiClient;
    private final DataCiteMetadataResolver dataCiteMetadataResolver;

    @JacocoGenerated
    public UpdateDoiEventHandler() {
        this(defaultDoiClient(), new DataCiteMetadataResolver());
    }

    public UpdateDoiEventHandler(DoiClient doiClient, DataCiteMetadataResolver dataCiteMetadataResolver) {
        super(DoiUpdateRequestEvent.class);
        this.doiClient = doiClient;
        this.dataCiteMetadataResolver = dataCiteMetadataResolver;
    }

    @Override
    protected Void processInputPayload(DoiUpdateRequestEvent input,
                                       AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> event,
                                       Context context) {

        validateInput(input);

        var doi = getDoiFromEventOrDraftDoi(input);

        try {
            String dataCiteXmlMetadata = dataCiteMetadataResolver.getDataCiteMetadataXml(input.getPublicationId());
            makePublicationFindable(input, doi, dataCiteXmlMetadata);

            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        } catch (PublicationApiClientException e) {
            makePublicationRegistered(input, e, doi);
        }

        return null;
    }

    private void makePublicationRegistered(
        DoiUpdateRequestEvent input,
        PublicationApiClientException e,
        Doi doi) {
        logger.info(RECEIVED_REQUEST_TO_MAKE_DOI_REGISTERED_LOG,
                    doi.getUri(),
                    input.getPublicationId(),
                    input.getCustomerId());

        if (e.getStatus().getStatusCode() == GONE.getStatusCode()) {
            logger.info(SHOULD_REMOVE_METADATA_LOG_MESSAGE, input.getPublicationId());
            deleteMetadata(input.getCustomerId(), doi);
        } else {
            logger.error("Unknown error for publication id {}", input.getPublicationId(), e);
            throw e;
        }

        logger.info(SUCCESSFUL_DOI_REGISTERED, doi.getUri(), input.getPublicationId(), input.getCustomerId());
    }

    private void makePublicationFindable(DoiUpdateRequestEvent input, Doi doi, String dataCiteXmlMetadata) throws ClientException {
        logger.info(RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG, doi.getUri(), input.getPublicationId(),
                    input.getCustomerId());

        doiClient.updateMetadata(input.getCustomerId(), doi, dataCiteXmlMetadata);
        doiClient.setLandingPage(input.getCustomerId(), doi, input.getPublicationId());
        logger.info(SUCCESSFULLY_MADE_DOI_FINDABLE, doi.getUri());
    }

    private void deleteMetadata(URI customerId, Doi doi) {
        try {
            doiClient.deleteMetadata(customerId, doi);
        } catch (ClientException ex) {
            throw new RuntimeException(ex);
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

    private Doi getDoiFromEventOrDraftDoi(DoiUpdateRequestEvent input) {
        return Doi.fromUri(input.getDoi());
    }
}