package no.unit.nva.doi.assigndoi.model.event;

import java.net.URI;

public class DoiUpdate {

    private URI doi;
    private URI publicationId;

    DoiUpdate(URI publicationId, URI doi) {
        this.doi = doi;
        this.publicationId = publicationId;
    }

    public static final class Builder {

        private URI doi;
        private URI publicationId;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withDoi(URI doi) {
            this.doi = doi;
            return this;
        }

        public Builder withPublicationId(URI publicationId) {
            this.publicationId = publicationId;
            return this;
        }

        public DoiUpdate build() {
            return new DoiUpdate(publicationId, doi);
        }
    }
}
