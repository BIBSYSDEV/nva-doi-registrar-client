package no.unit.nva.doi.datacite.clients;

import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.net.URI;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.utils.FakeCustomerExtractor;

public final class TestDataFactory {
    static final String CUSTOMER_PASSWORD = "password";
    static final String CUSTOMER_USERNAME = "username";
    static final String DOI_PREFIX = "1";

    private TestDataFactory() {
    }

    static URI createValidCustomer(FakeCustomerExtractor customerConfigExtractor) {
        var customerId = randomUri();
        var customerConfig = new CustomerConfig(customerId,
                                                CUSTOMER_PASSWORD,
                                                CUSTOMER_USERNAME,
                                                DOI_PREFIX);
        customerConfigExtractor.setCustomerConfig(customerConfig);
        return customerId;
    }

}
