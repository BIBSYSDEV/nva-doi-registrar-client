package no.unit.nva.datacite.handlers.doi;

import static com.amazonaws.util.SdkHttpUtils.urlDecode;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URI;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClientV2;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadMethodException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDraftDoiHandler extends ApiGatewayHandler<Void, Void> {

    public static final String BAD_DATACITE_RESPONSE_MESSAGE = "Bad response from DataCite fetching doi";
    public static final String CUSTOMER_ID = "customerId";
    protected static final String ERROR_DELETING_DRAFT_DOI = "Error deleting draft DOI";
    protected static final String NOT_DRAFT_DOI_ERROR = "DOI state is not draft, aborting deletion.";
    private static final String DOI_STATE_DRAFT = "draft";
    private final Logger logger = LoggerFactory.getLogger(DeleteDraftDoiHandler.class);
    private final DoiClient doiClient;

    public DeleteDraftDoiHandler(DoiClient doiClient, Environment environment) {
        super(Void.class, environment);
        this.doiClient = doiClient;
    }

    @JacocoGenerated
    public DeleteDraftDoiHandler() {
        this(defaultDoiClient(), new Environment());
    }

    @Override
    protected Void processInput(Void input, RequestInfo requestInfo, Context context)
        throws BadGatewayException, BadMethodException, BadRequestException {
        var customerId = URI.create(urlDecode(requestInfo.getQueryParameter(CUSTOMER_ID)));
        var doi = getDoiFromPath(requestInfo);
        validateRequest(customerId, doi);
        return attempt(() -> deleteDraftDoi(customerId, doi))
                   .orElseThrow(failure -> handleFailure(failure.getException(), ERROR_DELETING_DRAFT_DOI));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Void output) {
        return HttpURLConnection.HTTP_ACCEPTED;
    }

    private static Doi getDoiFromPath(RequestInfo requestInfo) {
        return Doi.fromUriString(
            requestInfo.getPathParameter("doiPrefix") + "/" + requestInfo.getPathParameter("doiSuffix"));
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        return new DataCiteClientV2();
    }

    private BadGatewayException handleFailure(Exception exception, String message) {
        logger.error("Delete draft doi failed with {}", exception);
        return new BadGatewayException(message);
    }

    private void validateRequest(URI customerId, Doi doi) throws BadMethodException, BadGatewayException {
        var doiState = attempt(() -> doiClient.getDoi(customerId, doi))
                           .orElseThrow(failure ->
                                            handleFailure(failure.getException(), BAD_DATACITE_RESPONSE_MESSAGE));
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
