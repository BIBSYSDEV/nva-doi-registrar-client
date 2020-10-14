package no.unit.nva.events.examples;

import java.net.URI;
import java.util.Objects;
import no.unit.nva.events.handlers.JsonSerializable;
import nva.commons.utils.JacocoGenerated;

public class DataciteDoiRequest implements JsonSerializable {

    private URI publicationId;
    private URI existingDoi;
    private String xml;
    private String type;

    @JacocoGenerated
    public DataciteDoiRequest() {
    }

    private DataciteDoiRequest(Builder builder) {
        setPublicationId(builder.publicationId);
        setExistingDoi(builder.existingDoi);
        setXml(builder.xml);
        setType(builder.type);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URI getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(URI publicationId) {
        this.publicationId = publicationId;
    }

    public URI getExistingDoi() {
        return existingDoi;
    }

    public void setExistingDoi(URI existingDoi) {
        this.existingDoi = existingDoi;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return toJsonString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataciteDoiRequest that = (DataciteDoiRequest) o;
        return Objects.equals(getPublicationId(), that.getPublicationId())
            && Objects.equals(getExistingDoi(), that.getExistingDoi())
            && Objects.equals(getXml(), that.getXml())
            && Objects.equals(getType(), that.getType());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getPublicationId(), getExistingDoi(), getXml(), getType());
    }

    /**
     * Deep copy of object.
     *
     * @return a builder.
     */
    public DataciteDoiRequest.Builder copy() {
        return DataciteDoiRequest.newBuilder()
            .withExistingDoi(getExistingDoi())
            .withPublicationId(getPublicationId())
            .withXml(getXml())
            .withType(getType());
    }

    public static final class Builder {

        private URI publicationId;
        private URI existingDoi;
        private String xml;
        private String type;

        private Builder() {
        }

        public Builder withPublicationId(URI publicationId) {
            this.publicationId = publicationId;
            return this;
        }

        public Builder withExistingDoi(URI existingDoi) {
            this.existingDoi = existingDoi;
            return this;
        }

        public Builder withXml(String xml) {
            this.xml = xml;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public DataciteDoiRequest build() {
            return new DataciteDoiRequest(this);
        }
    }
}
