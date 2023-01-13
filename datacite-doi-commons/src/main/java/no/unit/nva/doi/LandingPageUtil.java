package no.unit.nva.doi;

import static nva.commons.core.paths.UriWrapper.HTTPS;
import java.net.URI;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;

/**
 * Utility class to generate landing page URI for NVA publications.
 */
public final class LandingPageUtil {

    private static final String PATH_TO_REGISTRATIONS = "registration";
    private static final String FRONTEND_DOMAIN = new Environment().readEnv("FRONTEND_DOMAIN");

    private LandingPageUtil() {

    }

    public static URI publicationFrontPage(SortableIdentifier publicationIdentifier) {
        return new UriWrapper(HTTPS, FRONTEND_DOMAIN)
            .addChild(PATH_TO_REGISTRATIONS)
            .addChild(publicationIdentifier.toString())
            .getUri();
    }
}