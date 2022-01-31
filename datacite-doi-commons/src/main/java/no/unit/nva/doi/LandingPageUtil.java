package no.unit.nva.doi;

import java.net.URI;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;

/**
 * Utility class to generate landing page URI for NVA publications.
 */
public final class LandingPageUtil {

    public static final String PATH_TO_REGISTRATIONS = "publication";
    public static final String API_HOST = new Environment().readEnv("API_HOST");

    private LandingPageUtil() {

    }

    public static URI publicationFrontPage(SortableIdentifier publicationIdentifier) {
        return UriWrapper.fromHost(API_HOST)
            .addChild(PATH_TO_REGISTRATIONS)
            .addChild(publicationIdentifier.toString())
            .getUri();
    }
}