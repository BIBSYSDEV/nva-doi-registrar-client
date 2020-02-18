package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "relatedIdentifier")
public class RelatedIdentifier {

    @JacksonXmlText()
    private String relatedIdentifier;

    @JacksonXmlProperty(isAttribute = true)
    private String relatedIdentifierType;

    @JacksonXmlProperty(isAttribute = true)
    private String relationType;

    @JacksonXmlProperty(isAttribute = true)
    private String relatedMetadataScheme;

    @JacksonXmlProperty(isAttribute = true)
    private String schemeURI;

    public RelatedIdentifier() {
    }

    public RelatedIdentifier(String relatedIdentifier) {
        this.relatedIdentifier = relatedIdentifier;
    }

    public RelatedIdentifier(String relatedIdentifier, String relatedIdentifierType, String relationType) {
        this.relatedIdentifier = relatedIdentifier;
        this.relatedIdentifierType = relatedIdentifierType;
        this.relationType = relationType;
    }

    public RelatedIdentifier(String relatedIdentifier, String relatedIdentifierType, String relationType, String relatedMetadataScheme, String schemeURI) {
        this.relatedIdentifier = relatedIdentifier;
        this.relatedIdentifierType = relatedIdentifierType;
        this.relationType = relationType;
        this.relatedMetadataScheme = relatedMetadataScheme;
        this.schemeURI = schemeURI;
    }

    public String getRelatedIdentifier() {
        return relatedIdentifier;
    }

    public RelatedIdentifier relatedIdentifier(String relatedIdentifier) {
        this.relatedIdentifier = relatedIdentifier;
        return this;
    }

    public String getRelatedIdentifierType() {
        return relatedIdentifierType;
    }

    public RelatedIdentifier relatedIdentifierType(String relatedIdentifierType) {
        this.relatedIdentifierType = relatedIdentifierType;
        return this;
    }

    public String getRelationType() {
        return relationType;
    }

    public RelatedIdentifier relationType(String relationType) {
        this.relationType = relationType;
        return this;
    }

    public String getRelatedMetadataScheme() {
        return relatedMetadataScheme;
    }

    public RelatedIdentifier relatedMetadataScheme(String relatedMetadataScheme) {
        this.relatedMetadataScheme = relatedMetadataScheme;
        return this;
    }

    public String getSchemeURI() {
        return schemeURI;
    }

    public RelatedIdentifier schemeURI(String schemeURI) {
        this.schemeURI = schemeURI;
        return this;
    }

}
