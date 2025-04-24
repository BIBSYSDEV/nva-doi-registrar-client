package no.unit.nva.datacite.events;

import static java.util.Objects.nonNull;

public record ResourceUpdateEvent(String action, Resource oldData, Resource newData) {

    private static final String REMOVE_ACTION = "REMOVE";

    boolean isDeletionOfResourceWithDoi() {
        if (!REMOVE_ACTION.equals(action)) {
            return false;
        }

        return nonNull(oldData()) && nonNull(oldData().doi());
    }
}
