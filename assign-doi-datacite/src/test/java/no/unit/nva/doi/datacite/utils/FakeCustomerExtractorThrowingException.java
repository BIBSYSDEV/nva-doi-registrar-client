package no.unit.nva.doi.datacite.utils;

import java.net.URI;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigExtractor;
import no.unit.nva.doi.models.Doi;

public class FakeCustomerExtractorThrowingException implements CustomerConfigExtractor {

    @Override
    public CustomerConfig getCustomerConfig(Doi doi) throws CustomerConfigException {
        throw new CustomerConfigException();
    }

    @Override
    public CustomerConfig getCustomerConfig(URI customerId) throws CustomerConfigException {
        throw new CustomerConfigException();
    }
}
