package no.unit.nva.doi.datacite.models;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.net.URI;
import org.junit.jupiter.api.Test;

class DataCiteMdsClientConfigTest {

    private static final URI EXAMPLE_CUSTOMER_ID = URI.create("https://example.net/customer/id/123");
    private static final String DEMO_PREFIX = "10.5072";
    private static final String EXAMPLE_CUSTOMER_DOI_PREFIX = DEMO_PREFIX;

    @Test
    void constructorPopulatesAllFields() {
        var config = createFullyConfigWithoutSecretConfig();
        assertThat(config.getCustomerId(), is(equalTo(EXAMPLE_CUSTOMER_ID)));
        assertThat(config.getCustomerDoiPrefix(), is(equalTo(EXAMPLE_CUSTOMER_DOI_PREFIX)));
    }

    @Test
    void settersPopulatesAllFields() {
        var config = new DataCiteMdsClientConfig();
        assertThat(config.getCustomerId(), nullValue());
        assertThat(config.getCustomerDoiPrefix(), nullValue());
        config.setCustomerId(EXAMPLE_CUSTOMER_ID);
        config.setCustomerDoiPrefix(EXAMPLE_CUSTOMER_DOI_PREFIX);
        assertThat(config.getCustomerId(), is(equalTo(EXAMPLE_CUSTOMER_ID)));
        assertThat(config.getCustomerDoiPrefix(), is(equalTo(EXAMPLE_CUSTOMER_DOI_PREFIX)));
        assertThat(config, doesNotHaveNullOrEmptyFields());
    }

    @Test
    void isFullyConfiguredWithEmptySecretConfigThenReturnsFalse() {
        var secretConfig = new DataCiteMdsClientSecretConfig();
        assertThat(secretConfig.isFullyConfigured(), is(equalTo(false)));
    }

    @Test
    void isFullyConfiguredWithMissingInstitutionThenReturnsFalse() {
        var config = createFullyConfigWithoutSecretConfig();
        config.setCustomerId(null);
        assertThat(config.isFullyConfigured(), is(equalTo(false)));
    }

    @Test
    void isFullyConfiguredWithMissingInstitutionPrefixThenReturnsFalse() {
        var config = createFullyConfigWithoutSecretConfig();
        config.setCustomerDoiPrefix(null);
        assertThat(config.isFullyConfigured(), is(equalTo(false)));
    }

    @Test
    void isFullyConfiguredWithCorrectConfigurationReturnsTrue() {
        var config = createFullyConfigWithoutSecretConfig();
        assertThat(config.isFullyConfigured(), is(equalTo(true)));
    }

    private DataCiteMdsClientConfig createFullyConfigWithoutSecretConfig() {
        return new DataCiteMdsClientConfig(EXAMPLE_CUSTOMER_ID,
            EXAMPLE_CUSTOMER_DOI_PREFIX);
    }
}