package no.unit.nva.datacite.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import nva.commons.core.JacocoGenerated;

public class FindableDoiDLQHandler implements RequestHandler<SQSEvent, Void> {

    @JacocoGenerated
    public FindableDoiDLQHandler() {

    }

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        return null;
    }
}
