package no.unit.nva.doi.model;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class DoiUpdateDtoTest {

    private final ObjectMapper objectMapper = JsonUtils.objectMapper;

    @Test
    public void canWriteDoiUpdateDtoToJsonAndBack() throws JsonProcessingException {
        DoiUpdateDto doiUpdateDto = new DoiUpdateDto.Builder()
            .withDoi("http://sample.doi")
            .withModifiedDate(Instant.now())
            .withPublicationId(URI.create("http://sample.publication.id"))
            .build();

        String json = objectMapper.writeValueAsString(doiUpdateDto);
        DoiUpdateDto parsedDoiUpdateDto = objectMapper.readValue(json, DoiUpdateDto.class);

        assertThat(doiUpdateDto, doesNotHaveNullOrEmptyFields());
    }



}
