package no.unit.nva.datacite.config;

import static no.unit.nva.datacite.config.DataciteConfigurationFactory.ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.UUID;
import no.unit.nva.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;
import no.unit.nva.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataciteConfigurationFactoryTest {

    private static final String INVALID_JSON = "{{";
    private static final String EMPTY_CREDENTIALS_CONFIGURED = "[]";
    private static final String DEMO_PREFIX = "10.5072";
    private static final String KNOWN_CUSTOMER_ID = "https://example.net/customer/id/1234";
    private static final String UNKNOWN_CUSTOMER_ID = "https://example.net/customer/id/1249423";
    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_INSTITUTION = KNOWN_CUSTOMER_ID;
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserNameForRepository";
    private static final String EXAMPLE_MDS_PASSWORD = UUID.randomUUID().toString();
    private static final String EXAMPLE_ENDPOINT = "https://example.net/datacite/mds/api";
    private static final List<DataCiteMdsClientConfig> FAKE_CLIENT_CONFIGS = List.of(
        new DataCiteMdsClientSecretConfig(EXAMPLE_INSTITUTION, EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_ENDPOINT,
            EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD));
    private SecretCache secretCache;
    private DataciteConfigurationFactory sut;

    @BeforeEach
    void setUp() {
        configureWithNoCredentials();
        prepareCredentials();
        setupSystemUnderTest();
    }

    @Test
    void getCredentialsWithValidConfigurationReturnsSecretInstanceType()
        throws DataCiteMdsConfigValidationFailedException {
        var credentials = sut.getCredentials(KNOWN_CUSTOMER_ID);
        assertThat(credentials, is(instanceOf(DataCiteMdsClientSecretConfig.class)));
    }

    @Test
    void getConfigWithValidConfigurationReturnsConfigInstanceType()
        throws DataCiteMdsConfigValidationFailedException {
        var config = sut.getConfig(KNOWN_CUSTOMER_ID);
        assertThat(config, is(instanceOf(DataCiteMdsClientConfig.class)));
    }

    @Test
    void getConfigWithUnknownCustomerThrowsConfigValidationException() {
        assertThrows(DataCiteMdsConfigValidationFailedException.class, () -> sut.getConfig(UNKNOWN_CUSTOMER_ID));
    }

    @Test
    void getConfigWithMissingCustomerThrowsConfigValidationException() {
        configureWithNoCredentials();
        assertThrows(DataCiteMdsConfigValidationFailedException.class, () -> sut.getConfig(UNKNOWN_CUSTOMER_ID));
    }

    @Test
    void getCredentialsWithMissingCustomerThrowsNoCredentialsForCustomerRuntimeException() {
        configureWithNoCredentials();
        assertThrows(NoCredentialsForCustomerRuntimeException.class, () -> sut.getCredentials(KNOWN_CUSTOMER_ID));
    }

    @Test
    void getCredentialsWithUnknownCustomerThrowsNoCredentialsForCustomerRuntimeException() {
        assertThrows(NoCredentialsForCustomerRuntimeException.class, () -> sut.getCredentials(UNKNOWN_CUSTOMER_ID));
    }

    @Test
    void constructorThrowsExceptionWhenConfigurationError() {
        prepareBadCredentialsConfig();
        assertThrows(IllegalStateException.class,
            () -> new DataciteConfigurationFactory(secretCache, ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS));
    }

    private void setupSystemUnderTest() {
        sut = new DataciteConfigurationFactory(secretCache, ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS);
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