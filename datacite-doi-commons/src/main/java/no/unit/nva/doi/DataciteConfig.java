package no.unit.nva.doi;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

@JacocoGenerated
public final class DataciteConfig {

    private static final Environment ENVIRONMENT = new Environment();
    private static final int DATACITE_PORT = readDatacitePort();
    public static final URI DATACITE_REST_URI = setupDataciteRestUri();
    public static final URI DATACITE_MDS_URI = setupDataciteMdsUri();
    private DataciteConfig() {

    }

    private static Integer readDatacitePort() {
        return ENVIRONMENT.readEnvOpt("DATACITE_PORT")
            .map(Integer::parseInt)
            .orElse(443);
    }

    private static URI setupDataciteMdsUri() {
        return attempt(() -> ENVIRONMENT.readEnv("DATACITE_MDS_HOST"))
            .map(DataciteConfig::newUri)
            .orElseThrow();
    }

    private static URI setupDataciteRestUri() {
        return attempt(() -> ENVIRONMENT.readEnv("DATACITE_REST_HOST"))
            .map(DataciteConfig::newUri)
            .orElseThrow();
    }

    private static URI newUri(String host) {
        return UriWrapper.fromHost(host, DATACITE_PORT).getUri();
    }
}
