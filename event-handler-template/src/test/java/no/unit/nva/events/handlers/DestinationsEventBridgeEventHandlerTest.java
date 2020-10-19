package no.unit.nva.events.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.stubs.FakeContext;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DestinationsEventBridgeEventHandlerTest {

    public static final String VALID_AWS_EVENT_BRIDGE_EVENT = IoUtils.stringFromResources(
        Path.of("validAwsEventBridgeEvent.json"));
    private static final JsonPointer RESPONSE_PAYLOAD_POINTER = JsonPointer.compile("/detail/responsePayload");

    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void init() {
        this.outputStream = new ByteArrayOutputStream();
        this.context = new FakeContext();
    }

    @Test
    public void handleRequestAcceptsValidEvent() throws JsonProcessingException {
        DestinationsHandlerTestClass handler = new DestinationsHandlerTestClass();
        handler.handleRequest(IoUtils.stringToStream(VALID_AWS_EVENT_BRIDGE_EVENT), outputStream, context);
        SampleHandlerInput expectedInput = parseInput();
        assertThat(handler.inputBuffer.get(), is(equalTo(expectedInput)));
    }

    private SampleHandlerInput parseInput() throws JsonProcessingException {
        JsonNode tree = JsonUtils.objectMapper.readTree(VALID_AWS_EVENT_BRIDGE_EVENT);
        JsonNode inputNode = tree.at(RESPONSE_PAYLOAD_POINTER);
        SampleHandlerInput input = JsonUtils.objectMapper.convertValue(inputNode, SampleHandlerInput.class);
        return input;
    }

    private static class DestinationsHandlerTestClass
        extends DestinationsEventBridgeEventHandler<SampleHandlerInput, Void> {

        private final AtomicReference<SampleHandlerInput> inputBuffer = new AtomicReference<>();
        private final AtomicReference<AwsEventBridgeEvent<AwsEventBridgeDetail<SampleHandlerInput>>> eventBuffer =
            new AtomicReference<>();

        protected DestinationsHandlerTestClass() {
            super(SampleHandlerInput.class);
        }

        @Override
        protected Void processInputPayload(SampleHandlerInput input,
                                           AwsEventBridgeEvent<AwsEventBridgeDetail<SampleHandlerInput>> event,
                                           Context context) {
            this.inputBuffer.set(input);
            this.eventBuffer.set(event);
            return null;
        }
    }
}