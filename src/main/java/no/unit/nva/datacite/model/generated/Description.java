package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "description")
public class Description {

    @JacksonXmlProperty(localName = "xml:lang", isAttribute = true)
    private String lang;

    @JacksonXmlText()
    private String description;

    @JacksonXmlProperty(isAttribute = true)
    private String descriptionType;

    public Description() {
    }

    public Description(String description, String descriptionType) {
        this.description = description;
        this.descriptionType = descriptionType;
    }

    public Description(String lang, String description, String descriptionType) {
        this.lang = lang;
        this.description = description;
        this.descriptionType = descriptionType;
    }

    public String getLang() {
        return lang;
    }

    public Description lang(String lang) {
        this.lang = lang;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Description description(String description) {
        this.description = description;
        return this;
    }

    public String getDescriptionType() {
        return descriptionType;
    }

    public Description descriptionType(String descriptionType) {
        this.descriptionType = descriptionType;
        return this;
    }
}
