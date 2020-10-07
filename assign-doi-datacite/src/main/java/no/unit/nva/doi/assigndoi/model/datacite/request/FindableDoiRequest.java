package no.unit.nva.doi.assigndoi.model.datacite.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.Objects;

public class FindableDoiRequest {

    private Data data;

    public Data getData() {
        return data;
    }

    @JsonCreator
    public FindableDoiRequest(Data data) {
        this.data = data;
    }

    static class Data {

        private static final String TYPE = "dois";
        private final String id;
        private final Attributes attributes;

        @JsonCreator
        public Data(String id,
                    Attributes attributes) {
            this.id = id;
            this.attributes = attributes;
        }

        public static String getEventType() {
            return TYPE;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Data data = (Data) o;
            return Objects.equals(getId(), data.getId()) &&
                getAttributes().equals(data.getAttributes());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getAttributes());
        }

        /**
         * Landing page URL.
         */
        //private URI url; // TODO needed? not duplicated of Attributes.url ?
        /**
         * Base64 encoded metadata from the detail of the consumed event
         */
        //private String xml; // TODO needed? not duplicated of Attributes.xml?

        public Attributes getAttributes() {
            return attributes;
        }
    }

    static class Attributes {
        @JsonIgnore
        public static final String SCHEMA_DATACITE_KERNEL_4_0 = "https://schema.datacite.org/meta/kernel-4.0/index.html";
        private static final String EVENT = "publish";
        private final String doi; // repeat id from response in 1
        private final URI url; //landing page URI
        private final String xml; // Base64 encoded metadata from the detail of the consumed event

        @JsonCreator
        public Attributes(String doi, URI url, String xml) {
            this.doi = doi;
            this.url = url;
            this.xml = xml;
        }

        public static String getEventType() {
            return EVENT;
        }

        public String getDoi() {
            return doi;
        }

        public URI getUrl() {
            return url;
        }

        public String getXml() {
            return xml;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Attributes that = (Attributes) o;
            return Objects.equals(getDoi(), that.getDoi()) &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getXml(), that.getXml());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getDoi(), getUrl(), getXml());
        }
    }

    public static final class AttributesBuilder {

        private String doi;
        private URI url;
        private String xml;

        private AttributesBuilder() {
        }

        public static AttributesBuilder newBuilder() {
            return new AttributesBuilder();
        }

        public AttributesBuilder withDoi(String doi) {
            this.doi = doi;
            return this;
        }

        public AttributesBuilder withURL(URI url) {
            this.url = url;
            return this;
        }

        public AttributesBuilder withXML(String base64EncodedXML) {
            this.xml = base64EncodedXML;
            return this;
        }

        public Attributes build() {
            return new Attributes(doi, url, xml);
        }
    }

    public static final class DataBuilder {

        private String id;
        private Attributes attributes;

        private DataBuilder() {
        }

        public static DataBuilder newBuilder() {
            return new DataBuilder();
        }

        public DataBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataBuilder withAttributes(AttributesBuilder attributeBuilder) {
            this.attributes = attributeBuilder.build();
            return this;
        }

        public FindableDoiRequest build() {
            return new FindableDoiRequest(new Data(id, attributes));
        }
    }
}
