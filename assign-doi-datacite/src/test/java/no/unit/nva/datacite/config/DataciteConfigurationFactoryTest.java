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
import no.unit.nva.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataciteConfigurationFactoryTest {

    public static final String DEMO_PREFIX = "10.5072";
    public static final String INVALID_JSON = "{{";
    public static final String EMPTY_CREDENTIALS_CONFIGURED = "[]";
    private static final String KNOWN_CUSTOMER_ID = "https://example.net/customer/id/1234";
    private static final String EXAMPLE_ENDPOINT = "https://example.net/datacite/mds/api";
    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_INSTITUTION = KNOWN_CUSTOMER_ID;
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserNameForRepository";
    private static final String EXAMPLE_MDS_PASSWORD = UUID.randomUUID().toString();
    private static final List<DataCiteMdsClientConfig> FAKE_CLIENT_CONFIGS = List.of(
        new DataCiteMdsClientSecretConfig(EXAMPLE_INSTITUTION, EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_ENDPOINT,
            EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD));
    private static final String UNKNOWN_CUSTOMER_ID = "https://example.net/customer/id/1249423";
    private SecretCache secretCache;
    private DataciteConfigurationFactory sut;

    @BeforeEach
    void setUp() {
        configureWithNoCredentials();
        prepareCredentials();
        setupSystemUnderTest();
    }

    @Test
    void getCredentialsWithValidConfigurationReturnsPresentOptionalWithSecretInstanceType() {
        var credentials = sut.getCredentials(KNOWN_CUSTOMER_ID);
        assertThat(credentials.isPresent(), is(true));
        assertThat(credentials.get(), is(instanceOf(DataCiteMdsClientConfig.class)));
    }

    @Test
    void getConfigWithValidConfigurationReturnsOptionalWithConfigInstanceType() {
        var credentials = sut.getConfig(KNOWN_CUSTOMER_ID);
        assertThat(credentials.isPresent(), is(true));
        assertThat(credentials.get(), is(instanceOf(DataCiteMdsClientSecretConfig.class)));
    }

    @Test
    void getConfigWithUnknownCustomerReturnsOptionalEmpty() {
        assertThat(sut.getConfig(UNKNOWN_CUSTOMER_ID).isEmpty(), is(true));
    }

    @Test
    void getConfigWithMissingCustomerReturnsOptionalEmpty() {
        configureWithNoCredentials();
        assertThat(sut.getConfig(KNOWN_CUSTOMER_ID).isEmpty(), is(true));
    }

    @Test
    void getCredentialsWithMissingCustomerReturnsOptionalEmpty() {
        configureWithNoCredentials();
        assertThat(sut.getCredentials(KNOWN_CUSTOMER_ID).isEmpty(), is(true));
    }

    @Test
    void getSecretWithUnknownCustomerReturnsOptionalEmpty() {
        assertThat(sut.getConfig(UNKNOWN_CUSTOMER_ID).isEmpty(), is(true));
    }

    @Test
    void constructorThrowsExceptionWithConfigurationError() {
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