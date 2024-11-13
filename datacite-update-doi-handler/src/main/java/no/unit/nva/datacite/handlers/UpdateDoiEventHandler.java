package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import static nva.commons.core.attempt.Try.attempt;
import static org.zalando.problem.Status.GONE;
import static org.zalando.problem.Status.MOVED_PERMANENTLY;
import com.amazonaws.services.lambda.runtime.Context;
import jakarta.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import no.unit.nva.datacite.commons.DataCiteMetadataResolver;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.datacite.commons.PublicationApiClientException;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClientV2;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.core.JacocoGenerated;
import org.datacide.schema.kernel_4.RelatedIdentifierType;
import org.datacide.schema.kernel_4.RelationType;
import org.datacide.schema.kernel_4.Resource;
import org.datacide.schema.kernel_4.Resource.RelatedIdentifiers;
import org.datacide.schema.kernel_4.Resource.RelatedIdentifiers.RelatedIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateDoiEventHandler
    extends DestinationsEventBridgeEventHandler<DoiUpdateRequestEvent, Void> {

    public static final String MANDATORY_FIELD_ERROR_PREFIX = "Mandatory field is missing: ";
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG =
        "Received request to set landing page (make findable) for DOI {} to landing page {} for {}";
    private static final String SUCCESSFULLY_MADE_DOI_FINDABLE =
        "Successfully handled request for Doi {}";
    public static final String SHOULD_REMOVE_METADATA_LOG_MESSAGE =
        "Request for publication {} returned {}. Any Findable DOI associated with this URI "
        + "will transition to Registered DOI.";
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_REGISTERED_LOG =
        "Will attempt to transition DOI {} to Registered DOI (for publication {} and customer {} , "
        + "and duplicateOf \"{}\")";
    private static final String SUCCESSFUL_DOI_REGISTERED =
        "Transition DOI {} to Registered DOI was successful (for publication {} and customer {})";
    private static final Logger logger = LoggerFactory.getLogger(UpdateDoiEventHandler.class);
    public static final String ADDING_DUPLICATE_IDENTIFIER_TO_RESOURCE = "Adding duplicate identifier to resource {}";
    public static final String DELETING_DRAFT_DOI_MESSAGE = "Deleting draft DOI {} for customer {} when unpublished publication {}";
    public static final String DOI_ALREADY_REGISTERED_MESSAGE = "Doi is already registered {} at customer {} and publication {}";
    private final DoiClient doiClient;
    private final DataCiteMetadataResolver dataCiteMetadataResolver;

    @JacocoGenerated
    public UpdateDoiEventHandler() {
        this(defaultDoiClient(), new DataCiteMetadataResolver());
    }

    public UpdateDoiEventHandler(DoiClient doiClient,
                                 DataCiteMetadataResolver dataCiteMetadataResolver) {
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
            var dataCiteXmlMetadata = dataCiteMetadataResolver.getDataCiteMetadataXml(input.getPublicationId());
            makePublicationFindable(input, doi, dataCiteXmlMetadata);

            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        } catch (PublicationApiClientException e) {
            makeDoiRegisteredIfPublicationGone(input, e, doi);
        }

        return null;
    }

    private void makeDoiRegisteredIfPublicationGone(
        DoiUpdateRequestEvent input,
        PublicationApiClientException e,
        Doi doi) {
        logger.info(RECEIVED_REQUEST_TO_MAKE_DOI_REGISTERED_LOG,
                    doi.getUri(),
                    input.getPublicationId(),
                    input.getCustomerId(),
                    input.getDuplicateOf().orElse(null));
        var response = attempt(() -> doiClient.getDoi(input.getCustomerId(), doi)).toOptional();
        if (response.isPresent()) {
            handleDoi(response.get(), input, doi, e) ;
        } else {
            throwException(e, input);
        }
    }

    private void throwException(PublicationApiClientException exception, DoiUpdateRequestEvent input) {
        logger.error("Unknown error for publication id {}", input.getPublicationId(), exception);
        throw exception;
    }

    private void handleDoi(DoiStateDto doiStateDto, DoiUpdateRequestEvent input, Doi doi,
                           PublicationApiClientException e) {
        switch (doiStateDto.getState()) {
            case FINDABLE -> handleFindableDoi(input, doi, e);
            case DRAFT -> handleDraftDoi(input, doi);
            case REGISTERED -> handleRegisteredDoi(input, doi);
            case null, default -> throwException(e, input);
        }
        logger.info(SUCCESSFUL_DOI_REGISTERED,
                    doi.getUri(),
                    input.getPublicationId(),
                    input.getCustomerId());

    }

    private void handleRegisteredDoi(DoiUpdateRequestEvent requestEvent, Doi doi) {
        logger.info(DOI_ALREADY_REGISTERED_MESSAGE, doi, requestEvent.getCustomerId(), requestEvent.getPublicationId());
    }

    private void handleDraftDoi(DoiUpdateRequestEvent updateRequestEvent, Doi doi) {
        try {
            logger.info(DELETING_DRAFT_DOI_MESSAGE, doi.getUri(), updateRequestEvent.getCustomerId(), updateRequestEvent.getPublicationId());
            doiClient.deleteDraftDoi(updateRequestEvent.getCustomerId(), doi);
        } catch (ClientException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleFindableDoi(DoiUpdateRequestEvent input, Doi doi, PublicationApiClientException exception) {
        if (isDeletedPublication(exception) || isDeletedDuplicatePublication(exception)) {
            logger.info(SHOULD_REMOVE_METADATA_LOG_MESSAGE, input.getPublicationId(), exception.getStatus());

            var resource = getMetadata(input, doi);

            if (input.getDuplicateOf().isPresent()) {
                var duplicateOf = input.getDuplicateOf().orElseThrow();
                logger.info(ADDING_DUPLICATE_IDENTIFIER_TO_RESOURCE, duplicateOf);
                addDuplicateIdentifier(resource, duplicateOf);
            }

            deleteMetadata(input.getCustomerId(), doi, toString(resource));
        }
    }

    private static boolean isDeletedDuplicatePublication(PublicationApiClientException e) {
        return e.getStatus() == MOVED_PERMANENTLY;
    }

    private static boolean isDeletedPublication(PublicationApiClientException e) {
        return e.getStatus() == GONE;
    }

    private static String toString(Resource resource) {
        var sw = new StringWriter();
        JAXB.marshal(resource, sw);
        return sw.toString();
    }

    private Resource getMetadata(DoiUpdateRequestEvent input, Doi doi) {
        return attempt(() -> doiClient.getMetadata(input.getCustomerId(), doi))
                   .map(UpdateDoiEventHandler::unmarshall)
                   .orElseThrow();
    }

    private static Resource unmarshall(String value) {
        return JAXB.unmarshal(new StringReader(value), Resource.class);
    }

    private static void addDuplicateIdentifier(Resource resource, URI duplicateOf) {
        var newIdentifier = new RelatedIdentifiers.RelatedIdentifier();
        newIdentifier.setRelatedIdentifierType(RelatedIdentifierType.URL);
        newIdentifier.setValue(duplicateOf.toString());
        newIdentifier.setRelationType(RelationType.IS_IDENTICAL_TO);
        newIdentifier.setResourceTypeGeneral(resource.getResourceType().getResourceTypeGeneral());

        if (isNull(resource.getRelatedIdentifiers())) {
            resource.setRelatedIdentifiers(new RelatedIdentifiers());
        } else {
            if (isRelatedIdentifierPresent(resource, newIdentifier)) {
                return;  // If an identical identifier is found, don't add the new one
            }
        }

        resource.getRelatedIdentifiers().getRelatedIdentifier().add(newIdentifier);
    }

    private static boolean isRelatedIdentifierPresent(Resource resource, RelatedIdentifier newIdentifier) {
        return resource.getRelatedIdentifiers().getRelatedIdentifier().stream()
                   .anyMatch(existingIdentifier -> isIdentical(newIdentifier, existingIdentifier));
    }

    private static boolean isIdentical(RelatedIdentifier newIdentifier, RelatedIdentifier existingIdentifier) {
        return existingIdentifier.getValue().equals(newIdentifier.getValue())
               && existingIdentifier.getRelatedIdentifierType().equals(newIdentifier.getRelatedIdentifierType());
    }

    private void makePublicationFindable(
        DoiUpdateRequestEvent input,
        Doi doi,
        String dataCiteXmlMetadata) throws ClientException {
        logger.info(RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG,
                    doi.getUri(),
                    input.getPublicationId(),
                    input.getCustomerId());

        doiClient.updateMetadata(input.getCustomerId(), doi, dataCiteXmlMetadata);
        doiClient.setLandingPage(input.getCustomerId(), doi, input.getPublicationId());
        logger.info(SUCCESSFULLY_MADE_DOI_FINDABLE, doi.getUri());
    }

    private void deleteMetadata(URI customerId, Doi doi, String updatedMetadata) {
        try {
            doiClient.updateMetadata(customerId, doi, updatedMetadata);
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
