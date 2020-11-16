package no.unit.nva.doi.datacite.config;

import static java.util.Objects.isNull;
import static nva.commons.utils.JsonUtils.objectMapper;
import com.amazonaws.secretsmanager.caching.SecretCache;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.doi.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import nva.commons.utils.IoUtils;

/**
 * DataCite configuration factory to obtain DataCite related configuration.
 *
 * <p>{@link #getConfig(String)} for obtaining configuration for a specific customer, and
 * {@link #getCredentials(String)} for obtaining secret configuration, but this is restricted for implementations scoped
 * under package {@link no.unit.nva.doi.datacite.config}.
 */
public class DataCiteConfigurationFactory {

    public static final String ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS = "DATACITE_MDS_CONFIGS";
    public static final String ERROR_NOT_PRESENT_IN_CONFIG = " not present in config";
    public static final String ERROR_HAS_INVALID_CONFIGURATION = " has invalid configuration!";

    private Map<String, DataCiteMdsClientSecretConfig> customerConfigurations = new ConcurrentHashMap<>();

    /**
     * Construct a new DataCite configuration factory.
     *
     * @param secretCache to obtain secret configuration from
     * @param secretId    id to look up in AWS Secret Manager
     */
    public DataCiteConfigurationFactory(SecretCache secretCache, String secretId) {
        this(secretCache.getSecretString(secretId));
    }

    public DataCiteConfigurationFactory(InputStream jsonConfig) {
        this(IoUtils.streamToString(jsonConfig));
    }

    protected DataCiteConfigurationFactory(String secretConfigAsJsonString) {
        parseConfig(secretConfigAsJsonString);
    }

    /**
     * Construct a new DataCite configuration factory for unit/system tests with pre populated secrets.
     *
     * @param testSecretConfigs Pre populated DataCite configuration.
     */
    protected DataCiteConfigurationFactory(Map<String, DataCiteMdsClientSecretConfig> testSecretConfigs) {
        this.customerConfigurations = testSecretConfigs;
    }

    /**
     * Retrieve DataCite configuration for given NVA customer.
     *
     * @param customerId NVA customer id in format https://example.net/nva/customer/923923
     * @return DataCiteMdsClientConfig
     * @throws DataCiteMdsConfigValidationFailedException no valid customer configuration
     */
    public DataCiteMdsClientConfig getConfig(String customerId) throws DataCiteMdsConfigValidationFailedException {
        DataCiteMdsClientSecretConfig value = customerConfigurations.get(customerId);
        if (isNull(value)) {
            throw new DataCiteMdsConfigValidationFailedException(customerId + ERROR_NOT_PRESENT_IN_CONFIG);
        }
        return Optional.of(value)
            .filter(DataCiteMdsClientConfig::isFullyConfigured)
            .orElseThrow(
                () -> new DataCiteMdsConfigValidationFailedException(customerId + ERROR_HAS_INVALID_CONFIGURATION));
    }

    /**
     * Retrieve DataCite secret configuration for given NVA customer.
     *
     * @param customerId NVA customer id in format https://example.net/nva/customer/923923
     * @return Configuration wrapped in optional if present.
     * @throws NoCredentialsForCustomerRuntimeException missing credentials configuration in secret config.
     */
    protected DataCiteMdsClientSecretConfig getCredentials(String customerId) {
        return Optional.ofNullable(customerConfigurations.get(customerId))
            .filter(DataCiteMdsClientSecretConfig::isFullyConfigured)
            .orElseThrow(NoCredentialsForCustomerRuntimeException::new);
    }

    public int getNumbersOfConfiguredCustomers() {
        return customerConfigurations.size();
    }

    private void parseConfig(String secretConfigAsJsonString) {
        try {
            var secretConfigurations = Optional.ofNullable(objectMapper.readValue(secretConfigAsJsonString,
                DataCiteMdsClientSecretConfig[].class));
            secretConfigurations.ifPresent(this::populateCustomerConfigurationMap);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse secret configuration");
        }
    }

    private void populateCustomerConfigurationMap(DataCiteMdsClientSecretConfig[] dataCiteMdsClientConfigs) {
        for (DataCiteMdsClientSecretConfig dataCiteMdsClientSecretConfig : dataCiteMdsClientConfigs) {
            customerConfigurations.put(
                dataCiteMdsClientSecretConfig.getInstitution(), dataCiteMdsClientSecretConfig);
        }
    }
}
