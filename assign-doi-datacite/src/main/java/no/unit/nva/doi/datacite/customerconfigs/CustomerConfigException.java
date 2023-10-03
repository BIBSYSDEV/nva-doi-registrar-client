package no.unit.nva.doi.datacite.customerconfigs;

import no.unit.nva.doi.datacite.clients.exception.ClientException;

public class CustomerConfigException extends ClientException {

    public CustomerConfigException(Exception exception) {
        super(exception);
    }

    public CustomerConfigException() {
        super();
    }

    public CustomerConfigException(String message) {
        super(message);
    }
}
