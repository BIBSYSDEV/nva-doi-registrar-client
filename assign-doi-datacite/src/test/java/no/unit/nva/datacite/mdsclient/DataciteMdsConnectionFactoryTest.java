package no.unit.nva.datacite.mdsclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.secretsmanager.caching.SecretCache;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import no.unit.nva.datacite.config.DataciteConfigurationFactory;
import no.unit.nva.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataciteMdsConnectionFactoryTest {

    private static final String KNOWN_CUSTOMER_ID = "https://example.net/customer/id/1234";
    private static final String EXAMPLE_HOST = "https://example.net/datacite/mds/api";

    private static final String DEMO_PREFIX = "10.5072";
    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_INSTITUTION = KNOWN_CUSTOMER_ID;
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserNameForRepository";
    private static final String EXAMPLE_MDS_PASSWORD = UUID.randomUUID().toString();
    private static final DataCiteMdsClientConfig MOCK_DATACITE_CONFIG = new DataCiteMdsClientSecretConfig(
        EXAMPLE_INSTITUTION, EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_HOST, EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);
    private static final RequestorType IGNORED_REQUESTOR_TYPE = RequestorType.SERVER;
    private static final String IGNORED_URL = "https://example.net/ignored/in/test";
    private static final int EXAMPLE_PORT = 8888;
    private SecretCache secretCache;
    private DataciteMdsConnectionFactory sut;
    private DataciteConfigurationFactory configurationFactory;

    @BeforeEach
    void configure() {

        configurationFactory = mock(DataciteConfigurationFactory.class);
        when(configurationFactory.getConfig(KNOWN_CUSTOMER_ID)).thenReturn(Optional.of(MOCK_DATACITE_CONFIG));
        sut = new DataciteMdsConnectionFactory(
            new PasswordAuthenticationFactory(configurationFactory), EXAMPLE_HOST, EXAMPLE_PORT);
    }

    @Test
    void getAuthenticatedConnectionIsInstanceOfDataciteMdsConnection() {
        assertThat(sut.getAuthenticatedConnection(KNOWN_CUSTOMER_ID), is(instanceOf((DataCiteMdsConnection.class))));
    }

    @Test
    void getAuthenticatedConnectionAttachesPasswordAuthenticationOnHttpClient() throws MalformedURLException {
        Optional<Authenticator> authenticator = sut.getAuthenticatedConnection(KNOWN_CUSTOMER_ID)
            .getHttpClient()
            .authenticator();
        assertThat(authenticator.isPresent(), is(true));
    }
}