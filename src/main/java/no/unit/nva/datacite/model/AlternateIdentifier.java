package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "alternateIdentifier")
public class AlternateIdentifier {

    @JacksonXmlText()
    private String alternateIdentifier;

    @JacksonXmlProperty(isAttribute = true)
    private String alternateIdentifierType;

    public AlternateIdentifier() {
    }

    public AlternateIdentifier(String alternateIdentifier) {
        this.alternateIdentifier = alternateIdentifier;
    }

    public AlternateIdentifier(String alternateIdentifier, String alternateIdentifierType) {
        this.alternateIdentifier = alternateIdentifier;
        this.alternateIdentifierType = alternateIdentifierType;
    }

    public String getAlternateIdentifier() {
        return alternateIdentifier;
    }

    public AlternateIdentifier alternateIdentifier(String alternateIdentifier) {
        this.alternateIdentifier = alternateIdentifier;
        return this;
    }

    public String getAlternateIdentifierType() {
        return alternateIdentifierType;
    }

    public AlternateIdentifier alternateIdentifierType(String alternateIdentifierType) {
        this.alternateIdentifierType = alternateIdentifierType;
        return this;
    }

}
