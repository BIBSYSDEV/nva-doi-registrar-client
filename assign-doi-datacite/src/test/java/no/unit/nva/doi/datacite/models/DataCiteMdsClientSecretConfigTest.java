package no.unit.nva.doi.datacite.models;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.net.URI;
import org.junit.jupiter.api.Test;

public class DataCiteMdsClientSecretConfigTest {

    public static final URI CUSTOMER_ID = URI.create("https://example.net/customer/id");
    public static final URI DATA_CITE_MDS_CLIENT_URL = URI.create("https://example.net/mds/client/url");
    public static final String DATACITE_MDS_CLIENT_USERNAME = "dataCiteMdsClientUsername";
    public static final String DATACITE_MDS_CLIENT_PASSWORD = "dataCiteMdsClientPassword";
    private static final String DEMO_PREFIX = "10.5072";
    public static final String CUSTOMER_DOI_PREFIX = DEMO_PREFIX;
    private static final String EMPTY_STRING = "";
    private static final String BLANK_STRING = "    ";

    @Test
    void constructorPopulatesAllFields() {
        var secretConfig = createSecretConfigFullyPopulated();
        assertThat(secretConfig.getCustomerId(), is(equalTo(CUSTOMER_ID)));
        assertThat(secretConfig.getCustomerDoiPrefix(), is(equalTo(CUSTOMER_DOI_PREFIX)));
        assertThat(secretConfig.getDataCiteMdsClientUrl(), is(equalTo(DATA_CITE_MDS_CLIENT_URL)));
        assertThat(secretConfig.getDataCiteMdsClientUsername(), is(equalTo(DATACITE_MDS_CLIENT_USERNAME)));
        assertThat(secretConfig.getDataCiteMdsClientPassword(), is(equalTo(DATACITE_MDS_CLIENT_PASSWORD)));
        assertThat(secretConfig, doesNotHaveNullOrEmptyFields());
    }

    @Test
    void settersPopulatesAllFields() {
        DataCiteMdsClientSecretConfig secretConfig = new DataCiteMdsClientSecretConfig();
        secretConfig.setCustomerId(CUSTOMER_ID);
        secretConfig.setCustomerDoiPrefix(CUSTOMER_DOI_PREFIX);
        secretConfig.setDataCiteMdsClientUrl(DATA_CITE_MDS_CLIENT_URL);
        secretConfig.setDataCiteMdsClientUsername(DATACITE_MDS_CLIENT_USERNAME);
        secretConfig.setDataCiteMdsClientPassword(DATACITE_MDS_CLIENT_PASSWORD);
        assertThat(secretConfig.getCustomerId(), is(equalTo(CUSTOMER_ID)));
        assertThat(secretConfig.getCustomerDoiPrefix(), is(equalTo(CUSTOMER_DOI_PREFIX)));
        assertThat(secretConfig.getDataCiteMdsClientUrl(), is(equalTo(DATA_CITE_MDS_CLIENT_URL)));
        assertThat(secretConfig.getDataCiteMdsClientUsername(), is(equalTo(DATACITE_MDS_CLIENT_USERNAME)));
        assertThat(secretConfig.getDataCiteMdsClientPassword(), is(equalTo(DATACITE_MDS_CLIENT_PASSWORD)));
        assertThat(secretConfig, doesNotHaveNullOrEmptyFields());
    }

    @Test
    void isFullyConfiguredWithEmptySecretConfigThenReturnsFalse() {
        var secretConfig = new DataCiteMdsClientSecretConfig();
        assertThat(secretConfig.isFullyConfigured(), is(equalTo(false)));
    }

    @Test
    void isFullyConfiguredWithSomeMissingSecretsThenReturnsFalse() {
        var secretConfig = createSecretConfigFullyPopulated();
        secretConfig.setDataCiteMdsClientPassword(null);
        assertThat(secretConfig.isFullyConfigured(), is(equalTo(false)));

        secretConfig.setDataCiteMdsClientPassword(EMPTY_STRING);
        assertThat(secretConfig.isFullyConfigured(), is(equalTo(false)));

        secretConfig.setDataCiteMdsClientPassword(BLANK_STRING);
        assertThat(secretConfig.isFullyConfigured(), is(equalTo(false)));
    }

    @Test
    void isFullyConfiguredWithCorrectConfigurationReturnsTrue() {
        assertThat(createSecretConfigFullyPopulated().isFullyConfigured(), is(equalTo(true)));
    }

    private DataCiteMdsClientSecretConfig createSecretConfigFullyPopulated() {
        return new DataCiteMdsClientSecretConfig(CUSTOMER_ID,
            CUSTOMER_DOI_PREFIX, DATA_CITE_MDS_CLIENT_URL,
            DATACITE_MDS_CLIENT_USERNAME, DATACITE_MDS_CLIENT_PASSWORD);
    }
}