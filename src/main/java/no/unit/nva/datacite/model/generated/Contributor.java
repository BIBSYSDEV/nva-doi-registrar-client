package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "contributor")
public class Contributor {

    @JacksonXmlProperty(isAttribute = true)
    private String contributorType;
    private String contributorName;
    private NameIdentifier nameIdentifier;
    private String affiliation;

    public Contributor contributorType(String contributorType) {
        this.contributorType = contributorType;
        return this;
    }

    public Contributor contributorName(String contributorName) {
        this.contributorName = contributorName;
        return this;
    }

    public Contributor nameIdentifier(NameIdentifier nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
        return this;
    }

    public Contributor affiliation(String affiliation) {
        this.affiliation = affiliation;
        return this;
    }

    public String getContributorType() {
        return contributorType;
    }

    public String getContributorName() {
        return contributorName;
    }

    public NameIdentifier getNameIdentifier() {
        return nameIdentifier;
    }

    public String getAffiliation() {
        return affiliation;
    }
}
