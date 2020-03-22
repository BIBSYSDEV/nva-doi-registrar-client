package no.unit.nva.datacite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigTest {

    public static final String DUMMY_DATACITE_CONFIGS = "secret";

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
    public void testCheckPropertiesNotSet() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            final Config config = Config.getInstance();
            config.setDataCiteMdsConfigs(null);
            config.checkProperties();
        });
    }

    @Test
    public void testCheckPropertiesSet() {
        final Config instance = Config.getInstance();
        instance.setDataCiteMdsConfigs(DUMMY_DATACITE_CONFIGS);
        assertTrue(instance.checkProperties());
    }
}
