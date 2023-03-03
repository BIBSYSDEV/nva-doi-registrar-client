package no.unit.nva.datacite.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.model.Publication;
import nva.commons.core.JacocoGenerated;

public class DoiUpdateRequestEvent {

    public static final String TOPIC = "topic";
    public static final String ITEM = "item";

    @JsonProperty(TOPIC)
    private final String topic;

    @Deprecated
    @JsonProperty(ITEM)
    private final Publication item;

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
        @JsonProperty(ITEM) Publication publication,
        @JsonProperty("doi") URI doi,
        @JsonProperty("publicationId") URI publicationId,
        @JsonProperty("customerId") URI customerId) {
        this.topic = type;
        this.item = publication;
        this.doi = doi;
        this.publicationId = publicationId;
        this.customerId = customerId;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getTopic(),
                            getItem(),
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
               && Objects.equals(getItem(), that.getItem())
               && Objects.equals(getDoi(), that.getDoi())
               && Objects.equals(getCustomerId(), that.getCustomerId())
               && Objects.equals(getPublicationId(), that.getPublicationId());
    }

    @JacocoGenerated
    public String getTopic() {
        return topic;
    }

    @JacocoGenerated
    public Publication getItem() {
        return item;
    }

    @Deprecated
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
