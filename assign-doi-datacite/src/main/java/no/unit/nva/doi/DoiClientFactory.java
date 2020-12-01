package no.unit.nva.doi;

import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;

/**
 * Factory for obtaining a {@link DoiClient}.
 *
 * <p>Currently only supporting DataCite.
 *
 * @see DoiClient
 */
public final class DoiClientFactory {

    private DoiClientFactory() {
    }

    public static DoiClient getClient(DataCiteConfigurationFactory configFactory,
                                      DataCiteConnectionFactory mdsConnectionFactory) {
        return new DataCiteClient(configFactory, mdsConnectionFactory);
    }
}
