package no.unit.nva.events.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.stubs.FakeContext;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class EventHandlerTest {

    public static final String AWS_EVENT_BRIDGE_EVENT =
        IoUtils.stringFromResources(Path.of("validEventBridgeEvent.json"));
    public static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";
    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void init() {
        this.outputStream = new ByteArrayOutputStream();
        this.context = new FakeContext();
    }

    @Test
    public void handleRequestAcceptsValidEvent() throws JsonProcessingException {
        EventHandlerTestClass handler = new EventHandlerTestClass();
        handler.handleRequest(sampleInputStream(), outputStream, context);
        AwsEventBridgeEvent<SampleHandlerInput> expectedEvent = parseEvent();
        assertThat(handler.eventBuffer.get(), is(equalTo(expectedEvent)));
    }

    private InputStream sampleInputStream() {
        return IoUtils.stringToStream(AWS_EVENT_BRIDGE_EVENT);
    }

    @Test
    public void handleRequestLogsErrorWhenExceptionIsThrown() {
        TestAppender appender = LogUtils.getTestingAppender(EventHandler.class);
        var handler = new EventHandlerThrowingException();
        Executable action = () -> handler.handleRequest(sampleInputStream(), outputStream, context);
        assertThrows(RuntimeException.class, action);
        assertThat(appender.getMessages(), containsString(EXCEPTION_MESSAGE));
    }

    @Test
    public void handleRequestReThrowsExceptionWhenExceptionIsThrown() {
        var handler = new EventHandlerThrowingException();
        Executable action = () -> handler.handleRequest(sampleInputStream(), outputStream, context);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getMessage(), is(equalTo(EXCEPTION_MESSAGE)));
    }

    private AwsEventBridgeEvent<SampleHandlerInput> parseEvent() throws JsonProcessingException {
        JavaType javatype = JsonUtils.objectMapper.getTypeFactory()
            .constructParametricType(AwsEventBridgeEvent.class, SampleHandlerInput.class);
        return JsonUtils.objectMapper.readValue(AWS_EVENT_BRIDGE_EVENT, javatype);
    }

    private static class EventHandlerTestClass extends EventHandler<SampleHandlerInput, SampleHandlerInput> {

        private AtomicReference<AwsEventBridgeEvent<SampleHandlerInput>> eventBuffer = new AtomicReference<>();
        private AtomicReference<SampleHandlerInput> inputBuffer = new AtomicReference<>();

        protected EventHandlerTestClass() {
            super(SampleHandlerInput.class);
        }

        @Override
        protected SampleHandlerInput processInput(SampleHandlerInput input,
                                                  AwsEventBridgeEvent<SampleHandlerInput> event,
                                                  Context context) {
            eventBuffer.set(event);
            inputBuffer.set(input);

            return input;
        }
    }

    private static class EventHandlerThrowingException extends EventHandler<SampleHandlerInput, Void> {

        protected EventHandlerThrowingException() {
            super(SampleHandlerInput.class);
        }

        @Override
        protected Void processInput(SampleHandlerInput input, AwsEventBridgeEvent<SampleHandlerInput> event,
                                    Context context) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
    }
}