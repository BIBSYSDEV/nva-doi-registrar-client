package no.unit.nva.datacite.clients.models;

import java.net.URI;

public abstract class Doi {

    public static final String DOI_PROXY = "https://doi.org/";
    private static final String FORWARD_SLASH = "/";

    public abstract String prefix();

    public abstract String suffix();

    /**
     * Represents the DOI with "${prefix}/${suffix}.
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
