package no.unit.nva.doi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import no.unit.nva.doi.datacite.clients.DataciteClient;
import no.unit.nva.doi.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataciteMdsConnectionFactory;
import org.junit.jupiter.api.Test;

class DoiClientFactoryTest {

    @Test
    void getClientWithDataciteThenReturnDoiClient() {
        var dataciteConfigurationFactory = mock(DataciteConfigurationFactory.class);
        var dataciteMdsConnectionFactory = mock(DataciteMdsConnectionFactory.class);
        var actual = DoiClientFactory.getClient(dataciteConfigurationFactory, dataciteMdsConnectionFactory);
        assertThat(actual, is(instanceOf(DataciteClient.class)));
        assertThat(actual, isA(DoiClient.class));
    }
}