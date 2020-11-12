package no.unit.nva.doi.datacite.config;

import static nva.commons.utils.JsonUtils.objectMapper;
import com.amazonaws.secretsmanager.caching.SecretCache;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.doi.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;

/**
 * DataCite configuration factory to obtain DataCite related configuration.
 *
 * <p>{@link #getConfig(String)} for obtaining configuration for a specific customer, and
 * {@link #getCredentials(String)} for obtaining secret configuration, but this is restricted for implementations scoped
 * under package {@link no.unit.nva.datacite.config}.
 */
public class DataCiteConfigurationFactory {

    public static final String ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS = "DATACITE_MDS_CONFIGS";

    private Map<String, DataCiteMdsClientSecretConfig> dataCiteMdsClientConfigsMap = new ConcurrentHashMap<>();
    private SecretCache secretCache;

    /**
     * Construct a new DataCite configuration factory.
     *
     * @param secretCache to obtain secret configuration from
     * @param secretId    id to look up in AWS Secret Manager
     */
    public DataCiteConfigurationFactory(SecretCache secretCache, String secretId) {
        this.secretCache = secretCache;
        loadSecretsFromSecretManager(secretId);
    }

    /**
     * Construct a new DataCite configuration factory for unit/system tests with pre populated secrets.
     *
     * @param testSecretConfigs Pre populated DataCite configuration.
     */
    protected DataCiteConfigurationFactory(Map<String, DataCiteMdsClientSecretConfig> testSecretConfigs) {
        this.dataCiteMdsClientConfigsMap = testSecretConfigs;
    }

    /**
     * Retrieve DataCite configuration for given NVA customer.
     *
     * @param customerId NVA customer id in format https://example.net/nva/customer/923923
     * @return DataCiteMdsClientConfig
     * @throws DataCiteMdsConfigValidationFailedException no valid customer configuration
     */
    public DataCiteMdsClientConfig getConfig(String customerId) throws DataCiteMdsConfigValidationFailedException {
        return Optional.ofNullable(dataCiteMdsClientConfigsMap.get(customerId))
            .filter(DataCiteMdsClientConfig::isFullyConfigured)
            .orElseThrow(DataCiteMdsConfigValidationFailedException::new);
    }

    /**
     * Retrieve DataCite secret configuration for given NVA customer.
     *
     * @param customerId NVA customer id in format https://example.net/nva/customer/923923
     * @return Configuration wrapped in optional if present.
     * @throws NoCredentialsForCustomerRuntimeException missing credentials configuration in secret config.
     */
    protected DataCiteMdsClientSecretConfig getCredentials(String customerId) {
        return Optional.ofNullable(dataCiteMdsClientConfigsMap.get(customerId))
            .filter(DataCiteMdsClientSecretConfig::isFullyConfigured)
            .orElseThrow(NoCredentialsForCustomerRuntimeException::new);
    }

    private void loadSecretsFromSecretManager(String secretId) {
        try {
            String secretConfigAsJson = secretCache.getSecretString(secretId);
            var secretConfigurations = Optional.ofNullable(objectMapper.readValue(secretConfigAsJson,
                DataCiteMdsClientSecretConfig[].class));
            secretConfigurations.ifPresent(this::populateCustomerConfigurationMap);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse configuration from secret string: " + secretId);
        }
    }

    private void populateCustomerConfigurationMap(DataCiteMdsClientSecretConfig[] dataCiteMdsClientConfigs) {
        for (DataCiteMdsClientSecretConfig dataCiteMdsClientSecretConfig : dataCiteMdsClientConfigs) {
            dataCiteMdsClientConfigsMap.put(
                dataCiteMdsClientSecretConfig.getInstitution(), dataCiteMdsClientSecretConfig);
        }
    }
}
