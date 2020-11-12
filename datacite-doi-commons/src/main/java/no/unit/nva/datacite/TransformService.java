package no.unit.nva.datacite;

import no.unit.nva.publication.doi.dto.Publication;

public interface TransformService {

    String getXml(Publication publication);
}