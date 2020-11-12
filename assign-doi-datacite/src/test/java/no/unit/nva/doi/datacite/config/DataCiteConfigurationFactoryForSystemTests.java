package no.unit.nva.doi.datacite.config;

import java.util.Map;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;

/**
 * Datacite Configuration Factory which easily populates secret config and config for system tests.
 *
 * <p>This is to be used just like you would use {@link DataCiteConfigurationFactory}.
 */
public class DataCiteConfigurationFactoryForSystemTests extends DataCiteConfigurationFactory {

    public DataCiteConfigurationFactoryForSystemTests(Map<String, DataCiteMdsClientSecretConfig> secretConfigs) {
        super(secretConfigs);
    }
}
