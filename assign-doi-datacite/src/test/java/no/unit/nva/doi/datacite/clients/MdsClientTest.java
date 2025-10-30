package no.unit.nva.doi.datacite.clients;

import static no.unit.nva.doi.datacite.clients.TestDataFactory.DOI_PREFIX;
import static no.unit.nva.doi.datacite.clients.TestDataFactory.createValidCustomer;
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
import java.net.http.HttpClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.utils.FakeCustomerExtractor;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.stubs.WiremockHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
public class MdsClientTest {

    private static final String DOI_HOST = "doi.org";
    private static final String DOI_SUFFIX = "2";

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
        createValidCustomer(customerConfigExtractor);
        var doi = Doi.fromPrefixAndSuffix(DOI_HOST, DOI_PREFIX, DOI_SUFFIX);

        client = new MdsClient(runtimeInfo.getHttpBaseUrl(), customerConfigExtractor, httpClientMock);

        var exception = assertThrows(ClientException.class, () -> client.deleteDraftDoi(doi));
        var expectedMessage = String.format("Request http://localhost:%d/doi/%s/%s DELETE failed.",
                                            runtimeInfo.getHttpPort(),
                                            DOI_PREFIX,
                                            DOI_SUFFIX);

        assertThat(exception.getMessage(), is(equalTo(expectedMessage)));
        assertThat(exception.getCause().getMessage(), containsString(exceptionMessage));
    }

}