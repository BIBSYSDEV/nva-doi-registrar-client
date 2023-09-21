package no.unit.nva.doi.datacite.connectionfactories;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.doi.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import nva.commons.secrets.SecretsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataCite configuration factory to obtain DataCite related configuration.
 *
 * <p>{@link #getConfig(URI)} for obtaining configuration for a specific customer, and
 * {@link #getCredentials(URI)} for obtaining secret configuration, but this is restricted for implementations scoped
 * under package {@link no.unit.nva.doi.datacite.connectionfactories}.
 */
public class DataCiteConfigurationFactory {

    public static final String CUSTOMER_SECRETS_SECRET_NAME_EVN_VAR = "CUSTOMER_SECRETS_SECRET_NAME";
    public static final String CUSTOMER_SECRETS_SECRET_KEY_ENV_VAR = "CUSTOMER_SECRETS_SECRET_KEY";
    public static final String ERROR_NOT_PRESENT_IN_CONFIG = " not present in config";
    public static final String ERROR_HAS_INVALID_CONFIGURATION = " has invalid configuration!";

    private static final Logger logger = LoggerFactory.getLogger(DataCiteConfigurationFactory.class);

    @SuppressWarnings("PMD.ImmutableField")
    private Map<URI, DataCiteMdsClientSecretConfig> customerConfigurations = new ConcurrentHashMap<>();

    /**
     * Construct a new DataCite configuration factory.
     *
     * @param secretsReader to read the secret
     * @param secretName    the secret's name
     * @param secretKey     secret's key
     */
    public DataCiteConfigurationFactory(SecretsReader secretsReader, String secretName, String secretKey) {
        this(fetchSecret(secretsReader, secretName, secretKey));
    }

    public DataCiteConfigurationFactory(String jsonConfig) {
        parseConfig(jsonConfig);
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
        printConfigs(customerConfigurations);
        DataCiteMdsClientSecretConfig value = customerConfigurations.get(customerId);
        if (isNull(value)) {
            throw new DataCiteMdsConfigValidationFailedException(customerId + ERROR_NOT_PRESENT_IN_CONFIG);
        }
        return Optional.of(value)
            .filter(DataCiteMdsClientConfig::isFullyConfigured)
            .orElseThrow(
                () -> new DataCiteMdsConfigValidationFailedException(customerId + ERROR_HAS_INVALID_CONFIGURATION));
    }

    private void printConfigs(Map<URI, DataCiteMdsClientSecretConfig> customerConfigurations) {
        logger.info("Number of customers: " + customerConfigurations.size());
        customerConfigurations.forEach(this::printCustomer);
    }

    //TODO: this should not be merged. AND YES I DID THIS OUT OF DESPERATION
    private void printCustomer(URI uri, DataCiteMdsClientSecretConfig dataCiteMdsClientSecretConfig) {
        logger.info("Customer with uri "
                + uri.toString()
                + ", has dataciteusername: "
                + dataCiteMdsClientSecretConfig.getDataCiteMdsClientUsername()
                + ", and password "
                + dataCiteMdsClientSecretConfig.getDataCiteMdsClientPassword());
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

    private static String fetchSecret(SecretsReader secretsReader, String secretName, String secretKey) {
        return attempt(() -> secretsReader.fetchSecret(secretName, secretKey)).orElseThrow();
    }

    private void parseConfig(String secretConfig) {
        try {
            var secretConfigurations =
                Optional.ofNullable(dtoObjectMapper.readValue(secretConfig, DataCiteMdsClientSecretConfig[].class));
            secretConfigurations.ifPresent(this::populateCustomerConfigurationMap);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse secret configuration");
        }
    }

    private void populateCustomerConfigurationMap(DataCiteMdsClientSecretConfig... dataCiteMdsClientConfigs) {
        for (DataCiteMdsClientSecretConfig dataCiteMdsClientSecretConfig : dataCiteMdsClientConfigs) {
            customerConfigurations.put(
                dataCiteMdsClientSecretConfig.getCustomerId(), dataCiteMdsClientSecretConfig);
        }
    }
}
