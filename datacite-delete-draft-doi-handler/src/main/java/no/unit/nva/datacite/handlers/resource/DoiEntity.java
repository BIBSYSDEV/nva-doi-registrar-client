package no.unit.nva.datacite.handlers.resource;

import java.net.URI;

public record DoiEntity(String identifier, URI doi, Publisher publisher) {}
