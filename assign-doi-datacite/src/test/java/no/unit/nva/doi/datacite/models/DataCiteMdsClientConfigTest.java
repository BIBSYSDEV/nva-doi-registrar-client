package no.unit.nva.doi.datacite.models;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;

class DataCiteMdsClientConfigTest {

    private static final String EXAMPLE_INSTITUTION = "https://example.net/customer/id/123";
    private static final String DEMO_PREFIX = "10.5072";
    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_MDS_CLIENT_URL = "https://example.net/datacite/mds/api";

    @Test
    void constructorPopulatesAllFields() {
        var config = createFullyConfigWithoutSecretConfig();
        assertThat(config.getInstitution(), is(equalTo(EXAMPLE_INSTITUTION)));
        assertThat(config.getInstitutionPrefix(), is(equalTo(EXAMPLE_INSTITUTION_PREFIX)));
    }

    @Test
    void settersPopulatesAllFields() {
        var config = new DataCiteMdsClientConfig();
        assertThat(config.getInstitution(), nullValue());
        assertThat(config.getInstitutionPrefix(), nullValue());
        config.setInstitution(EXAMPLE_INSTITUTION);
        config.setInstitutionPrefix(EXAMPLE_INSTITUTION_PREFIX);
        assertThat(config.getInstitution(), is(equalTo(EXAMPLE_INSTITUTION)));
        assertThat(config.getInstitutionPrefix(), is(equalTo(EXAMPLE_INSTITUTION_PREFIX)));
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
        config.setInstitution(null);
        assertThat(config.isFullyConfigured(), is(equalTo(false)));
    }

    @Test
    void isFullyConfiguredWithMissingInstitutionPrefixThenReturnsFalse() {
        var config = createFullyConfigWithoutSecretConfig();
        config.setInstitutionPrefix(null);
        assertThat(config.isFullyConfigured(), is(equalTo(false)));
    }

    @Test
    void isFullyConfiguredWithCorrectConfigurationReturnsTrue() {
        var config = createFullyConfigWithoutSecretConfig();
        assertThat(config.isFullyConfigured(), is(equalTo(true)));
    }

    private DataCiteMdsClientConfig createFullyConfigWithoutSecretConfig() {
        return new DataCiteMdsClientConfig(EXAMPLE_INSTITUTION,
            EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_MDS_CLIENT_URL);
    }
}