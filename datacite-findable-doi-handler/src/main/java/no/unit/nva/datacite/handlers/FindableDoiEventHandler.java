package no.unit.nva.datacite.handlers;

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
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnectionFactory;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto.Builder;
import no.unit.nva.publication.doi.update.dto.DoiUpdateHolder;
import no.unit.nva.transformer.Transformer;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindableDoiEventHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateHolder> {

    public static final String PUBLICATION_IS_MISSING_ERROR = "Publication is missing";
    public static final String CUSTOMER_ID_IS_MISSING_ERROR = "CustomerId is missing";// log messages
    public static final String RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG =
        "Received request to set landing page (make findable) for DOI {} to landing page {} for {}";
    public static final String EVENT_SOURCE = "doi.updateDoiStatus";
    public static final String DOI_IS_MISSING_OR_INVALID_ERROR = "Doi is missing or invalid";
    public static final String PUBLICATION_ID_MISSING_ERROR = "Publication id is missing";
    private static final Logger logger = LoggerFactory.getLogger(FindableDoiEventHandler.class);
    private static final String SUCCESSFULLY_MADE_DOI_FINDABLE = "Successfully handled request for Doi {} : {}";
    private static final String TRANSFORMING_PUBLICATION_ERROR = "Failed transforming publication into XML matching "
        + "DataCite XMLSchema";
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
        URI customerId = getCustomerId(publication);
        Doi doi = getDoi(publication);

        URI publicationId = getPublicationId(publication);
        URI landingPage = getLandingPage(publicationId);
        logger.debug(RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG, doi.toUri(), landingPage, customerId);

        try {
            doiClient.updateMetadata(customerId, doi, getDataCiteXmlMetadata(publication));
            doiClient.setLandingPage(customerId, doi, landingPage);
            DoiUpdateHolder doiUpdateHolder = new DoiUpdateHolder(EVENT_SOURCE,
                new Builder()
                    .withPublicationId(publicationId)
                    .withModifiedDate(Instant.now())
                    .withDoi(doi.toUri()).build());
            logger.debug(SUCCESSFULLY_MADE_DOI_FINDABLE, doi.toUri(), doiUpdateHolder.toJsonString());
            return doiUpdateHolder;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        String dataCiteConfigJson = AppEnv.getDataCiteConfig();
        DataCiteConfigurationFactory dataCiteConfigurationFactory = new DataCiteConfigurationFactory(
            IoUtils.stringToStream(dataCiteConfigJson));

        DataCiteMdsConnectionFactory dataCiteMdsConnectionFactory = new DataCiteMdsConnectionFactory(
            new PasswordAuthenticationFactory(dataCiteConfigurationFactory), AppEnv.getDataCiteHost(),
            AppEnv.getDataCitePort());

        return DoiClientFactory.getClient(dataCiteConfigurationFactory, dataCiteMdsConnectionFactory);
    }

    private Doi getDoi(Publication input) {
        return attempt(() -> Optional.ofNullable(input.getDoi())
            .map(doiUri -> Doi.builder().withDoi(doiUri).build())
            .orElseThrow(() -> new IllegalArgumentException(DOI_IS_MISSING_OR_INVALID_ERROR)))
            .orElseThrow((e) -> new IllegalArgumentException(DOI_IS_MISSING_OR_INVALID_ERROR, e.getException()));
    }

    private URI getPublicationId(Publication input) {
        return Optional.ofNullable(input.getId())
            .orElseThrow(() -> new IllegalArgumentException(PUBLICATION_ID_MISSING_ERROR));
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

    private String getDataCiteXmlMetadata(Publication publication) {
        DataCiteMetadataDto dataCiteMetadataDto = DataCiteMetadataDtoMapper.fromPublication(publication);
        try {
            return new Transformer(dataCiteMetadataDto).asXml();
        } catch (JAXBException e) {
            throw new RuntimeException(TRANSFORMING_PUBLICATION_ERROR, e);
        }
    }
}
