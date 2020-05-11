package no.unit.nva.datacite;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateDoiResponse {

    private final String doi;


    /**
     * Creates a response containing created doi (prefix/suffix).
     *
     * @param doi doi
     */
    @JsonCreator
    public CreateDoiResponse(@JsonProperty("doi") String doi) {
        this.doi = doi;
    }

    public String getDoi() {
        return doi;
    }

}
