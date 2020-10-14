package no.unit.nva.events.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class EventMessageParserTest {

    @Test
    public void parseThrowsRuntimeExceptionWhenParsingFails() {
        String invalidJson = "invalidJson";
        EventParser<SampleHandlerInput> eventParser = new EventParser<>(invalidJson);
        Executable action = () -> eventParser.parse(SampleHandlerInput.class);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertThat(exception.getCause(), is(instanceOf(JsonParseException.class)));
    }
}