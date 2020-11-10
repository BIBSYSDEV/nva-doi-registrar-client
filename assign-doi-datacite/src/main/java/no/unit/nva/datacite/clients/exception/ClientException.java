package no.unit.nva.datacite.clients.exception;

import nva.commons.utils.JacocoGenerated;

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
}
