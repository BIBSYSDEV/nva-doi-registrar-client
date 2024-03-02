package no.unit.nva.doi.datacite.clients;

import nva.commons.core.Environment;
import nva.commons.core.useragent.UserAgent;

import java.net.URI;

public final class UserAgentUtil {

    public static final Environment ENVIRONMENT = new Environment();

    private UserAgentUtil() {
    }

    public static String create(Class<?> clazz) {
        return UserAgent.newBuilder()
                .client(clazz)
                .environment(ENVIRONMENT.readEnv("API_HOST"))
                .email("support@sikt.no")
                .repository(URI.create("https://github.com/BIBSYSDEV/nva-doi-registrar-client"))
                .version("1.0")
                .build()
                .toString();
    }
}
