package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "subject")
public class Subject {

    @JacksonXmlProperty(localName = "xml:lang", isAttribute = true)
    private String lang;

    @JacksonXmlProperty(isAttribute = true)
    private String schemeURI;

    @JacksonXmlProperty(isAttribute = true)
    private String subjectScheme;

    @JacksonXmlText()
    private String subject;

    public Subject() {
    }

    public Subject(String lang, String subject) {
        this.lang = lang;
        this.subject = subject;
    }

    public Subject lang(String lang) {
        this.lang = lang;
        return this;
    }

    public Subject schemeURI(String schemeURI) {
        this.schemeURI = schemeURI;
        return this;
    }

    public Subject subjectScheme(String subjectScheme) {
        this.subjectScheme = subjectScheme;
        return this;
    }

    public Subject subject(String subject) {
        this.subject = subject;
        return this;
    }


    public String getLang() {
        return lang;
    }

    public String getSchemeURI() {
        return schemeURI;
    }

    public String getSubjectScheme() {
        return subjectScheme;
    }

    public String getSubject() {
        return subject;
    }

}
