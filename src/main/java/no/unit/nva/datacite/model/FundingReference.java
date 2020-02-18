package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "fundingReference")
public class FundingReference {

    private String funderName;
    private FunderIdentifier funderIdentifier;
    private AwardNumber awardNumber;
    private String awardTitle;

    public FundingReference(String funderName) {
        this.funderName = funderName;
    }

    public FundingReference funderName(String funderName) {
        this.funderName = funderName;
        return this;
    }

    public FundingReference funderIdentifier(FunderIdentifier funderIdentifier) {
        this.funderIdentifier = funderIdentifier;
        return this;
    }

    public FundingReference awardNumber(AwardNumber awardNumber) {
        this.awardNumber = awardNumber;
        return this;
    }

    public FundingReference awardTitle(String awardTitle) {
        this.awardTitle = awardTitle;
        return this;
    }

    public String getFunderName() {
        return funderName;
    }

    public FunderIdentifier getFunderIdentifier() {
        return funderIdentifier;
    }

    public AwardNumber getAwardNumber() {
        return awardNumber;
    }

    public String getAwardTitle() {
        return awardTitle;
    }
}
