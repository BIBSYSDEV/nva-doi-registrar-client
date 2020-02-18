package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "creator")
public class Creator {

    private String creatorName;
    private String givenName;
    private String familyName;
    private NameIdentifier nameIdentifier;
    private String affiliation;

    public Creator creatorName(String creatorName) {
        this.creatorName = creatorName;
        return this;
    }

    public Creator givenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public Creator familyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public Creator nameIdentifier(NameIdentifier nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
        return this;
    }

    public Creator affiliation(String affiliation) {
        this.affiliation = affiliation;
        return this;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public NameIdentifier getNameIdentifier() {
        return nameIdentifier;
    }

    public String getAffiliation() {
        return affiliation;
    }
}
