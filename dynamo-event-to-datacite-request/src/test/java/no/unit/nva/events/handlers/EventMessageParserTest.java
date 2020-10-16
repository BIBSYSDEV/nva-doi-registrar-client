package no.unit.nva.events.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.utils.JsonUtils;
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

    @Test
    public void parseParsesCorrectlyNestedGenericTypes() throws JsonProcessingException {
        ClassA<ClassB<ClassC<String>>> expectedDetail = createdNestedGenericsObject();
        AwsEventBridgeEvent<ClassA<ClassB<ClassC<String>>>> event = createEventWithDetail(expectedDetail);

        String eventJson = JsonUtils.objectMapper.writeValueAsString(event);

        EventParser<ClassA<ClassB<ClassC<String>>>> parser = new EventParser<>(eventJson);

        AwsEventBridgeEvent<ClassA<ClassB<ClassC<String>>>> parsedEvent =
            parser.parse(ClassA.class, ClassB.class, ClassC.class, String.class);

        assertThat(parsedEvent.getDetail(), is(equalTo(expectedDetail)));
    }

    private AwsEventBridgeEvent<ClassA<ClassB<ClassC<String>>>> createEventWithDetail(
        ClassA<ClassB<ClassC<String>>> expectedDetail) {
        AwsEventBridgeEvent<ClassA<ClassB<ClassC<String>>>> event = new AwsEventBridgeEvent<>();
        event.setDetail(expectedDetail);
        event.setAccount("someAccount");
        event.setId("SomeId");
        return event;
    }

    private ClassA<ClassB<ClassC<String>>> createdNestedGenericsObject() {
        ClassC<String> bottom = new ClassC<>();
        bottom.setFieldC("Hello");
        ClassB<ClassC<String>> middle = new ClassB<>();
        middle.setFieldB(bottom);
        ClassA<ClassB<ClassC<String>>> top = new ClassA<>();
        top.setFieldA(middle);
        return top;
    }

    private static class ClassA<InputType> implements WithType {

        private InputType fieldA;

        public InputType getFieldA() {
            return fieldA;
        }

        public void setFieldA(InputType fieldA) {
            this.fieldA = fieldA;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClassA<?> classA = (ClassA<?>) o;
            return Objects.equals(getFieldA(), classA.getFieldA());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFieldA());
        }
    }

    private static class ClassB<InputType> implements WithType {

        private InputType fieldB;

        public InputType getFieldB() {
            return fieldB;
        }

        public void setFieldB(InputType fieldB) {
            this.fieldB = fieldB;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClassB<?> classB = (ClassB<?>) o;
            return Objects.equals(getFieldB(), classB.getFieldB());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFieldB());
        }
    }

    private static class ClassC<InputType> implements WithType {

        private InputType fieldC;

        public InputType getFieldC() {
            return fieldC;
        }

        public void setFieldC(InputType fieldC) {
            this.fieldC = fieldC;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClassC<?> classC = (ClassC<?>) o;
            return Objects.equals(getFieldC(), classC.getFieldC());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFieldC());
        }
    }
}