package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "size")
public class Size {

    @JacksonXmlText
    private String size;

    public Size(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public Size size(String size) {
        this.size = size;
        return this;
    }

}
