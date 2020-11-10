package no.unit.nva.datacite.config;

import java.util.Map;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;

/**
 * Only use for {@link no.unit.nva.datacite.clients.DataciteClientTest}
 */
public class DataciteConfigurationFactoryForDataciteClientTest extends DataciteConfigurationFactory {
    public DataciteConfigurationFactoryForDataciteClientTest(Map<String, DataCiteMdsClientSecretConfig> secretConfigs) {
        super(secretConfigs);
    }
}
