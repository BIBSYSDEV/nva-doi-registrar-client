package no.unit.nva.datacite.handlers.doi;

import static no.unit.nva.datacite.handlers.resource.DeleteDraftDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.resource.DeleteDraftDoiAppEnv.getCustomerSecretsSecretName;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URI;
import no.unit.nva.datacite.handlers.model.DeleteDraftDoiRequest;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.models.Doi;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadMethodException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

public class DeleteDraftDoiHandler extends ApiGatewayHandler<DeleteDraftDoiRequest, Void> {

    public static final String BAD_DATACITE_RESPONSE_MESSAGE = "Bad response from DataCite fetching doi";
    protected static final String ERROR_DELETING_DRAFT_DOI = "Error deleting draft DOI";
    protected static final String NOT_DRAFT_DOI_ERROR = "DOI state is not draft, aborting deletion.";
    private static final String DOI_STATE_DRAFT = "draft";
    private final DoiClient doiClient;

    public DeleteDraftDoiHandler(DoiClient doiClient, Environment environment) {
        super(DeleteDraftDoiRequest.class, environment);
        this.doiClient = doiClient;
    }

    @JacocoGenerated
    public DeleteDraftDoiHandler() {
        this(defaultDoiClient(), new Environment());
    }

    @Override
    protected Void processInput(DeleteDraftDoiRequest input, RequestInfo requestInfo, Context context)
        throws BadGatewayException, BadMethodException {
        var customerId = input.getCustomerId();
        var doi = Doi.fromUri(input.getDoi());
        validateRequest(customerId, doi);
        return attempt(() -> deleteDraftDoi(customerId, doi))
                   .orElseThrow(failure -> new BadGatewayException(ERROR_DELETING_DRAFT_DOI));
    }

    @Override
    protected Integer getSuccessStatusCode(DeleteDraftDoiRequest input, Void output) {
        return HttpURLConnection.HTTP_ACCEPTED;
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory connectionFactory = new DataCiteConnectionFactory(configFactory);
        return new DataCiteClient(configFactory, connectionFactory);
    }

    private void validateRequest(URI customerId, Doi doi) throws BadMethodException, BadGatewayException {
        var doiState = attempt(() -> doiClient.getDoi(customerId, doi))
                           .orElseThrow(failure -> new BadGatewayException(BAD_DATACITE_RESPONSE_MESSAGE));
        if (!DOI_STATE_DRAFT.equalsIgnoreCase(doiState.getState())) {
            throw new BadMethodException(NOT_DRAFT_DOI_ERROR);
        }
    }

    private Void deleteDraftDoi(URI customerId, Doi draftDoi) {
        try {
            doiClient.deleteDraftDoi(customerId, draftDoi);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
