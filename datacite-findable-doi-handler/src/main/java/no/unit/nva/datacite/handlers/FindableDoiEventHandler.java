package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import static no.unit.nva.datacite.handlers.LandingPageUtil.getLandingPage;
import static nva.commons.utils.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import javax.xml.bind.JAXBException;
import no.unit.nva.doi.DataCiteMetadataDtoMapper;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.DoiClientFactory;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.DoiRequestStatus;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import no.unit.nva.publication.doi.dto.PublicationStatus;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto.Builder;
import no.unit.nva.publication.doi.update.dto.DoiUpdateHolder;
import no.unit.nva.transformer.Transformer;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.aws.SecretsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindableDoiEventHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateHolder> {

    public static final String MANDATORY_FIELD_ERROR_PREFIX = "Mandatory field is missing: ";

    public static final String PUBLICATION_ID_FIELD_INFO = "Publication.id";
    public static final String PUBLICATION_INSTITUTION_OWNER_FIELD_INFO = "Publication.institutionOwner";
    public static final String PUBLICATION_MODIFIED_DATE_FIELD_INFO = "Publication.modifiedDate";
    public static final String PUBLICATION_TYPE_FIELD_INFO = "Publication.type";
    public static final String PUBLICATION_MAIN_TITLE_FIELD_INFO = "Publication.mainTitle";
    public static final String PUBLICATION_STATUS_FIELD_INFO = "Pblication.status";
    public static final String PUBLICATION_PUBLICATION_DATE_FIELD_INFO = "Publication.publicationDate";

    public static final String DOI_REQUEST_STATUS_FIELD_INFO = "DoiRequest.status";
    public static final String DOI_REQUEST_MODIFIED_DATE_FIELD_INFO = "DoiRequest.modifiedDate";

    public static final String PUBLICATION_DATE_YEAR_FIELD_INFO = "PublicationDate.year";

    // Data validation exceptions
    public static final String PUBLICATION_IS_MISSING_ERROR = "Publication is missing";
    public static final String DOI_IS_MISSING_OR_INVALID_ERROR = "Doi is missing or invalid";
    public static final String DOI_REQUEST_STATUS_WRONG_ERROR = "DoiRequestStatus is not APPROVED";
    public static final String TRANSFORMING_PUBLICATION_ERROR = "Failed transforming publication into XML matching "
        + "DataCite XMLSchema";

    // log errors
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG =
        "Received request to set landing page (make findable) for DOI {} to landing page {} for {}";
    private static final String SUCCESSFULLY_MADE_DOI_FINDABLE = "Successfully handled request for Doi {} : {}";

    private static final Logger logger = LoggerFactory.getLogger(FindableDoiEventHandler.class);
    public static final String CREATING_FINDABLE_DOI_FOR_DRAFT_PUBLICATION_ERROR =
        "Error: Attempting to make findable a non published publication";
    private final DoiClient doiClient;

    @JacocoGenerated
    public FindableDoiEventHandler() {
        this(defaultDoiClient());
    }

    public FindableDoiEventHandler(DoiClient doiClient) {
        super(PublicationHolder.class);
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateHolder processInputPayload(PublicationHolder input,
                                                  AwsEventBridgeEvent<AwsEventBridgeDetail<PublicationHolder>> event,
                                                  Context context) {

        Publication publication = getPublication(input);
        verifyPublicationCanBecomeFindable(publication);

        URI customerId = publication.getInstitutionOwner();
        Doi doi = getDoi(publication);
        URI publicationId = publication.getId();
        URI landingPage = getLandingPage(publicationId);

        logger.debug(RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG, doi.toUri(), landingPage, customerId);

        try {
            doiClient.updateMetadata(customerId, doi, getDataCiteXmlMetadata(publication));
            doiClient.setLandingPage(customerId, doi, landingPage);
            DoiUpdateHolder doiUpdateHolder = new DoiUpdateHolder(DoiUpdateHolder.DEFAULT_TYPE,
                createDoiUpdateDto(doi, publicationId));
            logger.debug(SUCCESSFULLY_MADE_DOI_FINDABLE, doi.toUri(), doiUpdateHolder.toJsonString());
            return doiUpdateHolder;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private void verifyPublicationCanBecomeFindable(Publication publication) {
        checkPublicationIsValid(publication);
        verifyPublicationIsPublished(publication);
        verifyPublicationIsCuratorApproved(publication);
    }

    private void checkPublicationIsValid(Publication publication) {
        mandatoryFieldsAreNotNull(publication);
        publicationDateHasYear(publication);
        doiRequestMandatoryFieldsAreNotNull(publication);
    }

    private void doiRequestMandatoryFieldsAreNotNull(Publication publication) {
        requireFieldIsNotNull(publication.getDoiRequest().getStatus(), DOI_REQUEST_STATUS_FIELD_INFO);
        requireFieldIsNotNull(publication.getDoiRequest().getModifiedDate(), DOI_REQUEST_MODIFIED_DATE_FIELD_INFO);
    }

    private void publicationDateHasYear(Publication publication) {
        requireFieldIsNotNull(publication.getPublicationDate().getYear(), PUBLICATION_DATE_YEAR_FIELD_INFO);
    }

    private void mandatoryFieldsAreNotNull(Publication publication) {
        requireFieldIsNotNull(publication.getId(), PUBLICATION_ID_FIELD_INFO);
        requireFieldIsNotNull(publication.getInstitutionOwner(), PUBLICATION_INSTITUTION_OWNER_FIELD_INFO);
        requireFieldIsNotNull(publication.getModifiedDate(), PUBLICATION_MODIFIED_DATE_FIELD_INFO);
        requireFieldIsNotNull(publication.getType(), PUBLICATION_TYPE_FIELD_INFO);
        requireFieldIsNotNull(publication.getMainTitle(), PUBLICATION_MAIN_TITLE_FIELD_INFO);
        requireFieldIsNotNull(publication.getStatus(), PUBLICATION_STATUS_FIELD_INFO);
        requireFieldIsNotNull(publication.getPublicationDate(), PUBLICATION_PUBLICATION_DATE_FIELD_INFO);
    }

    protected <T> void requireFieldIsNotNull(T value, String fieldName) {
        if (isNull(value)) {
            String errorMessage = MANDATORY_FIELD_ERROR_PREFIX + fieldName;
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void verifyPublicationIsPublished(Publication publication) {
        if (!PublicationStatus.PUBLISHED.equals(publication.getStatus())) {
            throw new IllegalStateException(CREATING_FINDABLE_DOI_FOR_DRAFT_PUBLICATION_ERROR);
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory dataCiteConfigurationFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), AppEnv.getCustomerSecretsSecretName(), AppEnv.getCustomerSecretsSecretKey());

        DataCiteConnectionFactory dataCiteMdsConnectionFactory = new DataCiteConnectionFactory(
            dataCiteConfigurationFactory,
            AppEnv.getDataCiteHost(),
            AppEnv.getDataCitePort()
        );

        return DoiClientFactory.getClient(dataCiteConfigurationFactory, dataCiteMdsConnectionFactory);
    }

    private void verifyPublicationIsCuratorApproved(Publication publication) {
        Optional.ofNullable(publication.getDoiRequest())
            .flatMap(e -> Optional.ofNullable(e.getStatus()))
            .filter(status -> status
                .equals(DoiRequestStatus.APPROVED))
            .orElseThrow(() -> new IllegalArgumentException(DOI_REQUEST_STATUS_WRONG_ERROR));
    }

    private DoiUpdateDto createDoiUpdateDto(Doi doi, URI publicationId) {
        return new Builder()
            .withPublicationId(publicationId)
            .withModifiedDate(Instant.now())
            .withDoi(doi.toUri()).build();
    }

    private Doi getDoi(Publication input) {
        return attempt(() -> Optional.ofNullable(input.getDoi())
            .map(doiUri -> Doi.builder().withDoi(doiUri).build())
            .orElseThrow(() -> new IllegalArgumentException(DOI_IS_MISSING_OR_INVALID_ERROR)))
            .orElseThrow((e) -> new IllegalArgumentException(DOI_IS_MISSING_OR_INVALID_ERROR, e.getException()));
    }





    private Publication getPublication(PublicationHolder input) {
        return Optional
            .ofNullable(input.getItem())
            .orElseThrow(() -> new IllegalArgumentException(PUBLICATION_IS_MISSING_ERROR));
    }

    private String getDataCiteXmlMetadata(Publication publication) {
        DataCiteMetadataDto dataCiteMetadataDto = DataCiteMetadataDtoMapper.fromPublication(publication);
        try {
            return new Transformer(dataCiteMetadataDto).asXml();
        } catch (JAXBException e) {
            throw new RuntimeException(TRANSFORMING_PUBLICATION_ERROR, e);
        }
    }
}
