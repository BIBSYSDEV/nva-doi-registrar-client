package no.unit.nva.doi.datacite.clients;

import static no.unit.nva.doi.datacite.clients.DataCiteClientErrorMessages.ERROR_COMMUNICATION_TEMPLATE;
import static no.unit.nva.doi.datacite.clients.DataCiteClientErrorMessages.ERROR_CREATING_DOI_TEMPLATE;
import static no.unit.nva.doi.datacite.clients.DataCiteClientErrorMessages.ERROR_DELETING_DOI_METADATA_TEMPLATE;
import static no.unit.nva.doi.datacite.clients.DataCiteClientErrorMessages.ERROR_DELETING_DOI_TEMPLATE;
import static no.unit.nva.doi.datacite.clients.DataCiteClientErrorMessages.ERROR_GETTING_DOI_TEMPLATE;
import static no.unit.nva.doi.datacite.clients.DataCiteClientErrorMessages.ERROR_SETTING_DOI_URL_TEMPLATE;
import static no.unit.nva.doi.datacite.clients.DataCiteClientErrorMessages.ERROR_UPDATING_METADATA_FOR_DOI_TEMPLATE;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.CreateDoiException;
import no.unit.nva.doi.datacite.clients.exception.DeleteDraftDoiException;
import no.unit.nva.doi.datacite.clients.exception.DeleteMetadataException;
import no.unit.nva.doi.datacite.clients.exception.GetDoiException;
import no.unit.nva.doi.datacite.clients.exception.SetLandingPageException;
import no.unit.nva.doi.datacite.clients.exception.UpdateMetadataException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.restclient.DataCiteRestConnection;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.datacite.restclient.models.DraftDoiDto;
import no.unit.nva.doi.models.Doi;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DoiClient implementation towards Registry Agency DataCite.
 *
 * <p>Notice in DataCite APIs, de-listed state is registered.
 *
 * @see DoiClient
 */
public class DataCiteClient implements DoiClient {

    protected static final String DOI_HOST = new Environment().readEnv("DOI_HOST");
    private static final Logger logger = LoggerFactory.getLogger(DataCiteClient.class);
    private final DataCiteConnectionFactory dataCiteApiConnectionFactory;
    private final DataCiteConfigurationFactory configFactory;

    public DataCiteClient(DataCiteConfigurationFactory configFactory,
                          DataCiteConnectionFactory connectionFactory

    ) {
        this.configFactory = configFactory;
        this.dataCiteApiConnectionFactory = connectionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Doi createDoi(URI customerId) throws ClientException {
        DataCiteMdsClientConfig customerConfigInfo = configFactory.getConfig(customerId);
        try {
            DataCiteRestConnection connection = prepareAuthenticatedDataCiteRestConnection(customerId);
            String doiPrefix = customerConfigInfo.getCustomerDoiPrefix();
            HttpResponse<String> response = sendDraftDoiRequest(connection, doiPrefix);
            logger.info("Responseee: {}", response);
            DraftDoiDto responseBody = DraftDoiDto.fromJson(response.body());

            return responseBody.toDoi().changeHost(DOI_HOST);
        } catch (IOException | InterruptedException e) {
            throw logAndCreateClientException("createDoi", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMetadata(URI customerId, Doi doi, String metadataDataCiteXml) throws ClientException {
        try {
            var response = prepareAuthenticatedMdsDataCiteConnection(customerId)
                .postMetadata(doi.toIdentifier(), metadataDataCiteXml);

            logger.info("Datacite response:" + response.body());
            if (isUnsuccessfulResponse(response)) {
                logger.error(ERROR_UPDATING_METADATA_FOR_DOI_TEMPLATE, doi.toIdentifier(), response.statusCode());
                throw new UpdateMetadataException(doi, response.statusCode());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw logAndCreateClientException("updateMetadata", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLandingPage(URI customerId, Doi doi, URI landingPage) throws ClientException {
        try {
            var response = prepareAuthenticatedMdsDataCiteConnection(customerId)
                .registerUrl(doi.toIdentifier(), landingPage.toASCIIString());
            if (isUnsuccessfulResponse(response)) {
                logger.error(ERROR_SETTING_DOI_URL_TEMPLATE, doi.toIdentifier(), response.statusCode());
                throw new SetLandingPageException(doi, response.statusCode());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw logAndCreateClientException("setLandingPage", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMetadata(URI customerId, Doi doi) throws ClientException {
        try {
            var response = prepareAuthenticatedMdsDataCiteConnection(customerId)
                .deleteMetadata(doi.toIdentifier());
            if (isUnsuccessfulResponse(response)) {
                logger.error(ERROR_DELETING_DOI_METADATA_TEMPLATE, doi.toIdentifier(), response.statusCode());
                throw new DeleteMetadataException(doi, response.statusCode());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw logAndCreateClientException("deleteMetadata", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDraftDoi(URI customerId, Doi doi) throws ClientException {
        try {
            var response = prepareAuthenticatedMdsDataCiteConnection(customerId)
                .deleteDoi(doi.toIdentifier());
            if (isUnsuccessfulResponse(response)) {
                logger.error(ERROR_DELETING_DOI_TEMPLATE, doi.toIdentifier(), response.statusCode());
                throw new DeleteDraftDoiException(doi, response.statusCode());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw logAndCreateClientException("deleteDraftDoi", e);
        }
    }

    @Override
    public DoiStateDto getDoi(URI customerId, Doi doi) throws ClientException {
        try {
            var response = prepareAuthenticatedDataCiteRestConnection(customerId)
                .getDoi(doi.toIdentifier());
            if (isUnsuccessfulResponse(response)) {
                logger.error(ERROR_GETTING_DOI_TEMPLATE, doi.toIdentifier(), response.statusCode());
                throw new GetDoiException(doi, response.statusCode());
            }
            return DoiStateDto.fromJson(response.body());
        } catch (IOException | InterruptedException e) {
            throw logAndCreateClientException("getDoi", e);
        }
    }

    private HttpResponse<String> sendDraftDoiRequest(DataCiteRestConnection connection, String prefix)
        throws IOException, InterruptedException, CreateDoiException {
        HttpResponse<String> response = connection.createDoi();
        if (isUnsuccessfulResponse(response)) {
            throw handleUnsuccessfulResponse(prefix, response);
        }
        return response;
    }

    private CreateDoiException handleUnsuccessfulResponse(String prefix, HttpResponse<String> response) {
        logger.error(ERROR_CREATING_DOI_TEMPLATE, prefix, response.statusCode(), response.body());
        return new CreateDoiException(prefix, response.statusCode(), response.body());
    }

    private DataCiteMdsConnection prepareAuthenticatedMdsDataCiteConnection(URI customerId) {
        return dataCiteApiConnectionFactory.getAuthenticatedMdsConnection(customerId);
    }

    private DataCiteRestConnection prepareAuthenticatedDataCiteRestConnection(URI customerId) {
        return dataCiteApiConnectionFactory.getAuthenticatedRestConnection(customerId);
    }

    private ClientException logAndCreateClientException(String doiClientMethodName, Exception parentException) {
        logger.error(ERROR_COMMUNICATION_TEMPLATE, doiClientMethodName);
        return new ClientException(doiClientMethodName, parentException);
    }

    private boolean isUnsuccessfulResponse(HttpResponse<String> response) {
        return response.statusCode() / 100 != 2;
    }
}
