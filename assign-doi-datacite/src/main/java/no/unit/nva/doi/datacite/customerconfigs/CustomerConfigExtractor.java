package no.unit.nva.doi.datacite.customerconfigs;

import java.net.URI;

public interface CustomerConfigExtractor {

    CustomerConfig getCustomerConfig(URI customerId) throws CustomerConfigException;

}
