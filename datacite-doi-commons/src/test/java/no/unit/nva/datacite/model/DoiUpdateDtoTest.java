package no.unit.nva.datacite.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;

public class DoiUpdateDtoTest {

    private final ObjectMapper objectMapper = JsonUtils.objectMapper;

    @Test
    public void test() throws JsonProcessingException {
        DoiUpdateDto doiUpdateDto = new DoiUpdateDto.Builder()
            .withDoi("http://sample.doi")
            .withModifiedDate(Instant.now())
            .withPublicationId(URI.create("http://sample.publication.id"))
            .build();

        String json = objectMapper.writeValueAsString(doiUpdateDto);
        DoiUpdateDto parsedDoiUpdateDto = objectMapper.readValue(json, DoiUpdateDto.class);

        assertEquals(doiUpdateDto.getDoi(), parsedDoiUpdateDto.getDoi());
        assertEquals(doiUpdateDto.getModifiedDate(), parsedDoiUpdateDto.getModifiedDate());
        assertEquals(doiUpdateDto.getPublicationId(), parsedDoiUpdateDto.getPublicationId());
    }



}
