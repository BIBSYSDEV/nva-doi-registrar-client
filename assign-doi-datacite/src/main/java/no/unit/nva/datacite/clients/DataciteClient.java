package no.unit.nva.datacite.clients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import no.unit.nva.datacite.clients.exception.ClientException;
import no.unit.nva.datacite.clients.exception.CreateDoiException;
import no.unit.nva.datacite.clients.exception.DeleteDraftDoiException;
import no.unit.nva.datacite.clients.exception.DeleteMetadataException;
import no.unit.nva.datacite.clients.exception.SetLandingPageException;
import no.unit.nva.datacite.clients.exception.UpdateMetadataException;
import no.unit.nva.datacite.clients.models.Doi;
import no.unit.nva.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.datacite.mdsclient.DataciteMdsConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DoiClient implementation towards Registry Agency Datacite.
 *
 * <p>Notice in Datacite APIs, de-listed state is registered.
 *
 * @see DoiClient
 */
public class DataciteClient implements DoiClient {

    public static final String HTTP_STATUS_LOG_TEMPLATE = " ({})";
    public static final String ERROR_CREATING_DOI = "Error creating new DOI with metadata";
    public static final String ERROR_UPDATING_METADATA_FOR_DOI = "Error updating metadata for DOI";
    public static final String ERROR_SETTING_DOI_URL = "Error setting DOI url: ";
    public static final String ERROR_DELETING_DOI_METADATA = "Error deleting DOI metadata";
    public static final String ERROR_DELETING_DOI = "Error deleting DOI: ";
    public static final String ERROR_COMMUNICATION_TEMPLATE = "Error during API communication: ({})";
    public static final String COLON_SPACE = ": ";
    protected static final String CHARACTER_PARENTHESES_START = "(";
    protected static final String CHARACTER_PARENTHESES_STOP = ")";
    protected static final String CHARACTER_WHITESPACE = " ";
    protected static final char FORWARD_SLASH = '/';
    private static final String PREFIX_TEMPLATE_ENTRY = "{}";
    public static final String ERROR_DELETING_DOI_TEMPLATE =
        ERROR_DELETING_DOI
            + COLON_SPACE
            + PREFIX_TEMPLATE_ENTRY
            + HTTP_STATUS_LOG_TEMPLATE;
    public static final String ERROR_DELETING_DOI_METADATA_TEMPLATE =
        ERROR_DELETING_DOI_METADATA
            + COLON_SPACE
            + PREFIX_TEMPLATE_ENTRY
            + HTTP_STATUS_LOG_TEMPLATE;
    public static final String ERROR_SETTING_DOI_URL_TEMPLATE =
        ERROR_SETTING_DOI_URL
            + COLON_SPACE
            + PREFIX_TEMPLATE_ENTRY
            + HTTP_STATUS_LOG_TEMPLATE;
    public static final String ERROR_UPDATING_METADATA_FOR_DOI_TEMPLATE =
        ERROR_UPDATING_METADATA_FOR_DOI
            + COLON_SPACE
            + PREFIX_TEMPLATE_ENTRY
            + HTTP_STATUS_LOG_TEMPLATE;
    public static final String ERROR_CREATING_DOI_TEMPLATE =
        ERROR_CREATING_DOI
            + PREFIX_TEMPLATE_ENTRY
            + HTTP_STATUS_LOG_TEMPLATE;
    private static final Logger logger = LoggerFactory.getLogger(DataciteClient.class);
    private final DataciteMdsConnectionFactory mdsConnectionFactory;
    private final DataciteConfigurationFactory configFactory;

    public DataciteClient(DataciteConfigurationFactory configFactory,
                          DataciteMdsConnectionFactory mdsConnectionFactory) {
        this.configFactory = configFactory;
        this.mdsConnectionFactory = mdsConnectionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Doi createDoi(String customerId, String metadataDataciteXml) throws ClientException {
        var prefix = configFactory.getConfig(customerId).getInstitutionPrefix();
        try {
            var response = prepareAuthenticatedDataciteConnection(customerId)
                .postMetadata(prefix, metadataDataciteXml);
            if (isUnsuccessfulResponse(response)) {
                logger.error(ERROR_CREATING_DOI_TEMPLATE, prefix, response.statusCode());
                throw new CreateDoiException(prefix, response.statusCode());
            }
            String createMetadataResponseBody = response.body();
            var doi = extractDoiPrefixAndSuffix(createMetadataResponseBody);
            return doi;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw logAndCreateClientException("createDoi", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMetadata(String customerId, Doi doi, String metadataDataciteXml) throws ClientException {
        try {
            var response = prepareAuthenticatedDataciteConnection(customerId)
                .postMetadata(doi.toIdentifier(), metadataDataciteXml);
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
    public void setLandingPage(String customerId, Doi doi, URI landingPage) throws ClientException {
        try {
            var response = prepareAuthenticatedDataciteConnection(customerId)
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
    public void deleteMetadata(String customerId, Doi doi) throws ClientException {
        try {
            var response = prepareAuthenticatedDataciteConnection(customerId)
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
    public void deleteDraftDoi(String customerId, Doi doi) throws ClientException {
        try {
            var response = prepareAuthenticatedDataciteConnection(customerId)
                .deleteDoi(doi.toIdentifier());
            if (isUnsuccessfulResponse(response)) {
                logger.error(ERROR_DELETING_DOI_TEMPLATE, doi.toIdentifier(), response.statusCode());
                throw new DeleteDraftDoiException(doi, response.statusCode());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw logAndCreateClientException("deleteDraftDoi", e);
        }
    }

    private DataCiteMdsConnection prepareAuthenticatedDataciteConnection(String customerId) {
        return mdsConnectionFactory.getAuthenticatedConnection(customerId);
    }

    private ClientException logAndCreateClientException(String doiClientMethodName, Exception parentException) {
        logger.error(ERROR_COMMUNICATION_TEMPLATE, doiClientMethodName);
        return new ClientException(doiClientMethodName, parentException);
    }

    private Doi extractDoiPrefixAndSuffix(String createMetadataResponseBody) {
        var identifier = StringUtils.substringBetween(createMetadataResponseBody,
            CHARACTER_PARENTHESES_START,
            CHARACTER_PARENTHESES_STOP);
        return Doi.builder().identifier(identifier).build();
    }

    private boolean isUnsuccessfulResponse(HttpResponse<String> response) {
        return response.statusCode() / 100 != 2;
    }
}
