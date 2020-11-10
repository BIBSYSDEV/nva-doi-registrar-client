package no.unit.nva.datacite.models;

public class DataCiteMdsClientConfig {

    protected String institution;
    protected String institutionPrefix;
    protected String dataCiteMdsClientUrl;

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getInstitutionPrefix() {
        return institutionPrefix;
    }

    public void setInstitutionPrefix(String institutionPrefix) {
        this.institutionPrefix = institutionPrefix;
    }
}
