package no.unit.nva.datacite.handlers.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import nva.commons.core.JacocoGenerated;

public class UpdateDoiRequest {

    @JsonProperty("doi")
    private final URI doi;

    @JsonProperty("publicationId")
    private final URI publicationId;

    @JsonProperty("customerId")
    private final URI customerId;

    @JacocoGenerated
    @JsonCreator
    public UpdateDoiRequest(
        @JsonProperty("doi") URI doi,
        @JsonProperty("publicationId") URI publicationId,
        @JsonProperty("customerId") URI customerId) {
        this.doi = doi;
        this.publicationId = publicationId;
        this.customerId = customerId;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getDoi(),
                            getPublicationId(),
                            getCustomerId());
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoiUpdateRequestEvent)) {
            return false;
        }
        DoiUpdateRequestEvent that = (DoiUpdateRequestEvent) o;
        return Objects.equals(getDoi(), that.getDoi())
               && Objects.equals(getCustomerId(), that.getCustomerId())
               && Objects.equals(getPublicationId(), that.getPublicationId());
    }

    @JacocoGenerated
    public URI getDoi() {
        return doi;
    }

    @JacocoGenerated
    public URI getPublicationId() {
        return publicationId;
    }

    @JacocoGenerated
    public URI getCustomerId() {
        return customerId;
    }
}
