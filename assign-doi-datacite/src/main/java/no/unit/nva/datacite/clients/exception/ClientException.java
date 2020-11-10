package no.unit.nva.datacite.clients.exception;

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
