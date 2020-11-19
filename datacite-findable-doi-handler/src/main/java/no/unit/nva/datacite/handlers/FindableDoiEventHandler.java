package no.unit.nva.datacite.handlers;

import static nva.commons.utils.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.datacite.model.DoiUpdateDto;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.DoiClientFactory;
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnectionFactory;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindableDoiEventHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateDto> {

    private static final Logger logger = LoggerFactory.getLogger(FindableDoiEventHandler.class);
    private final DoiClient doiClient;

    public FindableDoiEventHandler() {
        this(defaultDoiClient());
    }

    private static DoiClient defaultDoiClient() {
        String dataCiteConfigJson = AppEnv.getDataCiteConfig();
        DataCiteConfigurationFactory dataCiteConfigurationFactory = new DataCiteConfigurationFactory(
            IoUtils.stringToStream(dataCiteConfigJson));

        DataCiteMdsConnectionFactory dataCiteMdsConnectionFactory = new DataCiteMdsConnectionFactory(
            new PasswordAuthenticationFactory(dataCiteConfigurationFactory), AppEnv.getDataCiteHost(),
            AppEnv.getDataCitePort());

        return DoiClientFactory.getClient(dataCiteConfigurationFactory, dataCiteMdsConnectionFactory);
    }

    public FindableDoiEventHandler(DoiClient doiClient) {
        super(PublicationHolder.class);
        this.doiClient = doiClient;
    }

    @Override
    protected DoiUpdateDto processInputPayload(PublicationHolder input,
                                               AwsEventBridgeEvent<AwsEventBridgeDetail<PublicationHolder>> event,
                                               Context context) {


        String jsonString = attempt(() -> JsonUtils.objectMapper.writeValueAsString(event))
            .orElseThrow();
        logger.info(jsonString);

        

        return new DoiUpdateDto.Builder().withPublicationId(input.getItem().getId()).build();
    }
}
