package no.unit.nva.doi.datacite.restclient.models;

import static java.util.Arrays.stream;

public enum State {
    DRAFT("draft"), REGISTERED("registered"), FINDABLE("findable");

    public static final String UNKNOWN_DOI_STATE_MESSAGE = "Unknown doi state: ";
    private final String value;

    State(String value) {
        this.value = value;
    }

    public static State fromValue(String value) {
        return stream(values())
                   .filter(state -> state.getValue().equalsIgnoreCase(value))
                   .findAny()
                   .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_DOI_STATE_MESSAGE + value));
    }

    public String getValue() {
        return value;
    }
}
