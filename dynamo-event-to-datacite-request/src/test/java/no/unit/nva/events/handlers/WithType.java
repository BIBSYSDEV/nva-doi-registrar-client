package no.unit.nva.events.handlers;

public interface WithType {

    default String getType() {
        return this.getClass().getName();
    }

    default void setType() {
        // do nothing;
    }
}
