package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "identifier")
public class Identifier {

    @JacksonXmlProperty(isAttribute = true)
    private String identifierType = "DOI";

    @JacksonXmlText()
    private String identifier;

    public Identifier() {
    }

    public Identifier(String identifier) {
        this.identifier = identifier;
    }

    public Identifier(String identifierType, String identifier) {
        this.identifierType = identifierType;
        this.identifier = identifier;
    }

    public Identifier identifierType(String identifierType) {
        this.identifierType = identifierType;
        return this;
    }

    public Identifier identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public String getIdentifier() {
        return identifier;
    }

}
