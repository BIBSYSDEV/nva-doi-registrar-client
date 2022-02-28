package no.unit.nva.datacite.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Objects;

import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.JacocoGenerated;

public class DoiUpdateRequestEvent {

    public static final String TOPIC = "topic";
    public static final String ITEM = "item";

    @JsonProperty(TOPIC)
    private final String topic;
    @JsonProperty(ITEM)
    private final Item item;

    @JacocoGenerated
    @JsonCreator
    public DoiUpdateRequestEvent(
        @JsonProperty(TOPIC) String type,
        @JsonProperty(ITEM) Item item) {
        this.topic = type;
        this.item = item;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getTopic(), getItem());
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
        return Objects.equals(getTopic(), that.getTopic()) && Objects.equals(getItem(), that.getItem());
    }

    @JacocoGenerated
    public String getTopic() {
        return topic;
    }

    @JacocoGenerated
    public Item getItem() {
        return item;
    }

    public static class Item {

        private String metadata;
        private URI customerId;
        private URI doi;
        private SortableIdentifier publicationIdentifier;
        private boolean canBecomeFindable;
        private boolean doiRequested;
        private URI landingPage;

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        public URI getCustomerId() {
            return customerId;
        }

        public void setCustomerId(URI customerId) {
            this.customerId = customerId;
        }

        public URI getDoi() {
            return doi;
        }

        public void setDoi(URI doi) {
            this.doi = doi;
        }

        public SortableIdentifier getPublicationIdentifier() {
            return publicationIdentifier;
        }

        public void setPublicationIdentifier(SortableIdentifier publicationIdentifier) {
            this.publicationIdentifier = publicationIdentifier;
        }

        public boolean isCanBecomeFindable() {
            return canBecomeFindable;
        }

        public void setCanBecomeFindable(boolean canBecomeFindable) {
            this.canBecomeFindable = canBecomeFindable;
        }

        public URI getLandingPage() {
            return landingPage;
        }

        public void setLandingPage(URI landingPage) {
            this.landingPage = landingPage;
        }

        public boolean isDoiRequested() {
            return doiRequested;
        }

        public void setDoiRequested(boolean doiRequested) {
            this.doiRequested = doiRequested;
        }
    }
}
