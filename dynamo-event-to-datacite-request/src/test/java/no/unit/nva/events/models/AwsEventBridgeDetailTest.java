package no.unit.nva.events.models;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import no.unit.nva.events.examples.DataciteDoiRequest;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class AwsEventBridgeDetailTest {

    private final String detailJson = IoUtils.stringFromResources(Path.of("validEventBridgeDetailSample.json"));

    @Test
    public void objectMapperReturnsAwsEverBridgeDetailObjectForValidJson() throws JsonProcessingException {
        var detail = parseDetail();
        assertThat(detail, is(not(nullValue())));
        assertThat(detail, doesNotHaveNullOrEmptyFields());
    }

    @Test
    public void objectMapperSerialized() throws JsonProcessingException {
        var detail = parseDetail();
        assertThat(detail, is(not(nullValue())));
        assertThat(detail, doesNotHaveNullOrEmptyFields());
    }

    @Test
    public void copyCreatesEqualObject() throws JsonProcessingException {
        var original = parseDetail();
        var copy = original.copy().build();
        assertThat(copy, is(equalTo(original)));
        assertThat(copy, is(not(sameInstance(original))));
    }

    private AwsEventBridgeDetail<DataciteDoiRequest> parseDetail()
        throws JsonProcessingException {
        TypeReference<AwsEventBridgeDetail<DataciteDoiRequest>> detailTypeReference = new TypeReference<>() {};
        return JsonUtils.objectMapper.readValue(detailJson, detailTypeReference);
    }
}