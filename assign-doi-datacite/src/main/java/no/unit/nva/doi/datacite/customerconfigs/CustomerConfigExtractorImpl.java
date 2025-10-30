package no.unit.nva.doi.datacite.customerconfigs;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.doi.models.Doi;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

public class CustomerConfigExtractorImpl implements CustomerConfigExtractor {

    private final SecretsReader secretsReader;
    private final String secretName;
    private final String secretKey;

    private final Map<String, CustomerConfig> customerConfigs;

    @JacocoGenerated
    public CustomerConfigExtractorImpl(String secretName,
                                       String secretKey) {
        this(new SecretsReader(), secretName, secretKey);
    }

    public CustomerConfigExtractorImpl(SecretsReader secretsReader,
                                       String secretName,
                                       String secretKey) {
        this.secretsReader = secretsReader;
        this.secretName = secretName;
        this.secretKey = secretKey;
        this.customerConfigs = new HashMap<>();
    }

    @Override
    public CustomerConfig getCustomerConfig(final Doi doi)
        throws CustomerConfigException {
        var doiPrefix = doi.getPrefix();
        readCustomerConfigFromSecretsReaderIfCustomerConfigsIsEmpty();
        return Optional.ofNullable(customerConfigs.get(doiPrefix))
                   .orElseThrow(CustomerConfigException::new);
    }

    @Override
    public CustomerConfig getCustomerConfig(URI customerId) throws CustomerConfigException {
        readCustomerConfigFromSecretsReaderIfCustomerConfigsIsEmpty();
        return Optional.ofNullable(customerConfigs.get(customerId.toString()))
                .orElseThrow(CustomerConfigException::new);
    }

    private void readCustomerConfigFromSecretsReaderIfCustomerConfigsIsEmpty() throws CustomerConfigException {
        if (customerConfigs.isEmpty()) {
            readCustomerConfigFromSecretsReader();
        }
    }

    private void readCustomerConfigFromSecretsReader() throws CustomerConfigException {
        var customerConfigs = extractCustomerConfigsFromSecretsReader();
        createCustomersMap(customerConfigs);
    }

    private CustomerConfig[] extractCustomerConfigsFromSecretsReader()
        throws CustomerConfigException {
        return attempt(() -> secretsReader.fetchSecret(secretName, secretKey))
                   .map(this::convertCustomerConfigStringToDto)
                   .orElseThrow(fail -> new CustomerConfigException(fail.getException()));
    }

    private CustomerConfig[] convertCustomerConfigStringToDto(String customerConfigsString)
        throws CustomerConfigException {
        return attempt(() ->
                           JsonUtils.dtoObjectMapper.readValue(customerConfigsString,
                                                               CustomerConfig[].class))
                   .orElseThrow(fail -> new CustomerConfigException(fail.getException()));
    }

    private void createCustomersMap(final CustomerConfig... customers) {
        Arrays.stream(customers)
            .filter(customer -> nonNull(customer.getDoiPrefix()))
            .forEach(customer -> {
                customerConfigs.put(customer.getCustomerId().toString(), customer);
                customerConfigs.put(customer.getDoiPrefix(), customer);
            });
    }
}
