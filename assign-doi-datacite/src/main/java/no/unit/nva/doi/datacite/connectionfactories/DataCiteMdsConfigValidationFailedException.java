package no.unit.nva.doi.datacite.connectionfactories;

import no.unit.nva.doi.datacite.clients.exception.ClientException;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class DataCiteMdsConfigValidationFailedException extends ClientException {

    public DataCiteMdsConfigValidationFailedException(String message) {
        super(message);
    }

    public DataCiteMdsConfigValidationFailedException(Exception e) {
        super(e);
    }

    public DataCiteMdsConfigValidationFailedException(String message, Exception e) {
        super(message, e);
    }

    public DataCiteMdsConfigValidationFailedException() {
        super();
    }
}
