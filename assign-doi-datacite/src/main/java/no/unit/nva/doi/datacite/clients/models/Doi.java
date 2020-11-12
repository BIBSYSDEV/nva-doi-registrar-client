package no.unit.nva.doi.datacite.clients.models;

import java.net.URI;

/**
 * Doi class for working with Dois.
 *
 * <p>Use {@link Doi#builder()} for constructing a new Doi instance.
 */
public abstract class Doi {

    public static final String DOI_PROXY = "https://doi.org/";
    private static final String FORWARD_SLASH = "/";

    public static ImmutableDoi.Builder builder() {
        return ImmutableDoi.builder();
    }

    public abstract String prefix();

    public abstract String suffix();

    /**
     * Represents the DOI with ${prefix}/${suffix}.
     *
     * @return prefix/suffix (DOI identifier)
     */
    public String toIdentifier() {
        return prefix() + FORWARD_SLASH + suffix();
    }

    /**
     * Represents the DOI as an URI, this includes proxy, prefix and suffix.
     *
     * @return DOI as URI with proxy, prefix and suffix.
     */
    public URI toId() {
        return URI.create(DOI_PROXY + prefix() + FORWARD_SLASH + suffix());
    }
}
