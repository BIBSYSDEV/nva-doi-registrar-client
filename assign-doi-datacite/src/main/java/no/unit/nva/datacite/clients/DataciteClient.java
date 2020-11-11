package no.unit.nva.datacite.clients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import no.unit.nva.datacite.clients.exception.ClientException;
import no.unit.nva.datacite.clients.models.Doi;
import no.unit.nva.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.datacite.mdsclient.DataciteMdsConnectionFactory;
import no.unit.nva.datacite.models.DataCiteMdsClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DoiClient implementation towards Registry Agency Datacite.
 */
public class DataciteClient implements DoiClient {

    public static final String ERROR_SETTING_DOI_METADATA = "Error setting DOI metadata";
    public static final String ERROR_SETTING_DOI_URL = "Error setting DOI url";
    public static final String ERROR_DELETING_DOI_METADATA = "Error deleting DOI metadata";
    public static final String ERROR_DELETING_DOI = "Error deleting DOI";
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
        var prefix = configFactory.getConfig(customerId)
            .map(DataCiteMdsClientConfig::getInstitutionPrefix)
            .orElseThrow();
        Doi doi;
        try {
            HttpResponse<String> createMetadataResponse =
                mdsConnectionFactory
                    .getAuthenticatedConnection(customerId)
                    .postMetadata(prefix, metadataDataciteXml);
            if (!isSucessfullApiResponse(createMetadataResponse)) {
                throw new ClientException(
                    ERROR_SETTING_DOI_METADATA + CHARACTER_WHITESPACE
                        + CHARACTER_PARENTHESES_START
                        + createMetadataResponse.statusCode()
                        + CHARACTER_PARENTHESES_STOP);
            }
            String createMetadataResponseBody = createMetadataResponse.body();
            doi = extractDoiPrefixAndSuffix(createMetadataResponseBody);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new ClientException(ERROR_SETTING_DOI_METADATA, e);
        }
        return doi;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMetadata(String customerId, Doi doi, String metadataDataciteXml) throws ClientException {
        try {
            var response = mdsConnectionFactory
                .getAuthenticatedConnection(customerId)
                .postMetadata(doi.toIdentifier(), metadataDataciteXml);
            if (!isSucessfullApiResponse(response)) {
                throw new ClientException(ERROR_SETTING_DOI_METADATA
                    + CHARACTER_WHITESPACE
                    + CHARACTER_PARENTHESES_START
                    + response.statusCode()
                    + CHARACTER_PARENTHESES_STOP);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new ClientException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLandingPage(String customerId, Doi doi, URI landingPage) throws ClientException {
        try {
            var response = mdsConnectionFactory
                .getAuthenticatedConnection(customerId)
                .registerUrl(doi.toIdentifier(), landingPage.toASCIIString());
            if (!isSucessfullApiResponse(response)) {
                logger.error(ERROR_SETTING_DOI_URL
                    + CHARACTER_WHITESPACE
                    + CHARACTER_PARENTHESES_START
                    + response.statusCode()
                    + CHARACTER_PARENTHESES_STOP);
                throw new ClientException(ERROR_SETTING_DOI_URL);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMetadata(String customerId, Doi doi) throws ClientException {
        try {
            var response = mdsConnectionFactory
                .getAuthenticatedConnection(customerId)
                .deleteMetadata(doi.toIdentifier());
            if (!isSucessfullApiResponse(response)) {
                logger.error(ERROR_DELETING_DOI_METADATA
                    + CHARACTER_WHITESPACE
                    + CHARACTER_PARENTHESES_START
                    + response.statusCode()
                    + CHARACTER_PARENTHESES_STOP);
                throw new ClientException(ERROR_SETTING_DOI_URL);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDraftDoi(String customerId, Doi doi) throws ClientException {
        try {
            var response = mdsConnectionFactory
                .getAuthenticatedConnection(customerId)
                .deleteDoi(doi.toIdentifier());
            if (!isSucessfullApiResponse(response)) {
                logger.error(ERROR_DELETING_DOI
                    + CHARACTER_WHITESPACE
                    + CHARACTER_PARENTHESES_START
                    + response.statusCode()
                    + CHARACTER_PARENTHESES_STOP);
                throw new ClientException(ERROR_DELETING_DOI);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    private Doi extractDoiPrefixAndSuffix(String createMetadataResponseBody) {
        var identifier = StringUtils.substringBetween(createMetadataResponseBody,
            CHARACTER_PARENTHESES_START,
            CHARACTER_PARENTHESES_STOP);
        return Doi.builder().identifier(identifier).build();
    }

    private boolean isSucessfullApiResponse(HttpResponse<String> createDoiResponse) {
        return createDoiResponse.statusCode() / 100 == 2;
    }
}
