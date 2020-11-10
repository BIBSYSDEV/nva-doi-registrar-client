package no.unit.nva.datacite.clients;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import no.unit.nva.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.datacite.exception.ClientException;
import no.unit.nva.datacite.mdsclient.DataciteMdsConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataciteClient implements DoiClient {

    public static final String CHARACTER_PARENTHESES_START = "(";
    public static final String CHARACTER_PARENTHESES_STOP = ")";
    public static final String CHARACTER_WHITESPACE = " ";

    public static final String ERROR_SETTING_DOI_METADATA = "Error setting DOI metadata";
    public static final String ERROR_SETTING_DOI_URL = "Error setting DOI url";
    public static final String ERROR_DELETING_DOI_METADATA = "Error deleting DOI metadata";
    private static final Logger logger = LoggerFactory.getLogger(DataciteClient.class);

    private final DataciteMdsConnectionFactory mdsConnectionFactory;
    private final DataciteConfigurationFactory configFactory;

    public DataciteClient(DataciteConfigurationFactory configFactory,
                          DataciteMdsConnectionFactory mdsConnectionFactory) {
        this.configFactory = configFactory;
        this.mdsConnectionFactory = mdsConnectionFactory;
    }

    @Override
    public String createDoi(String customerId, String metadataDataciteXml) {
        var prefix = configFactory.getConfig(customerId).getInstitutionPrefix();
        String doi;
        try {
            HttpResponse<String> createMetadataResponse =
                mdsConnectionFactory.getAuthenticatedConnection(customerId).postMetadata(prefix, metadataDataciteXml);
            if (createMetadataResponse.statusCode() != SC_CREATED) {
                throw new RuntimeException(new ClientException(
                    ERROR_SETTING_DOI_METADATA + CHARACTER_WHITESPACE
                        + CHARACTER_PARENTHESES_START
                        + createMetadataResponse.statusCode()
                        + CHARACTER_PARENTHESES_STOP));
            }
            String createMetadataResponseBody = createMetadataResponse.body();
            doi = extractDoiPrefixAndSuffix(createMetadataResponseBody);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(new ClientException(ERROR_SETTING_DOI_METADATA));
        }
        return doi;
    }

    @Override
    public void updateMetadata(String customerId, String doi, String metadataDataciteXml) {
        try {
            HttpResponse<String> createDoiResponse = mdsConnectionFactory.getAuthenticatedConnection(customerId)
                .postMetadata(doi, metadataDataciteXml);
            if (!isSucessfullApiResponse(createDoiResponse)) {
                throw new RuntimeException(new ClientException(ERROR_SETTING_DOI_URL
                    + CHARACTER_WHITESPACE
                    + CHARACTER_PARENTHESES_START
                    + createDoiResponse.statusCode()
                    + CHARACTER_PARENTHESES_STOP));
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLandingPage(String customerId, String doi, URI url) {

    }

    public void deleteOrGraveyardDoi(String customerId, String doi) throws ClientException {
        try {
            HttpResponse<String> deleteDoiMetadata = mdsConnectionFactory.getAuthenticatedConnection(customerId)
                .deleteMetadata(doi);
            if (deleteDoiMetadata.statusCode() == SC_OK) {
                throw new ClientException(ERROR_SETTING_DOI_URL);
            } else {
                logger.error(ERROR_DELETING_DOI_METADATA
                    + CHARACTER_WHITESPACE
                    + CHARACTER_PARENTHESES_START
                    + deleteDoiMetadata.statusCode()
                    + CHARACTER_PARENTHESES_STOP);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    private String extractDoiPrefixAndSuffix(String createMetadataResponseBody) {
        return StringUtils.substringBetween(createMetadataResponseBody, CHARACTER_PARENTHESES_START,
            CHARACTER_PARENTHESES_STOP);
    }

    private boolean isSucessfullApiResponse(HttpResponse<String> createDoiResponse) {
        return (createDoiResponse.statusCode() / 100) == 2;
    }
}
