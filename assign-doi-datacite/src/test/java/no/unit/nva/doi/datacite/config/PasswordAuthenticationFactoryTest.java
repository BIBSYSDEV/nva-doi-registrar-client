package no.unit.nva.doi.datacite.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.UUID;
import no.unit.nva.doi.datacite.mdsclient.NoCredentialsForCustomerRuntimeException;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordAuthenticationFactoryTest {

    public static final String INTERNAL_CONFIGURATION_SETUP_ERROR = "Failed configuring up valid Datacite MDS config";
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
        DataciteConfigurationFactory configurationFactory = getConfigurationFactoryMock();
        sut = new PasswordAuthenticationFactory(configurationFactory);
    }

    @Test
    void getCredentialsForKnownCustomerReturnUsernameAndPassword() {
        var credentials = sut.getCredentials(KNOWN_CUSTOMER_ID);
        assertThat(credentials.getUserName(), is(equalTo(EXAMPLE_MDS_USERNAME)));
        assertThat(credentials.getPassword(), is(equalTo(EXAMPLE_MDS_PASSWORD.toCharArray())));
    }

    @Test
    void getCredentialsForUnknownCustomerHasNoCredentials() {
        assertThrows(NoCredentialsForCustomerRuntimeException.class, () -> sut.getCredentials(UNKNOWN_CUSTOMER_ID));
    }

    private DataciteConfigurationFactory getConfigurationFactoryMock() {
        var configurationFactory = mock(DataciteConfigurationFactory.class);
        try {
            when(configurationFactory.getConfig(KNOWN_CUSTOMER_ID)).thenReturn(MOCK_DATACITE_CONFIG);
            when(configurationFactory.getCredentials(KNOWN_CUSTOMER_ID)).thenReturn(MOCK_DATACITE_CONFIG);
            when(configurationFactory.getConfig(UNKNOWN_CUSTOMER_ID))
                .thenThrow(DataCiteMdsConfigValidationFailedException.class);
            when(configurationFactory.getCredentials(UNKNOWN_CUSTOMER_ID))
                .thenThrow(NoCredentialsForCustomerRuntimeException.class);
        } catch (DataCiteMdsConfigValidationFailedException e) {
            Assertions.fail(INTERNAL_CONFIGURATION_SETUP_ERROR);
        }

        return configurationFactory;
    }
}