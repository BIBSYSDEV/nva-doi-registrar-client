package no.unit.nva.datacite.handlers;

import static no.unit.nva.datacite.handlers.DraftDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.DraftDoiAppEnv.getCustomerSecretsSecretName;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.datacite.model.DoiResponse;
import no.unit.nva.datacite.model.ReserveDoiRequest;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReserveDraftDoiHandler extends ApiGatewayHandler<ReserveDoiRequest, DoiResponse> {

    public static final String BAD_RESPONSE_FROM_DATA_CITE = "Bad response from DataCite";
    private final DoiClient doiClient;

    private static final Logger logger = LoggerFactory.getLogger(ReserveDraftDoiHandler.class);

    public ReserveDraftDoiHandler(DoiClient doiClient, Environment environment) {
        super(ReserveDoiRequest.class, environment);
        this.doiClient = doiClient;
    }

    @JacocoGenerated
    public ReserveDraftDoiHandler() {
        this(defaultDoiClient(), new Environment());
    }

    @Override
    protected DoiResponse processInput(ReserveDoiRequest input, RequestInfo requestInfo, Context context)
        throws BadGatewayException {
        logger.info("Input " + input.getCustomer());
        var customerId = input.getCustomer();
        return attempt(() -> doiClient.createDoi(customerId))
                   .map(doi -> new DoiResponse(doi.getUri()))
                   .orElseThrow(failure -> new BadGatewayException(BAD_RESPONSE_FROM_DATA_CITE));
    }

    @Override
    protected Integer getSuccessStatusCode(ReserveDoiRequest input, DoiResponse output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory configFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory connectionFactory = new DataCiteConnectionFactory(configFactory);
        return new DataCiteClient(configFactory, connectionFactory);
    }
}
