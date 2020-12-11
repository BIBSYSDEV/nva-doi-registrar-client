package no.unit.nva.datacite.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Predicate;
import nva.commons.utils.StringUtils;

/**
 * Utility class to generate landing page URI for NVA publications.
 */
public final class LandingPageUtil {

    public static final String LANDING_PAGE_PATH_FORMAT = "/registration/%s/public";
    public static final String ERROR_PUBLICATION_LANDING_PAGE_COULD_NOT_BE_CONSTRUCTED = "Landing page could not be "
        + "constructed";
    public static final char PATH_SEPARATOR = '/';

    private LandingPageUtil() {
    }

    /**
     * Create publication landing page URI.
     *
     * @param publicationId IRI for publication.
     * @return landing page for publication.
     */
    public static URI getLandingPage(URI publicationId) {
        return Optional.of(publicationId)
            .filter(checkUriHasPath())
            .map(LandingPageUtil::buildLandingPageUrl)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_PUBLICATION_LANDING_PAGE_COULD_NOT_BE_CONSTRUCTED));
    }

    private static URI buildLandingPageUrl(URI publicationId) {
        String path = publicationId.getPath();
        int beginIndex = path.lastIndexOf(PATH_SEPARATOR);
        var publicationIdentifier = path.substring(++beginIndex);

        try {
            return createLandingPageUri(publicationId, publicationIdentifier);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(ERROR_PUBLICATION_LANDING_PAGE_COULD_NOT_BE_CONSTRUCTED, e);
        }
    }

    private static URI createLandingPageUri(URI uri, String publicationIdentifier) throws URISyntaxException {
        String landingPagePath = String.format(LANDING_PAGE_PATH_FORMAT, publicationIdentifier);
        return new URI(uri.getScheme(),
            uri.getUserInfo(),
            uri.getHost(),
            uri.getPort(),
            landingPagePath,
            null,
            null);
    }

    private static Predicate<URI> checkUriHasPath() {
        return uri -> StringUtils.isNotEmpty(uri.getPath());
    }
}
