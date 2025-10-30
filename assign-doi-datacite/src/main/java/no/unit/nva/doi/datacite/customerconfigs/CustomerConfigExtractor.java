package no.unit.nva.doi.datacite.customerconfigs;

import no.unit.nva.doi.models.Doi;

import java.net.URI;

public interface CustomerConfigExtractor {

    CustomerConfig getCustomerConfig(Doi doi) throws CustomerConfigException;

    CustomerConfig getCustomerConfig(URI customerId) throws CustomerConfigException;

}
