package no.unit.nva.doi.updatedoi.model.datacite.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DraftDoiRequest {
    @JsonProperty
    private final Data data;

    @JsonCreator
    public DraftDoiRequest(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    static class Data {
        private static final String TYPE = "dois";
        private final Attributes attributes;

        @JsonCreator
        public Data(Attributes attributes) {
            this.attributes = attributes;
        }

        public static String getType() {
            return TYPE;
        }

        public Attributes getAttributes() {
            return attributes;
        }
    }

    public static final class DataBuilder {
        private Attributes attributes;

        private DataBuilder() {}

        public static DataBuilder newBuilder() {
            return new DataBuilder();
        }

        public DataBuilder withAttributes(AttributesBuilder attributesBuilder) {
            this.attributes = attributesBuilder.build();
                return this;
        }

        public DraftDoiRequest build() {
            return new DraftDoiRequest(new Data(attributes));

        }
    }

    static class Attributes {
        /**
         * Institution prefix.
         */
        private final String prefix;

        @JsonCreator
        public Attributes(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public static final class AttributesBuilder {
        private String prefix;

        private AttributesBuilder() {}

        public static AttributesBuilder newBuilder() {
            return new AttributesBuilder();
        }

        public AttributesBuilder withPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Attributes build() {
            return new Attributes(prefix);
        }
    }
}



