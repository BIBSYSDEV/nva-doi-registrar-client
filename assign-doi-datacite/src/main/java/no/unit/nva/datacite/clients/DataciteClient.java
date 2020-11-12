package no.unit.nva.datacite.clients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import no.unit.nva.datacite.clients.exception.ClientException;
import no.unit.nva.datacite.clients.models.Doi;
import no.unit.nva.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.datacite.mdsclient.DataciteMdsConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DoiClient implementation towards Registry Agency Datacite.
 */
public class DataciteClient implements DoiClient {

    public static final String ERROR_SETTING_DOI_METADATA_TEMPLATE = "Error setting DOI metadata (%s)";
    public static final String ERROR_SETTING_DOI_URL_TEMPLATE = "Error setting DOI url (%s)";
    public static final String ERROR_DELETING_DOI_METADATA_TEMPLATE = "Error deleting DOI metadata (%s)";
    public static final String ERROR_DELETING_DOI_TEMPLATE = "Error deleting DOI (%s)";
    public static final String ERROR_COMMUNICATION_TEMPLATE = "Error during API communication: (%s)";
    protected static final String CHARACTER_PARENTHESES_START = "(";
    protected static final String CHARACTER_PARENTHESES_STOP = ")";
    protected static final String CHARACTER_WHITESPACE = " ";
    protected static final char FORWARD_SLASH = '/';
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
            if (!isSuccessfulApiResponse(response)) {
                throw logAndCreateApiException(response.statusCode(), ERROR_SETTING_DOI_METADATA_TEMPLATE);
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
            if (!isSuccessfulApiResponse(response)) {
                throw logAndCreateApiException(response.statusCode(), ERROR_SETTING_DOI_METADATA_TEMPLATE);
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
            if (!isSuccessfulApiResponse(response)) {
                throw logAndCreateApiException(response.statusCode(), ERROR_SETTING_DOI_URL_TEMPLATE);
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
            if (!isSuccessfulApiResponse(response)) {
                throw logAndCreateApiException(response.statusCode(), ERROR_DELETING_DOI_METADATA_TEMPLATE);
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
            if (!isSuccessfulApiResponse(response)) {
                throw logAndCreateApiException(response.statusCode(), ERROR_DELETING_DOI_TEMPLATE);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw logAndCreateClientException("deleteDraftDoi", e);
        }
    }

    private DataCiteMdsConnection prepareAuthenticatedDataciteConnection(String customerId) {
        return mdsConnectionFactory.getAuthenticatedConnection(customerId);
    }

    private ClientException logAndCreateClientException(String doiClientMethodName, Exception parentException) {
        // We have to expand the format string anyway for the exception..
        String errorMessage = String.format(ERROR_COMMUNICATION_TEMPLATE, doiClientMethodName);
        logger.error(errorMessage);
        return new ClientException(errorMessage, parentException);
    }

    private ClientException logAndCreateApiException(int statusCode, String errorStringTemplate) {
        // We have to expand the format string anyway for the exception..
        String errorMessage = String.format(errorStringTemplate, statusCode);
        logger.error(errorMessage);
        return new ClientException(errorMessage);
    }

    private Doi extractDoiPrefixAndSuffix(String createMetadataResponseBody) {
        var identifier = StringUtils.substringBetween(createMetadataResponseBody,
            CHARACTER_PARENTHESES_START,
            CHARACTER_PARENTHESES_STOP);
        return Doi.builder().identifier(identifier).build();
    }

    private boolean isSuccessfulApiResponse(HttpResponse<String> createDoiResponse) {
        return createDoiResponse.statusCode() / 100 == 2;
    }
}
