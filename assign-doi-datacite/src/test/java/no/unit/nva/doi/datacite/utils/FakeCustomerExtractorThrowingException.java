package no.unit.nva.doi.datacite.utils;

import java.net.URI;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigExtractor;

public class FakeCustomerExtractorThrowingException implements CustomerConfigExtractor {

    @Override
    public CustomerConfig getCustomerConfig(URI customerId) throws CustomerConfigException {
        throw new CustomerConfigException();
    }
}
