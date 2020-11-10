package no.unit.nva.datacite.config;

import java.net.PasswordAuthentication;
import java.util.Optional;

public class PasswordAuthenticationFactory {

    private final DataciteConfigurationFactory dataciteConfigurationFactory;

    public PasswordAuthenticationFactory(DataciteConfigurationFactory dataciteConfigurationFactory) {
        this.dataciteConfigurationFactory = dataciteConfigurationFactory;
    }

    public Optional<PasswordAuthentication> getCredentials(String customerId) {
        return dataciteConfigurationFactory.getCredentials(customerId)
            .map(secretConfig -> new PasswordAuthentication(
                secretConfig.getDataCiteMdsClientUsername(),
                secretConfig.getDataCiteMdsClientPassword().toCharArray()));
    }
}
