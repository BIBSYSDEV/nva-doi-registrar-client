package no.unit.nva.datacite;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigTest {

    public static final String DUMMY_DATACITE_CONFIGS = "secret";
    public static final String DUMMY_NVA_FRONTEND_HOST = "nvaFrontendHost";

    @Test
    public void testCorsHeaderNotSet() {
        final Config config = Config.getInstance();
        config.setCorsHeader(null);
        final String corsHeader = config.getCorsHeader();
        assertNull(corsHeader);
    }

    @Test
    public void testDataCiteConfigsNotSet() {
        final Config config = Config.getInstance();
        config.setDataCiteMdsConfigs(null);
        final String dataCiteMdsConfigs = config.getDataCiteMdsConfigs();
        assertNull(dataCiteMdsConfigs);
    }

    @Test
    public void testNvaHostNotSet() {
        final Config config = Config.getInstance();
        config.setNvaFrontendHost(null);
        final String nvaFrontendHost = config.getNvaFrontendHost();
        assertNull(nvaFrontendHost);
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
        instance.setDataCiteMdsConfigs(DUMMY_DATACITE_CONFIGS);
        instance.setNvaFrontendHost(DUMMY_NVA_FRONTEND_HOST);
        assertTrue(instance.checkProperties());
    }
}
