package no.unit.nva.doi.datacite.config;

import static java.util.Objects.isNull;
import static nva.commons.utils.JsonUtils.objectMapper;
import com.amazonaws.secretsmanager.caching.SecretCache;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
 * <p>{@link #getConfig(URI)} for obtaining configuration for a specific customer, and
 * {@link #getCredentials(URI)} for obtaining secret configuration, but this is restricted for implementations scoped
 * under package {@link no.unit.nva.doi.datacite.config}.
 */
public class DataCiteConfigurationFactory {

    public static final String ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS = "DATACITE_MDS_CONFIGS";
    public static final String ERROR_NOT_PRESENT_IN_CONFIG = " not present in config";
    public static final String ERROR_HAS_INVALID_CONFIGURATION = " has invalid configuration!";

    private Map<URI, DataCiteMdsClientSecretConfig> customerConfigurations = new ConcurrentHashMap<>();

    /**
     * Construct a new DataCite configuration factory.
     *
     * @param secretCache to obtain secret configuration from
     * @param secretId    id to look up in AWS Secret Manager
     */
    public DataCiteConfigurationFactory(SecretCache secretCache, String secretId) {
        this(IoUtils.stringToStream(secretCache.getSecretString(secretId)));
    }

    public DataCiteConfigurationFactory(InputStream jsonConfig) {
        parseConfig(jsonConfig);
    }

    public DataCiteConfigurationFactory(String secretConfigAsJsonString) {
        this(IoUtils.stringToStream(secretConfigAsJsonString));
    }

    /**
     * Construct a new DataCite configuration factory for unit/system tests with pre populated secrets.
     *
     * @param testSecretConfigs Pre populated DataCite configuration.
     */
    protected DataCiteConfigurationFactory(Map<URI, DataCiteMdsClientSecretConfig> testSecretConfigs) {
        this.customerConfigurations = testSecretConfigs;
    }

    /**
     * Retrieve DataCite configuration for given NVA customer.
     *
     * @param customerId NVA customer id in format https://example.net/nva/customer/923923
     * @return DataCiteMdsClientConfig
     * @throws DataCiteMdsConfigValidationFailedException no valid customer configuration
     */
    public DataCiteMdsClientConfig getConfig(URI customerId) throws DataCiteMdsConfigValidationFailedException {
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
     * Retrieve numbver of configured configurations.
     *
     * <p>It may contain configuration which is not fully configured and not valid!
     *
     * <p>They are evaluated and validated during runtime to avoid having to validate all records on startup
     *
     * @return number of configured customers
     */
    public int getNumbersOfConfiguredCustomers() {
        return customerConfigurations.size();
    }

    /**
     * Retrieve DataCite secret configuration for given NVA customer.
     *
     * @param customerId NVA customer id in format https://example.net/nva/customer/923923
     * @return Configuration wrapped in optional if present.
     * @throws NoCredentialsForCustomerRuntimeException missing credentials configuration in secret config.
     */
    protected DataCiteMdsClientSecretConfig getCredentials(URI customerId) {
        return Optional.ofNullable(customerConfigurations.get(customerId))
            .filter(DataCiteMdsClientSecretConfig::isFullyConfigured)
            .orElseThrow(NoCredentialsForCustomerRuntimeException::new);
    }

    private void parseConfig(InputStream secretConfig) {
        try {
            var secretConfigurations = Optional.ofNullable(objectMapper.readValue(secretConfig,
                DataCiteMdsClientSecretConfig[].class));
            secretConfigurations.ifPresent(this::populateCustomerConfigurationMap);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse secret configuration");
        }
    }

    private void populateCustomerConfigurationMap(DataCiteMdsClientSecretConfig[] dataCiteMdsClientConfigs) {
        for (DataCiteMdsClientSecretConfig dataCiteMdsClientSecretConfig : dataCiteMdsClientConfigs) {
            customerConfigurations.put(
                dataCiteMdsClientSecretConfig.getCustomerId(), dataCiteMdsClientSecretConfig);
        }
    }
}
