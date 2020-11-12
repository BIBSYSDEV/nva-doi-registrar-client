package no.unit.nva.datacite.clients.exception;

import nva.commons.utils.JacocoGenerated;

/**
 * Wrapper exception thrown for all {@link no.unit.nva.datacite.clients.DoiClient} exceptions.
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
