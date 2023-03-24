package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretName;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.datacite.handlers.model.DoiResponse;
import no.unit.nva.datacite.handlers.model.UpdateDoiRequest;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.models.Doi;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

public class FindableDoiHandler extends ApiGatewayHandler<UpdateDoiRequest, DoiResponse> {

    public static final String CUSTOMER_ID_IS_MISSING_ERROR_MESSAGE = "Customer ID is missing";
    public static final String PUBLICATION_ID_IS_MISSING_ERROR_MESSAGE = "Publication ID is missing";
    private final DoiClient doiClient;
    private final DataCiteMetadataResolver dataCiteMetadataResolver;

    @JacocoGenerated
    public FindableDoiHandler() {
        this(defaultDoiClient(), new DataCiteMetadataResolver());
    }

    public FindableDoiHandler(DoiClient doiClient, DataCiteMetadataResolver dataCiteMetadataResolver) {
        super(UpdateDoiRequest.class);
        this.doiClient = doiClient;
        this.dataCiteMetadataResolver = dataCiteMetadataResolver;
    }

    @Override
    protected DoiResponse processInput(UpdateDoiRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        validateRequest(input);
        return attempt(() -> getDoi(input))
                   .map(doi -> makeDoiFindable(input, doi))
                   .orElseThrow();
    }

    @Override
    protected Integer getSuccessStatusCode(UpdateDoiRequest input, DoiResponse output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory dataCiteConfigurationFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory dataCiteMdsConnectionFactory =
            new DataCiteConnectionFactory(dataCiteConfigurationFactory);

        return new DataCiteClient(dataCiteConfigurationFactory, dataCiteMdsConnectionFactory);
    }

    private Doi getDoi(UpdateDoiRequest input) throws ClientException {
        var doi = input.getDoi();
        return nonNull(doi) ? Doi.fromUri(doi) : doiClient.createDoi(input.getCustomerId());
    }

    private DoiResponse makeDoiFindable(UpdateDoiRequest input, Doi doi) throws ClientException {
        String dataCiteXmlMetadata = dataCiteMetadataResolver.getDataCiteMetadataXml(input.getPublicationId());
        doiClient.updateMetadata(input.getCustomerId(), doi, dataCiteXmlMetadata);
        doiClient.setLandingPage(input.getCustomerId(), doi, input.getPublicationId());
        return new DoiResponse(doi.getUri());
    }

    private void validateRequest(UpdateDoiRequest input) throws BadRequestException {
        if (isNull(input.getPublicationId())) {
            throw new BadRequestException(PUBLICATION_ID_IS_MISSING_ERROR_MESSAGE);
        }
        if (isNull(input.getCustomerId())) {
            throw new BadRequestException(CUSTOMER_ID_IS_MISSING_ERROR_MESSAGE);
        }
    }
}
