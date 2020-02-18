package no.unit.nva.datacite;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

    public static final String DUMMY_DATACITE_MDS_CLIENT_CONFIGS = "[{\"institution\": \"institution\",\"institutionPrefix\": \"institutionPrefix\",\"dataCiteMdsClient_url\": \"dataCiteMdsClient_url\",\"dataCiteMdsClient_username\": \"dataCiteMdsClient_username\",\"dataCiteMdsClient_password\": \"dataCiteMdsClient_password\"}]";

    @Test
    public void testCorsHeaderNotSet() {
        final Config config = Config.getInstance();
        config.setCorsHeader(null);
        final String corsHeader = config.getCorsHeader();
        assertNull(corsHeader);
    }

    @Test(expected = RuntimeException.class)
    public void testCheckPropertiesNotSet() {
        final Config config = Config.getInstance();
        config.setDataCiteMdsConfigs(null);
        config.checkProperties();
        fail();
    }

    @Test
    public void testCheckPropertiesSet() {
        final Config instance = Config.getInstance();
        instance.setDataCiteMdsConfigs(DUMMY_DATACITE_MDS_CLIENT_CONFIGS);
        assertTrue(instance.checkProperties());
    }
}
