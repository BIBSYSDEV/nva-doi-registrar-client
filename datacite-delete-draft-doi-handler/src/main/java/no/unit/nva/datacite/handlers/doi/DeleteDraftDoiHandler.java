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
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.Doi;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

public class DeleteDraftDoiHandler extends ApiGatewayHandler<DeleteDraftDoiRequest, Void> {

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
        throws  BadGatewayException {
        var customerId = input.getCustomerId();
        var doi = Doi.fromUri(input.getDoi());

        return attempt(() -> doiClient.getDoi(customerId, doi))
            .map(this::validateState)
            .map(action -> deleteDraftDoi(customerId, doi))
            .orElseThrow(failure -> new BadGatewayException(ERROR_DELETING_DRAFT_DOI));
    }

    private String validateState(DoiStateDto doi) throws BadRequestException {
        if (!DOI_STATE_DRAFT.equalsIgnoreCase(doi.getState())) {
            throw new BadRequestException(NOT_DRAFT_DOI_ERROR);
        }
        return doi.getDoi();
    }

    private Void deleteDraftDoi(URI customerId, Doi draftDoi) {
        try {
            doiClient.deleteDraftDoi(customerId, draftDoi);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        return null;
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

}
