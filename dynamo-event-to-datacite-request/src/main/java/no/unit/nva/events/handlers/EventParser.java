package no.unit.nva.events.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.StringUtils.stackTraceInSingleLine;
import static nva.commons.utils.attempt.Try.attempt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.utils.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventParser<I> {

    public static final String ERROR_PARSING_INPUT = "Could not parse input: ";
    public static final int SKIP_BOTTOM_TYPE = 2;
    private static final Logger logger = LoggerFactory.getLogger(EventParser.class);
    public static final String RAWTYPES = "rawtypes";
    private final String input;

    public EventParser(String input) {
        this.input = input;
    }

    public AwsEventBridgeEvent<I> parse(Class<I> iclass) {
        return attempt(() -> parseJson(iclass)).orElseThrow(this::handleParsingError);
    }

    @SuppressWarnings(RAWTYPES)
    public AwsEventBridgeEvent parse(Class... parameterClasses) {
        return attempt(() -> parseJson(parameterClasses)).orElseThrow(this::handleParsingError);
    }

    private AwsEventBridgeEvent<I> parseJson(Class<I> iclass) throws JsonProcessingException {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(AwsEventBridgeEvent.class, iclass);
        return objectMapper.readValue(input, javaType);
    }

    @SuppressWarnings(RAWTYPES)
    private AwsEventBridgeEvent parseJson(Class... nestedClasses)
        throws JsonProcessingException {
        JavaType nestedJavaTypes = nestedClassesToJavaType(nestedClasses);
        JavaType eventBridgeJavaType = constuctParametricType(AwsEventBridgeEvent.class, nestedJavaTypes);
        return objectMapper.readValue(input, eventBridgeJavaType);
    }

    private <S> RuntimeException handleParsingError(Failure<S> fail) {
        logger.error(ERROR_PARSING_INPUT + input);
        logger.error(stackTraceInSingleLine(fail.getException()));
        return new RuntimeException(fail.getException());
    }

    /*
     * Given an array of {@link Class} generic classes ClassA,ClassB,ClassC,...ClassZ,
     * it creates a {@link JavaType} for the object  ClassA<ClassB<ClassC...<ClassZ>>>>
     */
    @SuppressWarnings(RAWTYPES)
    private JavaType nestedClassesToJavaType(Class[] classes) {
        JavaType bottomType = constructNonParametricType(classes[classes.length - 1]);
        JavaType mostRecentType = bottomType;
        for (int index = classes.length - SKIP_BOTTOM_TYPE; index >= 0; index--) {
            Class currentClass = classes[index];
            JavaType newType = constuctParametricType(currentClass, mostRecentType);
            mostRecentType = newType;
        }
        return mostRecentType;
    }

    @SuppressWarnings(RAWTYPES)
    private JavaType constuctParametricType(Class currentClass, JavaType mostRecentType) {
        return objectMapper.getTypeFactory()
            .constructParametricType(currentClass, mostRecentType);
    }

    @SuppressWarnings(RAWTYPES)
    private JavaType constructNonParametricType(Class nonParametricClass) {
        return objectMapper.getTypeFactory().constructType(nonParametricClass);
    }
}
