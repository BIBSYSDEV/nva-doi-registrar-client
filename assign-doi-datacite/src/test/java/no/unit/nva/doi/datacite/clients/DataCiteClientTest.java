package no.unit.nva.doi.datacite.clients;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactoryForSystemTests;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConnectionFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnection;
import no.unit.nva.doi.datacite.models.DataCiteMdsClientSecretConfig;
import no.unit.nva.doi.models.Doi;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataCiteClientTest extends DataciteClientTestBase {

    public static final String ERROR_DOICLIENT_METHOD_DELETE_DRAFT_DOI = "deleteDraftDoi";

    private static final URI EXAMPLE_CUSTOMER_ID = URI.create("https://example.net/customer/id/4512");
    private static final String DEMO_PREFIX = "10.5072";
    private static final String EXAMPLE_CUSTOMER_DOI_PREFIX = DEMO_PREFIX;
    private static final String EXAMPLE_MDS_USERNAME = "exampleUserName";
    private static final String EXAMPLE_MDS_PASSWORD = "examplePassword";

    private DataCiteMdsClientSecretConfig validSecretConfig;
    private DataCiteConfigurationFactory configurationFactory;
    private DataCiteClient sut;
    private DataCiteConnectionFactory mdsConnectionFactory;

    private DataCiteMdsConnection mdsConnectionThrowingIoException;

    @BeforeEach
    void setUp() throws InterruptedException, IOException, URISyntaxException {
        configurationFactory = createDataConfigurationFactoryForTest();

        mdsConnectionFactory = mock(DataCiteConnectionFactory.class);
        mdsConnectionThrowingIoException = mock(DataCiteMdsConnection.class);
        when(mdsConnectionFactory.getAuthenticatedMdsConnection())
            .thenReturn(mdsConnectionThrowingIoException);

        when(mdsConnectionThrowingIoException.deleteDoi(anyString())).thenThrow(IOException.class);

        sut = new DataCiteClient(configurationFactory, mdsConnectionFactory);
    }

    @Test
    void deleteDraftDoiForCustomerWhereTransportExceptionHappensThenThrowsClientException() {
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();

        var actualException = assertThrows(ClientException.class, () -> sut.deleteDraftDoi(EXAMPLE_CUSTOMER_ID, doi));
        assertThat(actualException.getMessage(), containsString(ERROR_DOICLIENT_METHOD_DELETE_DRAFT_DOI));
    }

    @Test
    void transExceptionWhichThrowsClientExceptionIsLogged() {
        var appender = LogUtils.getTestingAppender(DataCiteClient.class);
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();

        assertThrows(ClientException.class, () -> sut.deleteDraftDoi(EXAMPLE_CUSTOMER_ID, doi));
        assertThat(appender.getMessages(), containsString(ERROR_DOICLIENT_METHOD_DELETE_DRAFT_DOI));
    }

    private DataCiteConfigurationFactoryForSystemTests createDataConfigurationFactoryForTest() {
        validSecretConfig = new DataCiteMdsClientSecretConfig(EXAMPLE_CUSTOMER_ID,
            EXAMPLE_CUSTOMER_DOI_PREFIX, EXAMPLE_MDS_USERNAME, EXAMPLE_MDS_PASSWORD);
        return new DataCiteConfigurationFactoryForSystemTests(
            Map.of(EXAMPLE_CUSTOMER_ID, validSecretConfig));
    }
}