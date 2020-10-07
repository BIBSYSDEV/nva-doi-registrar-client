package no.unit.nva.doi.assigndoi.model.datacite.request;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RegisterDoiRequest {
    private final Data data;

    @JsonCreator
    public RegisterDoiRequest(Data data) {
        this.data = data;
    }

    private static class Data {
        private final String id;
        private final String type;
        private final Attributes attributes;

        public Data(String id, String type,
                    Attributes attributes) {
            this.id = id;
            this.type = type;
            this.attributes = attributes;
        }
    }

    static class DataBuilder {
        private String id;
        private String type;
        private Attributes attributes;

        private DataBuilder() {}

        public static DataBuilder newBuilder() {
            return new DataBuilder();
        }

        public DataBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public DataBuilder withAttributes(Attributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public RegisterDoiRequest build() {
            return new RegisterDoiRequest(new Data(id, type, attributes));
        }
    }

    static class Attributes {
        private final String doi;
        private static final String EVENT = "register";

        private Attributes(String doi) {
            this.doi = doi;
        }

        public String getDoi() {
            return doi;
        }

        public String getEventType() {
            return EVENT;
        }
    }

    static class AttributesBuilder {
        private String doi;

        private AttributesBuilder() {};

        public static AttributesBuilder newBuilder() {
            return new AttributesBuilder();
        }

        public AttributesBuilder withDoi(String doi) {
            this.doi = doi;
            return this;
        }

        public Attributes build() {
            return new Attributes(doi);
        }
    }


}
