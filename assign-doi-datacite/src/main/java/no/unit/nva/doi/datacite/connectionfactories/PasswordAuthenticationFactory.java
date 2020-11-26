package no.unit.nva.doi.datacite.connectionfactories;

import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Optional;
import no.unit.nva.doi.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;

/**
 * Password authentication factory for providing {@link java.net.Authenticator} for NVA customers.
 *
 * @see DataCiteConnectionFactory
 * @see HttpClient#authenticator()
 */
public class PasswordAuthenticationFactory {

    private final DataCiteConfigurationFactory dataciteConfigurationFactory;

    public PasswordAuthenticationFactory(DataCiteConfigurationFactory dataciteConfigurationFactory) {
        this.dataciteConfigurationFactory = dataciteConfigurationFactory;
    }

    public PasswordAuthentication getCredentials(URI customerId) {
        return Optional.ofNullable(dataciteConfigurationFactory.getCredentials(customerId))
            .map(secretConfig -> new PasswordAuthentication(
                secretConfig.getDataCiteMdsClientUsername(),
                secretConfig.getDataCiteMdsClientPassword().toCharArray()))
            .orElseThrow(NoCredentialsForCustomerRuntimeException::new);
    }
}
