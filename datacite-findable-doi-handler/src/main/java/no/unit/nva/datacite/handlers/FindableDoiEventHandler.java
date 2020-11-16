package no.unit.nva.datacite.handlers;

import static nva.commons.utils.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.datacite.model.DoiUpdateDto;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import nva.commons.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindableDoiEventHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateDto> {

    private static final Logger logger = LoggerFactory.getLogger(FindableDoiEventHandler.class);

    public FindableDoiEventHandler() {
        super(PublicationHolder.class);
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
