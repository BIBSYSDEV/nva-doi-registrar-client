package no.unit.nva.doi.exception;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class MissingParametersException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpStatus.SC_BAD_REQUEST;

    public MissingParametersException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }

}
