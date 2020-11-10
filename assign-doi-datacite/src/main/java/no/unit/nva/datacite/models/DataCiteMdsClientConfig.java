package no.unit.nva.datacite.models;

import nva.commons.utils.JacocoGenerated;

public class DataCiteMdsClientConfig {

    protected String institution;
    protected String institutionPrefix;
    protected String dataCiteMdsClientUrl;

    @JacocoGenerated
    public DataCiteMdsClientConfig() {
    }

    public DataCiteMdsClientConfig(String institution, String institutionPrefix, String dataCiteMdsClientUrl) {
        this.institution = institution;
        this.institutionPrefix = institutionPrefix;
        this.dataCiteMdsClientUrl = dataCiteMdsClientUrl;
    }

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
