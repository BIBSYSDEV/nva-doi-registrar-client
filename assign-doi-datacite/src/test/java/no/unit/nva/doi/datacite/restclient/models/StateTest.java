package no.unit.nva.doi.datacite.restclient.models;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class StateTest {

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStateIsUnknown() {
        var state = "unknown";
        assertThrows(IllegalArgumentException.class, () -> State.fromValue(state), "Unknown doi state: " + state);
    }
}