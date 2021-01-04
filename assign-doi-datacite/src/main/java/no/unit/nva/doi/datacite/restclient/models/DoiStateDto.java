package no.unit.nva.doi.datacite.restclient.models;

import com.fasterxml.jackson.databind.JsonNode;
import nva.commons.utils.JacocoGenerated;

import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;

public class DoiStateDto {

    public static final String DATA_FIELD = "data";
    public static final String ATTRIBUTES_FIELD = "attributes";
    public static final String STATE = "state";
    public static final String DOI = "doi";

    private final String doi;
    private final String state;

    public DoiStateDto(String doi, String state) {
        this.doi = doi;
        this.state = state;
    }

    @JacocoGenerated
    public String getDoi() {
        return doi;
    }

    @JacocoGenerated
    public String getState() {
        return state;
    }

    /**
     * Create a DoiStateDto from a Json string.
     *
     * @param json a json object as it is expected and retuned from GET /dois/id endpoint in DataCite.
     * @return a DoiStateDto.
     */
    public static DoiStateDto fromJson(String json) {
        JsonNode tree = attempt(() -> objectMapper.readTree(json)).orElseThrow();
        JsonNode attributes = tree.path(DATA_FIELD).path(ATTRIBUTES_FIELD);

        String doi = attributes.path(DOI).textValue();
        String state = attributes.path(STATE).textValue();

        return new DoiStateDto(doi, state);
    }
}
