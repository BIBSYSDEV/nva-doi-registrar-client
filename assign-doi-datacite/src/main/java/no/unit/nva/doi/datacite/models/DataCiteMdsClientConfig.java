package no.unit.nva.doi.datacite.models;

import static java.util.Objects.nonNull;
import java.net.URI;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import nva.commons.core.JacocoGenerated;

/**
 * DAO for DataCite MDS Configuration for a associated NVA customer.
 *
 * @see DataCiteMdsClientSecretConfig
 * @see DataCiteConfigurationFactory
 */
public class DataCiteMdsClientConfig {

    protected URI customerId;
    protected String customerDoiPrefix;

    @JacocoGenerated
    public DataCiteMdsClientConfig() {
    }

    /**
     * Construct a {@link DataCiteMdsClientConfig}.
     *
     * @param customerId          customerId
     * @param customerDoiPrefix    customer's prefix for the NVA repository in the Registry Agency
     */
    public DataCiteMdsClientConfig(URI customerId, String customerDoiPrefix) {
        this.customerId = customerId;
        this.customerDoiPrefix = customerDoiPrefix;
    }

    public URI getCustomerId() {
        return customerId;
    }

    public void setCustomerId(URI customerId) {
        this.customerId = customerId;
    }

    public String getCustomerDoiPrefix() {
        return customerDoiPrefix;
    }

    public void setCustomerDoiPrefix(String customerDoiPrefix) {
        this.customerDoiPrefix = customerDoiPrefix;
    }

    /**
     * Is configuration fully configured with all required values.
     *
     * @return <code>true</code> if config is fully configured.
     */
    public boolean isFullyConfigured() {
        return nonNull(customerId) && nonNull(customerDoiPrefix);
    }
}
