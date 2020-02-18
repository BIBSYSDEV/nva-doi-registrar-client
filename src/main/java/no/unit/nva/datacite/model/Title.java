package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "title")
public class Title {

    @JacksonXmlProperty(localName = "xml:lang", isAttribute = true)
    private String lang;

    @JacksonXmlProperty(isAttribute = true)
    private String titleType;

    @JacksonXmlText
    private String title;

    public Title() {
    }

    public Title(String title) {
        this.title = title;
    }

    public Title(String lang, String titleType, String title) {
        this.lang = lang;
        this.titleType = titleType;
        this.title = title;
    }

    public Title lang(String lang) {
        this.lang = lang;
        return this;
    }

    public Title titleType(String titleType) {
        this.titleType = titleType;
        return this;
    }

    public Title title(String title) {
        this.title = title;
        return this;
    }


    public String getLang() {
        return lang;
    }

    public String getTitleType() {
        return titleType;
    }

    public String getTitle() {
        return title;
    }

}
