package no.unit.nva.doi.datacite.mdsclient;

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
import no.unit.nva.doi.datacite.config.DataCiteMdsConfigValidationFailedException;
import no.unit.nva.doi.datacite.config.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.config.PasswordAuthenticationFactory;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientConfig;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataCiteMdsConnectionFactoryTest {

    private static final String DEMO_PREFIX = "10.5072";
    private static final String KNOWN_CUSTOMER_ID = "https://example.net/customer/id/1234";

    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_INSTITUTION = KNOWN_CUSTOMER_ID;
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserNameForRepository";
    private static final String EXAMPLE_MDS_PASSWORD = UUID.randomUUID().toString();
    private static final String EXAMPLE_HOST = "example.net";
    private static final int EXAMPLE_PORT = 8888;

    private static final DataCiteMdsClientConfig MOCK_DATACITE_CONFIG = new DataCiteMdsClientSecretConfig(
        EXAMPLE_INSTITUTION, EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_HOST, EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);
    private static final String UNKNOWN_CUSTOMER_ID = "https://example.net/customer/id/41515-unknown-customer";

    private DataCiteConfigurationFactory configurationFactory;
    private DataCiteMdsConnectionFactory sut;

    @BeforeEach
    void configure() throws DataCiteMdsConfigValidationFailedException {

        configurationFactory = mock(DataCiteConfigurationFactory.class);
        when(configurationFactory.getConfig(KNOWN_CUSTOMER_ID)).thenReturn(MOCK_DATACITE_CONFIG);
        sut = new DataCiteMdsConnectionFactory(
            new PasswordAuthenticationFactory(configurationFactory), EXAMPLE_HOST, EXAMPLE_PORT);
    }

    @Test
    void getAuthenticatedConnectionIsInstanceOfDataciteMdsConnectionForKnownCustomer() {
        assertThat(sut.getAuthenticatedConnection(KNOWN_CUSTOMER_ID), is(instanceOf((DataCiteMdsConnection.class))));
    }

    @Test
    void getAuthenticatedConnectionHasAuthenticatorButThrowsExceptionForUnknownCustomerWhenAskedForCredentials() {

        var authenticator = sut.getAuthenticatedConnection(UNKNOWN_CUSTOMER_ID)
            .getHttpClient()
            .authenticator();
        assertThat(authenticator.isPresent(), is(true));
        assertThrows(NoCredentialsForCustomerRuntimeException.class,
            () -> prompAuthenticatorForCredentials(authenticator.get()));
    }

    @Test
    void getAuthenticatedConnectionAttachesPasswordAuthenticationOnHttpClient() {
        Optional<Authenticator> authenticator = sut.getAuthenticatedConnection(KNOWN_CUSTOMER_ID)
            .getHttpClient()
            .authenticator();
        assertThat(authenticator.isPresent(), is(true));
    }

    private PasswordAuthentication prompAuthenticatorForCredentials(Authenticator authenticator)
        throws UnknownHostException, MalformedURLException {
        return authenticator
            .requestPasswordAuthenticationInstance(EXAMPLE_HOST, InetAddress.getLocalHost(), EXAMPLE_PORT, null,
                "Please authenticate", "authenticationScheme",
                URI.create(String.format("https://%s:%s/dummypath", EXAMPLE_HOST, EXAMPLE_PORT)).toURL(),
                RequestorType.SERVER);
    }
}