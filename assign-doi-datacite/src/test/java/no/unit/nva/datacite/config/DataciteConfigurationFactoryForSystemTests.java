package no.unit.nva.datacite.config;

import java.util.Map;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;

/**
 * Datacite Configuration Factory which easily populates secret config and config for system tests.
 *
 * <p>This is to be used just like you would use {@link DataciteConfigurationFactory}.
 */
public class DataciteConfigurationFactoryForSystemTests extends DataciteConfigurationFactory {

    public DataciteConfigurationFactoryForSystemTests(Map<String, DataCiteMdsClientSecretConfig> secretConfigs) {
        super(secretConfigs);
    }
}
