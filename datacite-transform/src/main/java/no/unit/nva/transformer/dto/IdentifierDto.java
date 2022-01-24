package no.unit.nva.transformer.dto;


import org.datacide.schema.kernel_4.Resource.Identifier;

public class IdentifierDto {
    private final String value;
    private static final String type = "URL";

    public IdentifierDto(String value) {
        this.value = value;
    }

    private IdentifierDto(Builder builder) {
        this(builder.value);
    }

    /**
     * Creates a Datacite Identifier representation of the object.
     * @return A Datacite Identifier.
     */
    public Identifier asIdentifier() {
        Identifier identifier = new Identifier();
        identifier.setIdentifierType(type);
        identifier.setValue(value);
        return identifier;
    }


    public static final class Builder {
        private String value;

        public Builder() {
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public IdentifierDto build() {
            return new IdentifierDto(this);
        }
    }
}
