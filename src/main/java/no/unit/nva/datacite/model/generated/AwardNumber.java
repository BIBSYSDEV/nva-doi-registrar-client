package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "awardNumber")
public class AwardNumber {

    @JacksonXmlProperty(isAttribute = true)
    private String awardURI;

    @JacksonXmlText()
    private String awardNumber;

    public AwardNumber(String awardNumber) {
        this.awardNumber = awardNumber;
    }

    public AwardNumber awardURI(String awardURI) {
        this.awardURI = awardURI;
        return this;
    }

    public AwardNumber awardNumber(String awardNumber) {
        this.awardNumber = awardNumber;
        return this;
    }

    public String getAwardURI() {
        return awardURI;
    }

    public String getAwardNumber() {
        return awardNumber;
    }
}
