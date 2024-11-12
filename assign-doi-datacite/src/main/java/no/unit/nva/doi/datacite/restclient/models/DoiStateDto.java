package no.unit.nva.doi.datacite.restclient.models;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.JsonNode;
import nva.commons.core.JacocoGenerated;

public class DoiStateDto {

    public static final String DATA_FIELD = "data";
    public static final String ATTRIBUTES_FIELD = "attributes";
    public static final String STATE = "state";
    public static final String DOI = "doi";

    private final String doi;
    private final State state;

    public DoiStateDto(String doi, State state) {
        this.doi = doi;
        this.state = state;
    }

    @JacocoGenerated
    public String getDoi() {
        return doi;
    }

    @JacocoGenerated
    public State getState() {
        return state;
    }

    /**
     * Create a DoiStateDto from a Json string.
     *
     * @param json a json object as it is expected and retuned from GET /dois/id endpoint in DataCite.
     * @return a DoiStateDto.
     */
    public static DoiStateDto fromJson(String json) {
        JsonNode tree = attempt(() -> dtoObjectMapper.readTree(json)).orElseThrow();
        JsonNode attributes = tree.path(DATA_FIELD).path(ATTRIBUTES_FIELD);

        String doi = attributes.path(DOI).textValue();
        var state = attributes.path(STATE).textValue();

        return new DoiStateDto(doi, State.fromValue(state));
    }
}
