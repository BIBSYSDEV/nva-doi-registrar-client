package no.unit.nva.doi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnectionFactory;
import org.junit.jupiter.api.Test;

class DoiClientFactoryTest {

    @Test
    void getClientWithDataciteThenReturnDoiClient() {
        var dataciteConfigurationFactory = mock(DataCiteConfigurationFactory.class);
        var dataciteMdsConnectionFactory = mock(DataCiteMdsConnectionFactory.class);
        var actual = DoiClientFactory.getClient(dataciteConfigurationFactory, dataciteMdsConnectionFactory);
        assertThat(actual, is(instanceOf(DataCiteClient.class)));
        assertThat(actual, isA(DoiClient.class));
    }
}