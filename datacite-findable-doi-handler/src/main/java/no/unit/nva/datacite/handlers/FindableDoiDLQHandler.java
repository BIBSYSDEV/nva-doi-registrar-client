package no.unit.nva.datacite.handlers;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindableDoiDLQHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(FindableDoiDLQHandler.class);

    @JacocoGenerated
    public FindableDoiDLQHandler() {

    }

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        logger.info("Received input: {}", getJsonString(input));
        return null;
    }

    private String getJsonString(SQSEvent input) {
        return attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(input)).orElseThrow();
    }
}
