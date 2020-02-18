package no.unit.nva.datacite;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void testCorsHeaderNotSet() {
        final Config config = Config.getInstance();
        config.setCorsHeader(null);
        final String corsHeader = config.getCorsHeader();
        assertNull(corsHeader);
    }

}
