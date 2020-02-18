package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "rights")
public class Rights {

    @JacksonXmlText()
    private String rights;

    @JacksonXmlProperty(isAttribute = true)
    private String rightsURI;

    public Rights() {
    }

    public Rights(String rights, String rightsURI) {
        this.rights = rights;
        this.rightsURI = rightsURI;
    }

    public String getRights() {
        return rights;
    }

    public Rights rights(String rights) {
        this.rights = rights;
        return this;
    }

    public String getRightsURI() {
        return rightsURI;
    }

    public Rights rightsURI(String rightsURI) {
        this.rightsURI = rightsURI;
        return this;
    }

}
