package no.unit.nva.datacite.models;

import nva.commons.utils.JacocoGenerated;

public class DataCiteMdsClientSecretConfig extends DataCiteMdsClientConfig {

    private String dataCiteMdsClientUsername;
    private String dataCiteMdsClientPassword;

    @JacocoGenerated
    public DataCiteMdsClientSecretConfig() {
        super();
    }

    /**
     * POJO for DataCite MDS API configuration.
     *
     * @param institution Institution id
     * @param institutionPrefix Provider assigned prefix
     * @param dataCiteMdsClientUrl DataCite MDS API host
     * @param dataCiteMdsClientUsername Username
     * @param dataCiteMdsClientPassword Password
     */
    public DataCiteMdsClientSecretConfig(String institution, String institutionPrefix, String dataCiteMdsClientUrl,
                                         String dataCiteMdsClientUsername, String dataCiteMdsClientPassword) {
        super(institution, institutionPrefix, dataCiteMdsClientUrl);
        this.dataCiteMdsClientUsername = dataCiteMdsClientUsername;
        this.dataCiteMdsClientPassword = dataCiteMdsClientPassword;
    }



    public String getDataCiteMdsClientUrl() {
        return dataCiteMdsClientUrl;
    }

    public void setDataCiteMdsClientUrl(String dataCiteMdsClientUrl) {
        this.dataCiteMdsClientUrl = dataCiteMdsClientUrl;
    }

    public String getDataCiteMdsClientUsername() {
        return dataCiteMdsClientUsername;
    }

    public void setDataCiteMdsClientUsername(String dataCiteMdsClientUsername) {
        this.dataCiteMdsClientUsername = dataCiteMdsClientUsername;
    }

    public String getDataCiteMdsClientPassword() {
        return dataCiteMdsClientPassword;
    }

    public void setDataCiteMdsClientPassword(String dataCiteMdsClientPassword) {
        this.dataCiteMdsClientPassword = dataCiteMdsClientPassword;
    }
}
