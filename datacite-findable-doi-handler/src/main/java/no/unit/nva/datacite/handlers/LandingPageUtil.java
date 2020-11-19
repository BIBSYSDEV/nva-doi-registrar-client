package no.unit.nva.datacite.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class LandingPageUtil {
    static final String PUBLICATION_LANDING_PAGE_COULD_NOT_BE_CONSTRUCTED = "Landing page could not be "
        + "constructed";
    public static final String LANDING_PAGE_PATH_FORMAT = "/registration/%s/public";

    private LandingPageUtil() {
    }
    protected static URI getLandingPage(URI publicationId) {
        return Optional.of(publicationId)
            .map(LandingPageUtil::buildLandingPageUrl)
            .orElseThrow(() -> new IllegalArgumentException(PUBLICATION_LANDING_PAGE_COULD_NOT_BE_CONSTRUCTED));
    }

    static URI buildLandingPageUrl(URI publicationId) {
        String path = publicationId.getPath();
        int beginIndex = path.lastIndexOf("/");
        var publicationIdentifier = path.substring(++beginIndex);

        try {
            return createLandingPageUri(publicationId, publicationIdentifier);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(PUBLICATION_LANDING_PAGE_COULD_NOT_BE_CONSTRUCTED, e);
        }
    }

    static URI createLandingPageUri(URI uri, String publicationIdentifier) throws URISyntaxException {
        String landingPagePath = String.format(LANDING_PAGE_PATH_FORMAT, publicationIdentifier);
        return new URI(uri.getScheme(),
            uri.getUserInfo(),
            uri.getHost(),
            uri.getPort(),
            landingPagePath,
            null,
            null);
    }
}
