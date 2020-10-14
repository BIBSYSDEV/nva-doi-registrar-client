package no.unit.nva.events.models;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import no.unit.nva.events.examples.DataciteDoiRequest;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class AwsEventBridgeEventTest {

    private final String detailJson = IoUtils.stringFromResources(Path.of("validEventBridgeEvent.json"));

    @Test
    public void objectMapperReturnsAwsEverBridgeDetailObjectForValidJson() throws JsonProcessingException {
        var event = parseEvent();
        assertThat(event, is(not(nullValue())));
        assertThat(event, doesNotHaveNullOrEmptyFields());
    }

    @Test
    public void objectMapperSerialized() throws JsonProcessingException {
        var event = parseEvent();
        assertThat(event, is(not(nullValue())));
        assertThat(event, doesNotHaveNullOrEmptyFields());
    }

    private AwsEventBridgeEvent<AwsEventBridgeDetail<DataciteDoiRequest>> parseEvent()
        throws JsonProcessingException {
        TypeReference<AwsEventBridgeEvent<AwsEventBridgeDetail<DataciteDoiRequest>>> detailTypeReference =
            new TypeReference<>() {};
        return JsonUtils.objectMapper.readValue(detailJson, detailTypeReference);
    }
}