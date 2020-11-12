package no.unit.nva.datacite.config;

import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.util.Optional;
import no.unit.nva.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;

/**
 * Password authentication factory for providing {@link java.net.Authenticator} for NVA customers.
 *
 * @see no.unit.nva.datacite.mdsclient.DataciteMdsConnectionFactory
 * @see HttpClient#authenticator()
 */
public class PasswordAuthenticationFactory {

    private final DataciteConfigurationFactory dataciteConfigurationFactory;

    public PasswordAuthenticationFactory(DataciteConfigurationFactory dataciteConfigurationFactory) {
        this.dataciteConfigurationFactory = dataciteConfigurationFactory;
    }

    public PasswordAuthentication getCredentials(String customerId) {
        return Optional.ofNullable(dataciteConfigurationFactory.getCredentials(customerId))
            .map(secretConfig -> new PasswordAuthentication(
                secretConfig.getDataCiteMdsClientUsername(),
                secretConfig.getDataCiteMdsClientPassword().toCharArray()))
            .orElseThrow(NoCredentialsForCustomerRuntimeException::new);
    }
}
