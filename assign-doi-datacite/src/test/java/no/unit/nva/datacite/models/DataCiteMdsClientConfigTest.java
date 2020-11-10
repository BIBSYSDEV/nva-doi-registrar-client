package no.unit.nva.datacite.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

class DataCiteMdsClientConfigTest {

    private static final String EXAMPLE_INSTITUTION = "https://example.net/customer/id/123";
    private static final String DEMO_PREFIX = "10.5072";
    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_MDS_CLIENT_URL = "https://example.net/datacite/mds/api";

    @Test
    void testConstructor() {
        var config = new DataCiteMdsClientConfig(EXAMPLE_INSTITUTION,
            EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_MDS_CLIENT_URL);
        assertThat(config.getInstitution(), is(equalTo(EXAMPLE_INSTITUTION)));
        assertThat(config.getInstitutionPrefix(), is(equalTo(EXAMPLE_INSTITUTION_PREFIX)));
    }

    @Test
    void testSetters() {
        var config = new DataCiteMdsClientConfig();
        config.setInstitution(EXAMPLE_INSTITUTION);
        config.setInstitutionPrefix(EXAMPLE_INSTITUTION_PREFIX);
        assertThat(config.getInstitution(), is(equalTo(EXAMPLE_INSTITUTION)));
        assertThat(config.getInstitutionPrefix(), is(equalTo(EXAMPLE_INSTITUTION_PREFIX)));
    }
}