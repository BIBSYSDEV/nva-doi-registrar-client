package no.unit.nva.datacite.config;

import java.net.PasswordAuthentication;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;

public class PasswordAuthenticationFactory {
    private final DataciteConfigurationFactory dataciteConfigurationFactory;

    public PasswordAuthenticationFactory(DataciteConfigurationFactory dataciteConfigurationFactory) {
        this.dataciteConfigurationFactory = dataciteConfigurationFactory;
    }

    public PasswordAuthentication getCredentials(String customerId) {
        DataCiteMdsClientSecretConfig dataCiteMdsClientSecretConfig = dataciteConfigurationFactory.getCredentials(customerId);
        return new PasswordAuthentication(dataCiteMdsClientSecretConfig.getDataCiteMdsClientUsername(),
            dataCiteMdsClientSecretConfig.getDataCiteMdsClientPassword().toCharArray());
    }


}
