package no.unit.nva.datacite.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class DoiUpdateRequestEvent {

    public static final String TOPIC = "topic";

    @JsonProperty(TOPIC)
    private final String topic;

    @JsonProperty("doi")
    private final URI doi;

    @JsonProperty("publicationId")
    private final URI publicationId;

    @JsonProperty("customerId")
    private final URI customerId;

    @JacocoGenerated
    @JsonCreator
    public DoiUpdateRequestEvent(
        @JsonProperty(TOPIC) String type,
        @JsonProperty("doi") URI doi,
        @JsonProperty("publicationId") URI publicationId,
        @JsonProperty("customerId") URI customerId) {
        this.topic = type;
        this.doi = doi;
        this.publicationId = publicationId;
        this.customerId = customerId;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getTopic(),
                            getDoi(),
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
        return Objects.equals(getTopic(), that.getTopic())
               && Objects.equals(getDoi(), that.getDoi())
               && Objects.equals(getCustomerId(), that.getCustomerId())
               && Objects.equals(getPublicationId(), that.getPublicationId());
    }

    @JacocoGenerated
    public String getTopic() {
        return topic;
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
