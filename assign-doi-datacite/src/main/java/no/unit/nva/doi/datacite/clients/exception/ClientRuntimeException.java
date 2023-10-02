package no.unit.nva.doi.datacite.clients.exception;

import no.unit.nva.doi.DoiClient;
import nva.commons.core.JacocoGenerated;

/**
 * Wrapper exception thrown for all {@link DoiClient} runtime exceptions.
 */
@JacocoGenerated
public class ClientRuntimeException extends RuntimeException {

    public ClientRuntimeException(ClientException e) {
        super(e);
    }
}
