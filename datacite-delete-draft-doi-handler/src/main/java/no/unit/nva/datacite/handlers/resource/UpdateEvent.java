package no.unit.nva.datacite.handlers.resource;

public record UpdateEvent(String action, DoiEntity oldData, DoiEntity newData) {}
