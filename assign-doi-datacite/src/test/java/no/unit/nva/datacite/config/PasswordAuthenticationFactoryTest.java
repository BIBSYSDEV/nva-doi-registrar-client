package no.unit.nva.datacite.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;
import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordAuthenticationFactoryTest {

    private static final String DEMO_PREFIX = "10.5072";
    private static final String KNOWN_CUSTOMER_ID = "https://example.net/customer/id/1234";
    private static final String UNKNOWN_CUSTOMER_ID = "https://example.net/customer/id/92392323-is-unknown-customer";


    private static final String EXAMPLE_INSTITUTION_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_INSTITUTION = KNOWN_CUSTOMER_ID;

    private static final String EXAMPLE_MDS_USERNAME = "exampleUserNameForRepository";
    private static final String EXAMPLE_MDS_PASSWORD = UUID.randomUUID().toString();
    private static final String EXAMPLE_ENDPOINT = "https://example.net/datacite/mds/api";
    private static final DataCiteMdsClientSecretConfig MOCK_DATACITE_CONFIG = new DataCiteMdsClientSecretConfig(
        EXAMPLE_INSTITUTION, EXAMPLE_INSTITUTION_PREFIX, EXAMPLE_ENDPOINT, EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);

    private PasswordAuthenticationFactory sut;

    @BeforeEach
    void setUp() {
        var configurationFactory = mock(DataciteConfigurationFactory.class);
        when(configurationFactory.getConfig(KNOWN_CUSTOMER_ID)).thenReturn(Optional.of(MOCK_DATACITE_CONFIG));
        when(configurationFactory.getCredentials(KNOWN_CUSTOMER_ID)).thenReturn(Optional.of(MOCK_DATACITE_CONFIG));
        when(configurationFactory.getConfig(UNKNOWN_CUSTOMER_ID)).thenReturn(Optional.empty());
        when(configurationFactory.getCredentials(UNKNOWN_CUSTOMER_ID)).thenReturn(Optional.empty());
        sut = new PasswordAuthenticationFactory(configurationFactory);
    }

    @Test
    void getCredentialsForKnownCustomerReturnUsernameAndPassword() {
        var credentials = sut.getCredentials(KNOWN_CUSTOMER_ID);
        assertThat(credentials.isPresent(), is(true));
        var passwordAuthentication = credentials.get();
        assertThat(passwordAuthentication.getUserName(), is(equalTo(EXAMPLE_MDS_USERNAME)));
        assertThat(passwordAuthentication.getPassword(), is(equalTo(EXAMPLE_MDS_PASSWORD.toCharArray())));
    }

    @Test
    void getCredentialsForUnknownCustomerHasNoCredentials() {
        assertThat(sut.getCredentials(UNKNOWN_CUSTOMER_ID).isEmpty(), is(true));
    }
}