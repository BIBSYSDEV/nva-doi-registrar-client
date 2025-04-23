package no.unit.nva.doi.datacite.clients;

import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.utils.FakeCustomerExtractor;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.stubs.WiremockHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
public class MdsClientTest {

    public static final String DOI_HOST = "doi.org";
    public static final String DOI_PREFIX = "1";
    public static final String DOI_SUFFIX = "2";
    public static final String CUSTOMER_PASSWORD = "password";
    public static final String CUSTOMER_USERNAME = "username";
    public static final String CUSTOMER_DOI_PREFIX = "doiPrefix";
    private MdsClient client;
    private FakeCustomerExtractor customerConfigExtractor;

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {
        this.customerConfigExtractor = new FakeCustomerExtractor();
        this.client = new MdsClient(runtimeInfo.getHttpBaseUrl(), customerConfigExtractor, WiremockHttpClient.create());
    }

    @Test
    void shouldThrowClientExceptionWhenHttpClientThrowsIoExceptionOnDeleteDraftDoi(WireMockRuntimeInfo runtimeInfo)
        throws IOException,
               InterruptedException {
        var httpClientMock = mock(HttpClient.class);
        var exceptionMessage = "Something horrible happened";
        when(httpClientMock.send(any(), any())).thenThrow(new IOException(exceptionMessage));
        var customerId = createValidCustomer();
        var doi = Doi.fromPrefixAndSuffix(DOI_HOST, DOI_PREFIX, DOI_SUFFIX);

        client = new MdsClient(runtimeInfo.getHttpBaseUrl(), customerConfigExtractor, httpClientMock);

        var exception = assertThrows(ClientException.class, () -> client.deleteDraftDoi(customerId, doi));
        var expectedMessage = String.format("Request http://localhost:%d/doi/%s/%s DELETE failed.",
                                            runtimeInfo.getHttpPort(),
                                            DOI_PREFIX,
                                            DOI_SUFFIX);

        assertThat(exception.getMessage(), is(equalTo(expectedMessage)));
        assertThat(exception.getCause().getMessage(), containsString(exceptionMessage));
    }

    private URI createValidCustomer() {
        var customerId = randomUri();
        var customerConfig = new CustomerConfig(customerId,
                                                CUSTOMER_PASSWORD,
                                                CUSTOMER_USERNAME,
                                                DOI_PREFIX);
        customerConfigExtractor.setCustomerConfig(customerConfig);
        return customerId;
    }
}