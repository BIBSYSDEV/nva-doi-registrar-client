package no.unit.nva.doi.exception;

import nva.commons.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class InstitutionIdUnknownException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpStatus.SC_PAYMENT_REQUIRED;

    public InstitutionIdUnknownException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }

}
