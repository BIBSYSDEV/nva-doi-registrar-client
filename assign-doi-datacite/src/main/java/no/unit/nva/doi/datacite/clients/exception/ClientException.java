package no.unit.nva.doi.datacite.clients.exception;

import no.unit.nva.doi.DoiClient;
import nva.commons.core.JacocoGenerated;

/**
 * Wrapper exception thrown for all {@link DoiClient} exceptions.
 */
@JacocoGenerated
public class ClientException extends Exception {

    public ClientException(String message) {
        super(message);
    }

    public ClientException(Exception e) {
        super(e);
    }

    public ClientException(String message, Exception e) {
        super(message, e);
    }

    public ClientException() {
        super();
    }
}
