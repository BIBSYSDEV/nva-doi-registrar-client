package no.unit.nva.datacite.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.datacite.model.DoiUpdateDto;
import no.unit.nva.events.handlers.DestinationsEventBridgeEventHandler;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.PublicationHolder;

public class FindableDoiEventHandler extends DestinationsEventBridgeEventHandler<PublicationHolder, DoiUpdateDto> {

    protected FindableDoiEventHandler() {
        super(PublicationHolder.class);
    }

    @Override
    protected DoiUpdateDto processInputPayload(PublicationHolder input,
                                               AwsEventBridgeEvent<AwsEventBridgeDetail<PublicationHolder>> event,
                                               Context context) {
        return new DoiUpdateDto.Builder().withPublicationId(input.getItem().getId()).build();
    }
}
