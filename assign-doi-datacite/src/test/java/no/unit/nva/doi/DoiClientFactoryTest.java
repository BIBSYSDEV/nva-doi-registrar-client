package no.unit.nva.doi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import org.junit.jupiter.api.Test;

class DoiClientFactoryTest {

    @Test
    void getClientWithDataciteThenReturnDoiClient() {
        var dataciteConfigurationFactory = mock(DataCiteConfigurationFactory.class);
        var dataciteMdsConnectionFactory = mock(DataCiteConnectionFactory.class);
        var actual = DoiClientFactory.getClient(dataciteConfigurationFactory, dataciteMdsConnectionFactory);
        assertThat(actual, is(instanceOf(DataCiteClient.class)));
        assertThat(actual, isA(DoiClient.class));
    }
}