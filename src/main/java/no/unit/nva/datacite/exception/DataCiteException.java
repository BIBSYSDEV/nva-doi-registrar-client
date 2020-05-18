package no.unit.nva.datacite.exception;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class DataCiteException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpStatus.SC_INTERNAL_SERVER_ERROR;

    public DataCiteException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }

}
