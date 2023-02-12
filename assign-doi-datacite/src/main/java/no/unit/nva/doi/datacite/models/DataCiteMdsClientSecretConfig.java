package no.unit.nva.doi.datacite.models;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import java.net.URI;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import nva.commons.core.JacocoGenerated;

/**
 * DAO for DataCite MDS Configuration also including DataCite MDS secrets for a associated NVA customer.
 *
 * @see DataCiteConfigurationFactory
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
     * @param customerId                NVA customerId
     * @param customerDoiPrefix         NVA customer assigned DOI prefix
     * @param dataCiteMdsClientUsername Username
     * @param dataCiteMdsClientPassword Password
     */
    public DataCiteMdsClientSecretConfig(URI customerId,
                                         String customerDoiPrefix,
                                         String dataCiteMdsClientUsername,
                                         String dataCiteMdsClientPassword) {
        super(customerId, customerDoiPrefix);
        this.dataCiteMdsClientUsername = dataCiteMdsClientUsername;
        this.dataCiteMdsClientPassword = dataCiteMdsClientPassword;
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
