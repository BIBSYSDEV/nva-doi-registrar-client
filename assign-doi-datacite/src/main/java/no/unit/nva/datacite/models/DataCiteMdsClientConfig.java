package no.unit.nva.datacite.models;

import nva.commons.utils.JacocoGenerated;

/**
 * DAO for Datacite MDS Configuration for a associated NVA customer.
 *
 * @see DataCiteMdsClientSecretConfig
 * @see no.unit.nva.datacite.config.DataciteConfigurationFactory
 */
public class DataCiteMdsClientConfig {

    protected String institution;
    protected String institutionPrefix;
    protected String dataCiteMdsClientUrl;

    @JacocoGenerated
    public DataCiteMdsClientConfig() {
    }

    /**
     * Construct a {@link DataCiteMdsClientConfig}.
     *
     * @param institution          customerId
     * @param institutionPrefix    customer's prefix for the NVA repository in the Registry Agency
     * @param dataCiteMdsClientUrl Hostname to MDS API environment
     */
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
