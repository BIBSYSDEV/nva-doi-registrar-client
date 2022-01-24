package no.unit.nva.transformer.dto;

import java.util.List;
import no.unit.nva.transformer.Transformer;

public class DataCiteMetadataDto {
    private final IdentifierDto identifier;
    private final List<CreatorDto> creator;
    private final TitleDto title;
    private final PublisherDto publisher;
    private final String publicationYear;
    private final ResourceTypeDto resourceType;

    private DataCiteMetadataDto(Builder builder) {
        identifier = builder.identifier;
        creator = builder.creator;
        title = builder.title;
        publisher = builder.publisher;
        publicationYear = builder.publicationYear;
        resourceType = builder.resourceType;
    }

    public String asXml() {
        return new Transformer(this).asXml();
    }

    public IdentifierDto getIdentifier() {
        return identifier;
    }

    public List<CreatorDto> getCreator() {
        return creator;
    }

    public TitleDto getTitle() {
        return title;
    }

    public PublisherDto getPublisher() {
        return publisher;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public ResourceTypeDto getResourceType() {
        return resourceType;
    }

    public static final class Builder {
        private IdentifierDto identifier;
        private List<CreatorDto> creator;
        private TitleDto title;
        private PublisherDto publisher;
        private String publicationYear;
        private ResourceTypeDto resourceType;

        public Builder() {
        }

        public Builder withIdentifier(IdentifierDto identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withCreator(List<CreatorDto> creator) {
            this.creator = creator;
            return this;
        }

        public Builder withTitle(TitleDto title) {
            this.title = title;
            return this;
        }

        public Builder withPublisher(PublisherDto publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder withPublicationYear(String publicationYear) {
            this.publicationYear = publicationYear;
            return this;
        }

        public Builder withResourceType(ResourceTypeDto resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public DataCiteMetadataDto build() {
            return new DataCiteMetadataDto(this);
        }
    }
}
