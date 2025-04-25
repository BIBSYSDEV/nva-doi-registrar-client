package no.unit.nva.datacite.events;

import java.net.URI;

public record Resource(String identifier, URI doi, Publisher publisher) {

}
