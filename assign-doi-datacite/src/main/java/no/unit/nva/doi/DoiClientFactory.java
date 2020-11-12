package no.unit.nva.doi;

import no.unit.nva.doi.datacite.clients.DataciteClient;
import no.unit.nva.doi.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataciteMdsConnectionFactory;

/**
 * Factory for obtaining a {@link DoiClient}.
 *
 * <p>Currently only supporting Datacite.
 *
 * @see DoiClient
 */
public final class DoiClientFactory {

    private DoiClientFactory() {
    }

    public static DoiClient getClient(DataciteConfigurationFactory configFactory,
                                      DataciteMdsConnectionFactory mdsConnectionFactory) {
        return new DataciteClient(configFactory, mdsConnectionFactory);
    }
}
