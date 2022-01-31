package no.unit.nva.doi.datacite.mdsclient;

import static no.unit.nva.doi.DataciteConfig.DATACITE_MDS_URI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteMdsConfigValidationFailedException;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataCiteConnectionFactoryTest {

    private static final String DEMO_PREFIX = "10.5072";
    private static final URI KNOWN_CUSTOMER_ID = URI.create("https://example.net/customer/id/1234");

    private static final String EXAMPLE_CUSTOMER_DOI_PREFIX = DEMO_PREFIX;
    private static final URI EXAMPLE_CUSTOMER_ID = KNOWN_CUSTOMER_ID;
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserNameForRepository";
    private static final String EXAMPLE_MDS_PASSWORD = UUID.randomUUID().toString();

    private static final DataCiteMdsClientConfig MOCK_DATACITE_CONFIG = new DataCiteMdsClientSecretConfig(
        EXAMPLE_CUSTOMER_ID, EXAMPLE_CUSTOMER_DOI_PREFIX, EXAMPLE_MDS_USERNAME,
        EXAMPLE_MDS_PASSWORD);
    private static final URI UNKNOWN_CUSTOMER_ID = URI.create("https://example.net/customer/id/41515-unknown-customer");

    private DataCiteConfigurationFactory configurationFactory;
    private DataCiteConnectionFactory sut;

    @BeforeEach
    void configure() throws DataCiteMdsConfigValidationFailedException {

        configurationFactory = mock(DataCiteConfigurationFactory.class);
        when(configurationFactory.getConfig(KNOWN_CUSTOMER_ID)).thenReturn(MOCK_DATACITE_CONFIG);

        sut = new DataCiteConnectionFactory(configurationFactory);
    }

    @Test
    void getAuthenticatedConnectionIsInstanceOfDataciteMdsConnectionForKnownCustomer() {
        assertThat(sut.getAuthenticatedMdsConnection(KNOWN_CUSTOMER_ID), is(instanceOf((DataCiteMdsConnection.class))));
    }

    @Test
    void getAuthenticatedConnectionHasAuthenticatorButThrowsExceptionForUnknownCustomerWhenAskedForCredentials() {

        var authenticator = sut.getAuthenticatedMdsConnection(UNKNOWN_CUSTOMER_ID)
            .getHttpClient()
            .authenticator();
        assertThat(authenticator.isPresent(), is(true));
        assertThrows(NoCredentialsForCustomerRuntimeException.class,
                     () -> prompAuthenticatorForCredentials(authenticator.get()));
    }

    @Test
    void getAuthenticatedConnectionAttachesPasswordAuthenticationOnHttpClient() {
        Optional<Authenticator> authenticator = sut.getAuthenticatedMdsConnection(KNOWN_CUSTOMER_ID)
            .getHttpClient()
            .authenticator();
        assertThat(authenticator.isPresent(), is(true));
    }

    private PasswordAuthentication prompAuthenticatorForCredentials(Authenticator authenticator)
        throws UnknownHostException, MalformedURLException {

        var uriInRequest = new UriWrapper(DATACITE_MDS_URI).addChild("dummypath").getUri();

        return authenticator
            .requestPasswordAuthenticationInstance(DATACITE_MDS_URI.getHost(), InetAddress.getLocalHost(),
                                                   DATACITE_MDS_URI.getPort(),
                                                   null,
                                                   "Please authenticate", "authenticationScheme",
                                                   uriInRequest.toURL(),
                                                   RequestorType.SERVER);
    }
}