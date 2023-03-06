package no.unit.nva.datacite.handlers;

import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretName;
import com.amazonaws.services.lambda.runtime.Context;
import java.time.Instant;
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
import no.unit.nva.model.Publication;
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
    public static final String CREATING_FINDABLE_DOI_FOR_DRAFT_PUBLICATION_ERROR =
        "Error: Attempting to make findable a non published publication";
    public static final String API_HOST = new Environment().readEnv("API_HOST");
    // log errors
    private static final String RECEIVED_REQUEST_TO_MAKE_DOI_FINDABLE_LOG =
        "Received request to set landing page (make findable) for DOI {} to landing page {} for {}";
    private static final String SUCCESSFULLY_MADE_DOI_FINDABLE = "Successfully handled request for Doi {} : {}";
    private static final Logger logger = LoggerFactory.getLogger(FindableDoiEventHandler.class);
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

        var publication = input.getItem();

        var customerId = input.getCustomerId();
        var doi = Doi.fromUri(input.getDoi());
        var publicationIdentifier = SortableIdentifier.fromUri(input.getPublicationId());
        var landingPage = input.getPublicationId();

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

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory dataCiteConfigurationFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory dataCiteMdsConnectionFactory =
            new DataCiteConnectionFactory(dataCiteConfigurationFactory);

        return new DataCiteClient(dataCiteConfigurationFactory, dataCiteMdsConnectionFactory);
    }

    private DoiUpdateDto createDoiUpdateDto(Doi doi, SortableIdentifier publicationIdentifier) {
        return new DoiUpdateDto.Builder()
                   .withPublicationId(publicationIdentifier)
                   .withModifiedDate(Instant.now())
                   .withDoi(doi.getUri()).build();
    }

    private String getDataCiteXmlMetadata(Publication publication) {
        DataCiteMetadataDto dataCiteMetadataDto = DataCiteMetadataDtoMapper.fromPublication(publication);
        return new Transformer(dataCiteMetadataDto).asXml();
    }
}
