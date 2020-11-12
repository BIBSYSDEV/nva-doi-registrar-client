package no.unit.nva.datacite.models;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import nva.commons.utils.JacocoGenerated;

/**
 * DAO for Datacite MDS Configuration also including Datacite MDS secrets for a associated NVA customer.
 *
 * @see no.unit.nva.datacite.config.DataciteConfigurationFactory
 */
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
     * @param institution               Institution id
     * @param institutionPrefix         Provider assigned prefix
     * @param dataCiteMdsClientUrl      DataCite MDS API host
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

    /**
     * Is configuration fully configured with config secrets with all required values and {@inheritDoc}.
     *
     * @return <code>true</code> if fully configured with required config and secret config.
     */
    @Override
    public boolean isFullyConfigured() {
        // TODO Update with changes from https://github.com/BIBSYSDEV/nva-commons/pull/126
        return super.isFullyConfigured()
            && isNotBlank(dataCiteMdsClientUsername)
            && isNotBlank(dataCiteMdsClientPassword);
    }
}
