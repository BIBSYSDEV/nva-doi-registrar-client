package no.unit.nva.datacite.handlers;

import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;

@JacocoGenerated
public final class AppEnv {

    public static final String DATACITE_CONFIG = "DATACITE_CONFIG";
    public static final String DATACITE_HOST = "DATACITE_HOST";
    public static final String DATACITE_PORT = "DATACITE_PORT";
    private static final Environment ENVIRONMENT = new Environment();

    @JacocoGenerated
    private AppEnv() {
    }

    @JacocoGenerated
    public static String getDataCiteConfig() {
        return getEnvValue(DATACITE_CONFIG);
    }

    @JacocoGenerated
    public static String getDataCiteHost() {
        return getEnvValue(DATACITE_HOST);
    }

    @JacocoGenerated
    public static int getDataCitePort() {
        return Integer.parseInt(getEnvValue(DATACITE_PORT));
    }

    @JacocoGenerated
    private static String getEnvValue(final String name) {
        return ENVIRONMENT.readEnv(name);
    }
}
