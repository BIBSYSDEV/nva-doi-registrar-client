package no.unit.nva.doi.datacite.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

public class DataCiteMdsClientSecretConfigTest {

    public static final String INSTITUTION = "institution";
    public static final String INSTITUTION_PREFIX = "institutionPrefix";
    public static final String DATA_CITE_MDS_CLIENT_URL = "dataCiteMdsClientUrl";
    public static final String DATACITE_MDS_CLIENT_USERNAME = "dataCiteMdsClientUsername";
    public static final String DATACITE_MDS_CLIENT_PASSWORD = "dataCiteMdsClientPassword";
    private static final String EMPTY_STRING = "";
    private static final String BLANK_STRING = "    ";

    @Test
    void testConstructor() {
        var secretConfig = createSecretConfigFullyPopulated();
        assertThat(secretConfig.getInstitution(), is(equalTo(INSTITUTION)));
        assertThat(secretConfig.getInstitutionPrefix(), is(equalTo(INSTITUTION_PREFIX)));
        assertThat(secretConfig.getDataCiteMdsClientUrl(), is(equalTo(DATA_CITE_MDS_CLIENT_URL)));
        assertThat(secretConfig.getDataCiteMdsClientUsername(), is(equalTo(DATACITE_MDS_CLIENT_USERNAME)));
        assertThat(secretConfig.getDataCiteMdsClientPassword(), is(equalTo(DATACITE_MDS_CLIENT_PASSWORD)));
    }

    @Test
    void testSetters() {
        DataCiteMdsClientSecretConfig secretConfig = new DataCiteMdsClientSecretConfig();
        secretConfig.setInstitution(INSTITUTION);
        secretConfig.setInstitutionPrefix(INSTITUTION_PREFIX);
        secretConfig.setDataCiteMdsClientUrl(DATA_CITE_MDS_CLIENT_URL);
        secretConfig.setDataCiteMdsClientUsername(DATACITE_MDS_CLIENT_USERNAME);
        secretConfig.setDataCiteMdsClientPassword(DATACITE_MDS_CLIENT_PASSWORD);
        assertThat(secretConfig.getInstitution(), is(equalTo(INSTITUTION)));
        assertThat(secretConfig.getInstitutionPrefix(), is(equalTo(INSTITUTION_PREFIX)));
        assertThat(secretConfig.getDataCiteMdsClientUrl(), is(equalTo(DATA_CITE_MDS_CLIENT_URL)));
        assertThat(secretConfig.getDataCiteMdsClientUsername(), is(equalTo(DATACITE_MDS_CLIENT_USERNAME)));
        assertThat(secretConfig.getDataCiteMdsClientPassword(), is(equalTo(DATACITE_MDS_CLIENT_PASSWORD)));
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
        return new DataCiteMdsClientSecretConfig(INSTITUTION,
            INSTITUTION_PREFIX, DATA_CITE_MDS_CLIENT_URL,
            DATACITE_MDS_CLIENT_USERNAME, DATACITE_MDS_CLIENT_PASSWORD);
    }
}