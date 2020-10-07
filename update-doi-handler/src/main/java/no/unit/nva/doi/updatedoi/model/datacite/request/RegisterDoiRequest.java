package no.unit.nva.doi.updatedoi.model.datacite.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;

public class RegisterDoiRequest {
    private final Data data;

    @JsonCreator
    public RegisterDoiRequest(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    static class Data {
        private final String id;
        private final String type;
        private final Attributes attributes;

        public Data(String id, String type,
                    Attributes attributes) {
            this.id = id;
            this.type = type;
            this.attributes = attributes;
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
            return Objects.equals(id, data.id) &&
                Objects.equals(type, data.type) &&
                Objects.equals(attributes, data.attributes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type, attributes);
        }
    }

    static class Builder {
        private String id;
        private String type;
        private Attributes attributes;

        private Builder() {}

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withAttributes(Attributes attributes) {
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

        Attributes(String doi) {
            this.doi = doi;
        }

        public String getDoi() {
            return doi;
        }

        public String getEventType() {
            return EVENT;
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
            return Objects.equals(getDoi(), that.getDoi());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getDoi());
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
