package no.unit.nva.datacite.handlers;

import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;

@JacocoGenerated
public final class AppEnv {

    public static final String DATACITE_CONFIG = "DATACITE_CONFIG";
    public static final String DATACITE_MDS_HOST = "DATACITE_MDS_HOST";
    public static final String DATACITE_PORT = "DATACITE_PORT";
    private static final Environment ENVIRONMENT = new Environment();
    public static final String DATACITE_REST_HOST = "DATACITE_REST_HOST";

    @JacocoGenerated
    private AppEnv() {
    }

    @JacocoGenerated
    public static String getDataCiteConfig() {
        return getEnvValue(DATACITE_CONFIG);
    }

    @JacocoGenerated
    public static String getDataCiteMdsApiHost() {
        return getEnvValue(DATACITE_MDS_HOST);
    }

    @JacocoGenerated
    public static String getDataCiteRestApiHost() {
        return getEnvValue(DATACITE_REST_HOST);
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
