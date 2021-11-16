package no.unit.nva.datacite.handlers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.JacocoGenerated;

public class ResourceDraftedForDeletionEvent {

    public static final String TOPIC = "topic";
    public static final String IDENTIFIER = "identifier";
    public static final String STATUS = "status";
    public static final String DOI = "doi";
    public static final String CUSTOMER_ID = "customerId";

    @JsonProperty(TOPIC)
    private final String topic;
    @JsonProperty(IDENTIFIER)
    private final SortableIdentifier identifier;
    @JsonProperty(STATUS)
    private final String status;
    @JsonProperty(DOI)
    private final URI doi;
    @JsonProperty(CUSTOMER_ID)
    private final URI customerId;

    /**
     * Constructor for DeletePublicationEvent.
     *
     * @param type       type
     * @param identifier identifier
     * @param status     status
     * @param doi        doi
     * @param customerId customerId
     */
    @JsonCreator
    public ResourceDraftedForDeletionEvent(
        @JsonProperty(TOPIC) String type,
        @JsonProperty(IDENTIFIER) SortableIdentifier identifier,
        @JsonProperty(STATUS) String status,
        @JsonProperty(DOI) URI doi,
        @JsonProperty(CUSTOMER_ID) URI customerId) {
        this.topic = type;
        this.identifier = identifier;
        this.status = status;
        this.doi = doi;
        this.customerId = customerId;
    }

    public String getTopic() {
        return topic;
    }

    public SortableIdentifier getIdentifier() {
        return identifier;
    }

    public String getStatus() {
        return status;
    }

    public URI getDoi() {
        return doi;
    }

    public URI getCustomerId() {
        return customerId;
    }

    @JsonProperty("hasDoi")
    public boolean hasDoi() {
        return Objects.nonNull(doi);
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(topic, identifier, status, doi, customerId);
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceDraftedForDeletionEvent that = (ResourceDraftedForDeletionEvent) o;
        return topic.equals(that.topic)
               && identifier.equals(that.identifier)
               && status.equals(that.status)
               && Objects.equals(doi, that.doi)
               && Objects.equals(customerId, that.customerId);
    }
}
