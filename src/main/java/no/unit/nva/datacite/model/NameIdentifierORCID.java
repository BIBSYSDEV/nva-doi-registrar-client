package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "nameIdentifier")
public class NameIdentifierORCID extends NameIdentifier {


    public NameIdentifierORCID() {
    }

    public NameIdentifierORCID(String nameIdentifier) {
        super(nameIdentifier);
    }

}
