package no.unit.nva.doi.datacite.config;

import static no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory.ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS;
import static no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory.ERROR_HAS_INVALID_CONFIGURATION;
import static no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory.ERROR_NOT_PRESENT_IN_CONFIG;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import no.unit.nva.doi.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import nva.commons.utils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataCiteConfigurationFactoryTest {

    private static final String INVALID_JSON = "{{";
    private static final String EMPTY_CREDENTIALS_CONFIGURED = "[]";
    private static final String DEMO_PREFIX = "10.5072";
    private static final String KNOWN_CUSTOMER_ID = "https://example.net/customer/id/1234";
    private static final String KNOWN_CUSTOMER2_ID = "https://example.net/customer/id/42";
    private static final String UNKNOWN_CUSTOMER_ID = "https://example.net/customer/id/1249423";
    private static final String MISSING_CONFIGURATION_CUSTOMER_ID = "https://example.net/customer/id/missing"
        + "-configuration";
    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_INSTITUTION = KNOWN_CUSTOMER_ID;
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserNameForRepository";
    private static final String EXAMPLE_MDS_PASSWORD = UUID.randomUUID().toString();
    private static final String EXAMPLE_ENDPOINT = "https://example.net/datacite/mds/api";
    private static final List<DataCiteMdsClientConfig> FAKE_CLIENT_CONFIGS = List.of(
        new DataCiteMdsClientSecretConfig(EXAMPLE_INSTITUTION, EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_ENDPOINT,
            EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD));
    private static final String KNOWN_CUSTOMER2_PASSWORD = "randompasswd2";
    private SecretCache secretCache;
    private DataCiteConfigurationFactory dataCiteConfigurationFactory;

    @BeforeEach
    void setUp() {
        configureWithNoCredentials();
        prepareCredentials();
        setupSystemUnderTest();
    }

    @Test
    void constructorWithExampleConfigAsInputstreamThenContains2KnownCustomers()
        throws DataCiteMdsConfigValidationFailedException {
        dataCiteConfigurationFactory = createDataCiteConfigurationFactoryFromInputStream();

        assertThat(dataCiteConfigurationFactory.getNumbersOfConfiguredCustomers(), is(equalTo(3)));
        var customerConfig1 = dataCiteConfigurationFactory.getConfig(KNOWN_CUSTOMER_ID);
        assertThat(customerConfig1.getInstitution(), is(equalTo(KNOWN_CUSTOMER_ID)));
        var customerConfig2 = dataCiteConfigurationFactory.getCredentials(KNOWN_CUSTOMER2_ID);
        assertThat(customerConfig2.getInstitution(), is(equalTo(KNOWN_CUSTOMER2_ID)));
        assertThat(customerConfig2.getDataCiteMdsClientPassword(), is(equalTo(KNOWN_CUSTOMER2_PASSWORD)));
    }

    @Test
    void getCredentialsWithValidConfigurationReturnsSecretInstanceType()
        throws DataCiteMdsConfigValidationFailedException {
        var credentials = dataCiteConfigurationFactory.getCredentials(KNOWN_CUSTOMER_ID);
        assertThat(credentials, is(instanceOf(DataCiteMdsClientSecretConfig.class)));
    }

    @Test
    void getConfigWithValidConfigurationReturnsConfigInstanceType()
        throws DataCiteMdsConfigValidationFailedException {
        var config = dataCiteConfigurationFactory.getConfig(KNOWN_CUSTOMER_ID);
        assertThat(config, is(instanceOf(DataCiteMdsClientConfig.class)));
    }

    @Test
    void getConfigWithUnknownCustomerThrowsConfigValidationException() {
        var actualException = assertThrows(DataCiteMdsConfigValidationFailedException.class,
            () -> dataCiteConfigurationFactory.getConfig(UNKNOWN_CUSTOMER_ID));
        assertThat(actualException.getMessage(), containsString(UNKNOWN_CUSTOMER_ID));
        assertThat(actualException.getMessage(), containsString(ERROR_NOT_PRESENT_IN_CONFIG));
    }

    @Test
    void getConfigWithCustomerNotFullyConfiguredThrowsConfigValidationException() {
        dataCiteConfigurationFactory = createDataCiteConfigurationFactoryFromInputStream();
        var actualException = assertThrows(DataCiteMdsConfigValidationFailedException.class,
            () -> dataCiteConfigurationFactory.getConfig(MISSING_CONFIGURATION_CUSTOMER_ID));
        assertThat(actualException.getMessage(), containsString(MISSING_CONFIGURATION_CUSTOMER_ID));
        assertThat(actualException.getMessage(), containsString(ERROR_HAS_INVALID_CONFIGURATION));
    }

    @Test
    void getConfigWithMissingCustomerThrowsConfigValidationException() {
        configureWithNoCredentials();
        var actualException = assertThrows(DataCiteMdsConfigValidationFailedException.class,
            () -> dataCiteConfigurationFactory.getConfig(UNKNOWN_CUSTOMER_ID));
        assertThat(actualException.getMessage(), containsString(UNKNOWN_CUSTOMER_ID));
        assertThat(actualException.getMessage(), containsString(ERROR_NOT_PRESENT_IN_CONFIG));
    }

    @Test
    void getNumberOfConfiguredCustomersThenReturnsNumber() {
        assertThat(dataCiteConfigurationFactory.getNumbersOfConfiguredCustomers(), is(equalTo(FAKE_CLIENT_CONFIGS.size())));
    }

    @Test
    void getCredentialsWithMissingCustomerThrowsNoCredentialsForCustomerRuntimeException() {
        configureWithNoCredentials();
        assertThrows(NoCredentialsForCustomerRuntimeException.class, () -> dataCiteConfigurationFactory.getCredentials(KNOWN_CUSTOMER_ID));
    }

    @Test
    void getCredentialsWithUnknownCustomerThrowsNoCredentialsForCustomerRuntimeException() {
        assertThrows(NoCredentialsForCustomerRuntimeException.class, () -> dataCiteConfigurationFactory.getCredentials(UNKNOWN_CUSTOMER_ID));
    }

    @Test
    void constructorThrowsExceptionWhenConfigurationError() {
        prepareBadCredentialsConfig();
        assertThrows(IllegalStateException.class,
            () -> new DataCiteConfigurationFactory(secretCache, ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS));
    }

    private DataCiteConfigurationFactory createDataCiteConfigurationFactoryFromInputStream() {
        return new DataCiteConfigurationFactory(IoUtils.inputStreamFromResources(
            Path.of("example-mds-config.json")));
    }

    private void setupSystemUnderTest() {
        dataCiteConfigurationFactory = new DataCiteConfigurationFactory(secretCache, ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS);
    }

    private void configureWithNoCredentials() {
        secretCache = mock(SecretCache.class);
        when(secretCache.getSecretString(ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS))
            .thenReturn(EMPTY_CREDENTIALS_CONFIGURED);
        setupSystemUnderTest();
    }

    private void prepareCredentials() {
        try {
            when(secretCache.getSecretString(ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS))
                .thenReturn(objectMapper.writeValueAsString(FAKE_CLIENT_CONFIGS));
        } catch (JsonProcessingException e) {
            fail("Test configuration failed");
        }
    }

    private void prepareBadCredentialsConfig() {
        try {
            when(secretCache.getSecretString(ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS))
                .thenReturn(objectMapper.writeValueAsString(INVALID_JSON));
        } catch (JsonProcessingException e) {
            fail("Test configuration failed");
        }
    }
}