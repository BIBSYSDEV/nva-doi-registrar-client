package no.unit.nva.datacite.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import no.unit.nva.datacite.commons.DataCiteMetadataResolver;
import no.unit.nva.datacite.commons.PublicationApiClientException;
import no.unit.nva.datacite.commons.TestBase;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpsEnabled = true)
class RegisteredDoiEventHandlerTest extends TestBase {
    private final DoiClient doiClient = mock(DoiClient.class);
    private ByteArrayOutputStream outputStream;
    private RegisteredDoiEventHandler registeredDoiHandler;
    private Context context;

    private static final String DATACITE_XML_BODY = IoUtils.stringFromResources(Path.of("datacite.xml"));
    private static final URI CUSTOMER_ID_IN_INPUT_EVENT =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934")
            .getUri();

    private static final URI VALID_SAMPLE_DOI = UriWrapper.fromUri("https://doi.org/10.1000/182").getUri();

    @BeforeEach
    public void init(WireMockRuntimeInfo wireMockRuntimeInfo) {
        setBaseUrl(wireMockRuntimeInfo.getHttpBaseUrl());
        var httpClient = WiremockHttpClient.create();
        registeredDoiHandler = new RegisteredDoiEventHandler(doiClient, new DataCiteMetadataResolver(httpClient));
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    void shouldDeleteDoiMetadataIfGone()
        throws ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT)) {
            mockDataciteXmlGone(publicationIdentifier);
            registeredDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient).deleteMetadata(
                CUSTOMER_ID_IN_INPUT_EVENT,
                Doi.fromUri(VALID_SAMPLE_DOI));
        }
    }

    @Test
    void shouldThrowIfUnknownError() throws IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT)) {
            mockDataciteXmlError(publicationIdentifier);

            assertThrows(PublicationApiClientException.class, () -> {
                registeredDoiHandler.handleRequest(inputStream, outputStream, context);
            });
        }
    }

    @Test
    void shouldNotDeleteDoiMetadataIf200OK()
        throws ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT)) {
            mockDataciteXmlBody(publicationIdentifier, DATACITE_XML_BODY);
            registeredDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient, never()).deleteMetadata(
                CUSTOMER_ID_IN_INPUT_EVENT,
                Doi.fromUri(VALID_SAMPLE_DOI));
        }
    }
}
