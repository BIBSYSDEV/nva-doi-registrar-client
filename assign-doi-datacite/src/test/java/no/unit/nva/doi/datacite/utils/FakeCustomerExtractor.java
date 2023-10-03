package no.unit.nva.doi.datacite.utils;

import java.net.URI;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigExtractor;

public class FakeCustomerExtractor implements CustomerConfigExtractor {

    private CustomerConfig customerConfig;

    @Override
    public CustomerConfig getCustomerConfig(URI customerId) {
        return customerConfig;
    }

    public void setCustomerConfig(CustomerConfig customerConfig) {
        this.customerConfig = customerConfig;
    }
}
