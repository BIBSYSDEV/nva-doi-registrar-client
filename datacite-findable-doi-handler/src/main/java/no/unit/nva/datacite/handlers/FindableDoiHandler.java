package no.unit.nva.datacite.handlers;

import static java.util.Objects.isNull;
import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretKey;
import static no.unit.nva.datacite.handlers.FindableDoiAppEnv.getCustomerSecretsSecretName;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.datacite.handlers.model.DoiResponse;
import no.unit.nva.datacite.handlers.model.DoiUpdateRequest;
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

public class FindableDoiHandler extends ApiGatewayHandler<DoiUpdateRequest, DoiResponse> {

    public static final String CUSTOMER_ID_IS_MISSING_ERROR_MESSAGE = "Customer ID is missing";
    public static final String PUBLICATION_ID_IS_MISSING_ERROR_MESSAGE = "Publication ID is missing";
    private final DoiClient doiClient;
    private final DataCiteMetadataResolver dataCiteMetadataResolver;

    @JacocoGenerated
    public FindableDoiHandler() {
        this(defaultDoiClient(), new DataCiteMetadataResolver());
    }

    public FindableDoiHandler(DoiClient doiClient, DataCiteMetadataResolver dataCiteMetadataResolver) {
        super(DoiUpdateRequest.class);
        this.doiClient = doiClient;
        this.dataCiteMetadataResolver = dataCiteMetadataResolver;
    }

    @Override
    protected DoiResponse processInput(DoiUpdateRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        validateRequest(input);
        var doi = Doi.fromUri(input.getDoi());
        return attempt(() -> makeDoiFindable(input, doi)).orElseThrow();
    }

    @Override
    protected Integer getSuccessStatusCode(DoiUpdateRequest input, DoiResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {

        DataCiteConfigurationFactory dataCiteConfigurationFactory = new DataCiteConfigurationFactory(
            new SecretsReader(), getCustomerSecretsSecretName(), getCustomerSecretsSecretKey());

        DataCiteConnectionFactory dataCiteMdsConnectionFactory =
            new DataCiteConnectionFactory(dataCiteConfigurationFactory);

        return new DataCiteClient(dataCiteConfigurationFactory, dataCiteMdsConnectionFactory);
    }

    private DoiResponse makeDoiFindable(DoiUpdateRequest input, Doi doi) throws ClientException {
        String dataCiteXmlMetadata = dataCiteMetadataResolver.getDataCiteMetadataXml(input.getPublicationId());
        doiClient.updateMetadata(input.getCustomerId(), doi, dataCiteXmlMetadata);
        doiClient.setLandingPage(input.getCustomerId(), doi, input.getPublicationId());
        return new DoiResponse(doi.getUri());
    }

    private void validateRequest(DoiUpdateRequest input) throws BadRequestException {
        if (isNull(input.getPublicationId())) {
            throw new BadRequestException(PUBLICATION_ID_IS_MISSING_ERROR_MESSAGE);
        }
        if (isNull(input.getCustomerId())) {
            throw new BadRequestException(CUSTOMER_ID_IS_MISSING_ERROR_MESSAGE);
        }
    }
}
