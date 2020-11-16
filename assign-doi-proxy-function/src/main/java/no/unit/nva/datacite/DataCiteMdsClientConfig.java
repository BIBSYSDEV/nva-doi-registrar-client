package no.unit.nva.datacite;

import nva.commons.utils.JacocoGenerated;

/**
 * Deprecated.
 *
 * See {@code no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory} in assign-doi-datacite module.
 */
@Deprecated(forRemoval = true)
public class DataCiteMdsClientConfig {

    private String institution;
    private String institutionPrefix;
    private String dataCiteMdsClientUrl;
    private String dataCiteMdsClientUsername;
    private String dataCiteMdsClientPassword;

    /**
     * POJO for DataCite MDS API configuration.
     *
     * <p>No-arg constructor for that can be used together with setters.
     */
    @JacocoGenerated
    public DataCiteMdsClientConfig() {
    }

    /**
     * POJO for DataCite MDS API configuration.
     *
     * @param institution               Institution id
     * @param institutionPrefix         Provider assigned prefix
     * @param dataCiteMdsClientUrl      DataCite MDS API host
     * @param dataCiteMdsClientUsername Username
     * @param dataCiteMdsClientPassword Password
     */
    public DataCiteMdsClientConfig(String institution, String institutionPrefix, String dataCiteMdsClientUrl,
                                   String dataCiteMdsClientUsername, String dataCiteMdsClientPassword) {
        this.institution = institution;
        this.institutionPrefix = institutionPrefix;
        this.dataCiteMdsClientUrl = dataCiteMdsClientUrl;
        this.dataCiteMdsClientUsername = dataCiteMdsClientUsername;
        this.dataCiteMdsClientPassword = dataCiteMdsClientPassword;
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
