package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "funderIdentifier")
public class FunderIdentifier {

    @JacksonXmlProperty(isAttribute = true)
    private String funderIdentifierType;

    @JacksonXmlText()
    private String funderIdentifier;

    public FunderIdentifier(String funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
    }

    public FunderIdentifier funderIdentifierType(String funderIdentifierType) {
        this.funderIdentifierType = funderIdentifierType;
        return this;
    }

    public FunderIdentifier funderIdentifier(String funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
        return this;
    }

    public String getFunderIdentifierType() {
        return funderIdentifierType;
    }

    public String getFunderIdentifier() {
        return funderIdentifier;
    }
}
