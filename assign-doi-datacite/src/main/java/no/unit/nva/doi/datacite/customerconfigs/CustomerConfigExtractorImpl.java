package no.unit.nva.doi.datacite.customerconfigs;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;
import nva.commons.secrets.SecretsReader;

public class CustomerConfigExtractorImpl implements CustomerConfigExtractor {

    private final SecretsReader secretsReader;
    private final String secretName;
    private final String secretKey;

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
    }

    @Override
    public CustomerConfig getCustomerConfig(final URI customerId)
        throws CustomerConfigException {
        var customerConfigs = readCustomerConfigFromSecretsReader(secretsReader,
                                                                  secretName,
                                                                  secretKey);
        return Optional.ofNullable(customerConfigs.get(customerId))
                   .orElseThrow(CustomerConfigException::new);
    }

    private static Map<URI, CustomerConfig> readCustomerConfigFromSecretsReader(
        SecretsReader secretsReader,
        String secretName,
        String secretKey) throws CustomerConfigException {
        var customerConfigs = extractCustomerConfigsFromSecretsReader(secretsReader, secretName, secretKey);
        return createCustomersMap(customerConfigs);
    }

    private static CustomerConfig[] extractCustomerConfigsFromSecretsReader(SecretsReader secretsReader,
                                                                            String secretName,
                                                                            String secretKey)
        throws CustomerConfigException {
        return attempt(() -> secretsReader.fetchSecret(secretName, secretKey))
                   .map(CustomerConfigExtractorImpl::convertCustomerConfigStringToDto)
                   .orElseThrow(fail -> new CustomerConfigException(fail.getException()));
    }

    private static CustomerConfig[] convertCustomerConfigStringToDto(String customerConfigsString)
        throws CustomerConfigException {
        return attempt(() ->
                           JsonUtils.dtoObjectMapper.readValue(customerConfigsString,
                                                               CustomerConfig[].class))
                   .orElseThrow(fail -> new CustomerConfigException(fail.getException()));
    }

    private static Map<URI, CustomerConfig> createCustomersMap(final CustomerConfig... customers) {
        var customerConfigs = new HashMap<URI, CustomerConfig>();
        Arrays.stream(customers)
            .filter(customer -> nonNull(customer.getCustomerId()))
            .forEach(customer -> customerConfigs.put(customer.getCustomerId(), customer));
        return customerConfigs;
    }
}
