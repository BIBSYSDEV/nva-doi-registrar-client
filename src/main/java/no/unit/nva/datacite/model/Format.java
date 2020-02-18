package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "format")
public class Format {

    @JacksonXmlText()
    private String format;

    public Format(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public Format format(String format) {
        this.format = format;
        return this;
    }

}
