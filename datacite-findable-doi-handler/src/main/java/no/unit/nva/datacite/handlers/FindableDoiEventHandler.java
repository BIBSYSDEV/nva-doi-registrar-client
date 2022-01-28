package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretName;
import static no.unit.nva.datacite.handlers.PublicationPointers.DOI_REQUEST_MODIFIED_DATE_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.DOI_REQUEST_STATUS_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_DATE_YEAR_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_ID_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_INSTITUTION_OWNER_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_MAIN_TITLE_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_MODIFIED_DATE_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_PUBLICATION_DATE_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_STATUS_FIELD_INFO;
import static no.unit.nva.datacite.handlers.PublicationPointers.PUBLICATION_TYPE_FIELD_INFO;
import static no.unit.nva.doi.LandingPageUtil.publicationFrontPage;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import no.unit.nva.datacite.commons.DoiUpdateDto;
import no.unit.nva.datacite.commons.DoiUpdateEvent;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.doi.DataCiteMetadataDtoMapper;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.model.DoiRequestStatus;
import no.unit.nva.model.EntityDescription;
import no.unit.nva.model.Publication;
import no.unit.nva.model.PublicationStatus;
import no.unit.nva.model.Reference;
import no.unit.nva.model.instancetypes.PublicationInstance;
import no.unit.nva.transformer.Transformer;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindableDoiEventHandler
    extends DestinationsEventBridgeEventHandler<DoiUpdateRequestEvent, DoiUpdateEvent> {

    public static final String MANDATORY_FIELD_ERROR_PREFIX = "Mandatory field is missing: ";

    // Data validation exceptions
    public static final String PUBLICATION_IS_MISSING_ERROR = "Publication is missing";
    public static final String DOI_IS_MISSING_OR_INVALID_ERROR = "Doi is missing or invalid";
    public static final String DOI_REQUEST_STATUS_WRONG_ERROR = "DoiRequestStatus is not APPROVED";
    public static final String TRANSFORMING_PUBLICATION_ERROR = "Failed transforming publication into XML matching "
                                                                + "DataCite XMLSchema";
    public static final String CREATING_FINDABLE_DOI_FOR_DRAFT_PUBLICATION_ERROR =
        "Error: Attempting to make findable a non published publication";
    // log errors
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG =
        "Received request to set landing page (make findable) for DOI {} to landing page {} for {}";
    private static final String SUCCESSFULLY_MADE_DOI_FINDABLE = "Successfully handled request for Doi {} : {}";
    private static final Logger logger = LoggerFactory.getLogger(FindableDoiEventHandler.class);
    public static final String API_HOST = new Environment().readEnv("API_HOST");

    private final DoiClient doiClient;

    @JacocoGenerated
    public FindableDoiEventHandler() {
        this(defaultDoiClient());
    }

    public FindableDoiEventHandler(DoiClient doiClient) {
        super(DoiUpdateRequestEvent.class);
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateEvent processInputPayload(DoiUpdateRequestEvent input,
                                                 AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> event,
                                                 Context context) {

        Publication publication = input.getItem();
        verifyPublicationCanBecomeFindable(publication);

        URI customerId = extractPublisher(publication);
        Doi doi = getDoi(publication);
        SortableIdentifier publicationIdentifier = publication.getIdentifier();
        URI landingPage = publicationFrontPage(publicationIdentifier);

        logger.debug(RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG, doi.getUri(), landingPage, customerId);

        try {
            String dataCiteXmlMetadata = getDataCiteXmlMetadata(publication);
            doiClient.updateMetadata(customerId, doi, dataCiteXmlMetadata);
            doiClient.setLandingPage(customerId, doi, landingPage);
            DoiUpdateEvent doiUpdateHolder = new DoiUpdateEvent(DoiUpdateEvent.DOI_UPDATED_EVENT_TOPIC,
                                                                createDoiUpdateDto(doi, publicationIdentifier));
            logger.debug(SUCCESSFULLY_MADE_DOI_FINDABLE, doi.getUri(), doiUpdateHolder.toJsonString());
            return doiUpdateHolder;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected <T> void requireFieldIsNotNull(T value, String fieldName) {
        if (isNull(value)) {
            String errorMessage = MANDATORY_FIELD_ERROR_PREFIX + fieldName;
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory dataCiteConfigurationFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory dataCiteMdsConnectionFactory = new DataCiteConnectionFactory(
            dataCiteConfigurationFactory,
            FindableDoiAppEnv.getDataCiteMdsApiHost(),
            FindableDoiAppEnv.getDataCiteRestApiHost(),
            FindableDoiAppEnv.getDataCitePort()
        );

        return new DataCiteClient(dataCiteConfigurationFactory, dataCiteMdsConnectionFactory);
    }


    private URI extractPublisher(Publication publication) {
        return nonNull(publication.getPublisher()) ? publication.getPublisher().getId() : null;
    }

    private void verifyPublicationCanBecomeFindable(Publication publication) {
        checkPublicationIsValid(publication);
        verifyPublicationIsPublished(publication);
        verifyPublicationIsCuratorApproved(publication);
    }

    private void checkPublicationIsValid(Publication publication) {
        requirePublicationIsNotEmpty(publication);
        mandatoryFieldsAreNotNull(publication);
        publicationDateHasYear(publication);
        doiRequestMandatoryFieldsAreNotNull(publication);
        publicationHasNonEmptyDoi(publication);
    }

    private void requirePublicationIsNotEmpty(Publication publication) {
        if (isNull(publication)) {
            throw new IllegalArgumentException(PUBLICATION_IS_MISSING_ERROR);
        }
    }

    private void publicationHasNonEmptyDoi(Publication publication) {
        requireFieldIsNotNull(publication.getDoi(), DOI_IS_MISSING_OR_INVALID_ERROR);
    }

    private void doiRequestMandatoryFieldsAreNotNull(Publication publication) {
        requireFieldIsNotNull(publication.getDoiRequest().getStatus(), DOI_REQUEST_STATUS_FIELD_INFO);
        requireFieldIsNotNull(publication.getDoiRequest().getModifiedDate(), DOI_REQUEST_MODIFIED_DATE_FIELD_INFO);
    }

    private void publicationDateHasYear(Publication publication) {
        requireFieldIsNotNull(publication.getEntityDescription().getDate().getYear(), PUBLICATION_DATE_YEAR_FIELD_INFO);
    }

    private void mandatoryFieldsAreNotNull(Publication publication) {
        requireFieldIsNotNull(publication.getIdentifier(), PUBLICATION_ID_FIELD_INFO);
        requireFieldIsNotNull(publication.getPublisher(), PUBLICATION_INSTITUTION_OWNER_FIELD_INFO);
        requireFieldIsNotNull(publication.getModifiedDate(), PUBLICATION_MODIFIED_DATE_FIELD_INFO);
        requireFieldIsNotNull(extractInstanceType(publication), PUBLICATION_TYPE_FIELD_INFO);
        requireFieldIsNotNull(extractMainTitle(publication), PUBLICATION_MAIN_TITLE_FIELD_INFO);
        requireFieldIsNotNull(publication.getStatus(), PUBLICATION_STATUS_FIELD_INFO);
        requireFieldIsNotNull(publication.getEntityDescription().getDate(), PUBLICATION_PUBLICATION_DATE_FIELD_INFO);
    }

    private String extractMainTitle(Publication publication) {
        return Optional.of(publication)
            .map(Publication::getEntityDescription)
            .map(EntityDescription::getMainTitle)
            .orElse(null);
    }

    private String extractInstanceType(Publication publication) {
        return Optional.of(publication)
            .map(Publication::getEntityDescription)
            .map(EntityDescription::getReference)
            .map(Reference::getPublicationInstance)
            .map(PublicationInstance::getInstanceType)
            .orElse(null);
    }

    private void verifyPublicationIsPublished(Publication publication) {
        if (!PublicationStatus.PUBLISHED.equals(publication.getStatus())) {
            throw new IllegalStateException(CREATING_FINDABLE_DOI_FOR_DRAFT_PUBLICATION_ERROR);
        }
    }

    private void verifyPublicationIsCuratorApproved(Publication publication) {
        Optional.ofNullable(publication.getDoiRequest())
            .flatMap(e -> Optional.ofNullable(e.getStatus()))
            .filter(status -> status.equals(DoiRequestStatus.APPROVED))
            .orElseThrow(() -> new IllegalArgumentException(DOI_REQUEST_STATUS_WRONG_ERROR));
    }

    private DoiUpdateDto createDoiUpdateDto(Doi doi, SortableIdentifier publicationIdentifier) {
        return new DoiUpdateDto.Builder()
            .withPublicationId(publicationIdentifier)
            .withModifiedDate(Instant.now())
            .withDoi(doi.getUri()).build();
    }

    private Doi getDoi(Publication input) {
        return attempt(input::getDoi)
            .map(Doi::fromUri)
            .orElseThrow();
    }

    private String getDataCiteXmlMetadata(Publication publication) {
        DataCiteMetadataDto dataCiteMetadataDto = DataCiteMetadataDtoMapper.fromPublication(publication);
        return new Transformer(dataCiteMetadataDto).asXml();
    }
}
