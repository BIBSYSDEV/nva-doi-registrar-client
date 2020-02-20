package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class NameIdentifier {

    @JacksonXmlProperty(isAttribute = true)
    private String schemeURI;

    @JacksonXmlProperty(isAttribute = true)
    private String nameIdentifierScheme;

    @JacksonXmlText()
    private String nameIdentifier;

    public NameIdentifier() {
    }

    public NameIdentifier(String nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    public String getSchemeURI() {
        return schemeURI;
    }

    public String getNameIdentifierScheme() {
        return nameIdentifierScheme;
    }

    public String getNameIdentifier() {
        return nameIdentifier;
    }
}
