package no.unit.nva.datacite.handlers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static no.unit.nva.datacite.handlers.UpdateDoiEventHandler.MANDATORY_FIELD_ERROR_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import jakarta.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import no.unit.nva.datacite.commons.DataCiteMetadataResolver;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.datacite.commons.PublicationApiClientException;
import no.unit.nva.datacite.commons.TestBase;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.datacite.restclient.models.State;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import nva.commons.logutils.TestAppender;
import org.datacide.schema.kernel_4.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpsEnabled = true)
public class UpdateDoiEventHandlerTest extends TestBase {

    public static final String SUCCESSFULLY_HANDLED_REQUEST_FOR_DOI = "Successfully handled request for Doi";

    public static final String PUBLICATION_ID_CUSTOMER_ID = "publicationID, customerID";

    private static final URI CUSTOMER_ID_IN_INPUT_EVENT =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934")
            .getUri();

    private static final URI VALID_SAMPLE_DOI = UriWrapper.fromUri("https://doi.org/10.1000/182").getUri();
    private static final String DATACITE_XML_BODY = IoUtils.stringFromResources(Path.of("datacite.xml"));
    private static final String DATACITE_XML_WITH_DUPLICATE_BODY =
        IoUtils.stringFromResources(Path.of("datacite-with-duplicate.xml"));
    private final DoiClient doiClient = mock(DoiClient.class);
    private UpdateDoiEventHandler updateDoiHandler;
    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void init(WireMockRuntimeInfo wireMockRuntimeInfo) {
        setBaseUrl(wireMockRuntimeInfo.getHttpBaseUrl());
        var httpClient = WiremockHttpClient.create();
        updateDoiHandler = new UpdateDoiEventHandler(doiClient, new DataCiteMetadataResolver(httpClient));
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    void handleRequestThrowsIllegalArgumentExceptionOnMissingCustomerId() throws IOException {
        try (InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_customer_id_and_publication_id.json")) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                              () -> updateDoiHandler.handleRequest(inputStream,
                                                                                                   outputStream,
                                                                                                   context));

            assertThat(exception.getMessage(),
                       containsString(MANDATORY_FIELD_ERROR_PREFIX + PUBLICATION_ID_CUSTOMER_ID));
        }
    }

    @Test
    void shouldThrowPublicationApiClientExceptionWhenPublicationApiIsThrowingException() throws IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier)) {
            mockNotFoundResponse(publicationIdentifier);
            assertThrows(PublicationApiClientException.class,
                         () -> updateDoiHandler.handleRequest(inputStream,
                                                              outputStream,
                                                              context));
        }
    }

    @Test
    void shouldThrowRuntimeExceptionWhenDoiClientRespondsWithException() throws
                                                                         ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier)) {
            mockDataciteXmlBody(publicationIdentifier);
            doThrow(new ClientException()).when(doiClient).updateMetadata(any(), any(), any());
            assertThrows(ClientRuntimeException.class,
                         () -> updateDoiHandler.handleRequest(inputStream,
                                                              outputStream,
                                                              context));
        }
    }

    @Test
    void handleRequestReturnsDoiUpdateHolderOnSuccessWhenInputIsValid()
        throws ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier)) {
            mockDataciteXmlBody(publicationIdentifier);
            updateDoiHandler.handleRequest(inputStream, outputStream, context);

            URI expectedCustomerId = CUSTOMER_ID_IN_INPUT_EVENT;
            Doi expectedDoi = Doi.fromUri(VALID_SAMPLE_DOI);
            verify(doiClient).updateMetadata(
                eq(expectedCustomerId),
                eq(expectedDoi),
                eq(DATACITE_XML_BODY));
            verify(doiClient).setLandingPage(
                expectedCustomerId,
                expectedDoi,
                UriWrapper.fromUri(createPublicationId(publicationIdentifier)).getUri()
            );
        }
    }

    @Test
    void handleRequestSuccessfullyIsLogged() {
        TestAppender testingAppender = LogUtils.getTestingAppender(UpdateDoiEventHandler.class);
        var publicationIdentifier = SortableIdentifier.next().toString();
        var inputStream = createDoiRequestInputStream(publicationIdentifier);
        mockDataciteXmlBody(publicationIdentifier);
        updateDoiHandler.handleRequest(inputStream, outputStream, context);
        assertThat(testingAppender.getMessages(), containsString(SUCCESSFULLY_HANDLED_REQUEST_FOR_DOI));
    }

    @Test
    void whenDoiIsNotPresentInEventIllegalArgumentIsThrown() throws IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        var doiUpdateRequestNotContaininDoi = createDoiUpdateRequestNotContainingDoi(publicationIdentifier);
        var awsEventBridgeEvent = crateAwsEventBridgeEvent(doiUpdateRequestNotContaininDoi);
        try (var inputStream = toInputStream(awsEventBridgeEvent)) {
            assertThrows(IllegalArgumentException.class,
                         () -> updateDoiHandler.handleRequest(inputStream, outputStream, context));
        }
    }

    @Test
    void shouldDeleteDoiMetadataIfGone()
        throws ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        var doi = Doi.fromUri(VALID_SAMPLE_DOI);
        when(doiClient.getMetadata(any(), any())).thenReturn(DATACITE_XML_BODY);
        mockGetDoiResponse(State.FINDABLE);
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, null)) {
            mockDataciteXmlGone(publicationIdentifier);
            updateDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient).deleteMetadata(
                CUSTOMER_ID_IN_INPUT_EVENT,
                doi
            );
        }
    }

    @Test
    void shouldDeleteDoiMetadataIfGoneWithDuplicateUri()
        throws ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        var doi = Doi.fromUri(VALID_SAMPLE_DOI);
        var mainUri = UriWrapper.fromUri("https://example.no/publication/123").getUri();
        when(doiClient.getMetadata(any(), any())).thenReturn(DATACITE_XML_BODY);
        mockGetDoiResponse(State.FINDABLE);
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, mainUri)) {
            mockDataciteXmlPermanentlyMoved(publicationIdentifier, mainUri.toString());
            updateDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient).updateMetadata(
                eq(CUSTOMER_ID_IN_INPUT_EVENT),
                eq(doi),
                argThat(s -> comparableSerializedObject(s).equals(
                    comparableSerializedObject(DATACITE_XML_WITH_DUPLICATE_BODY)))
            );
        }
    }

    @Test
    void whenDeletingDoiMetadataDontDuplicateRelatedIds()
        throws ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        var doi = Doi.fromUri(VALID_SAMPLE_DOI);
        var mainUri = UriWrapper.fromUri("https://example.no/publication/123").getUri();
        mockGetDoiResponse(State.FINDABLE);
        when(doiClient.getMetadata(any(), any())).thenReturn(DATACITE_XML_WITH_DUPLICATE_BODY);

        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, mainUri)) {
            mockDataciteXmlPermanentlyMoved(publicationIdentifier, mainUri.toString());
            updateDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient).updateMetadata(
                eq(CUSTOMER_ID_IN_INPUT_EVENT),
                eq(doi),
                argThat(s -> comparableSerializedObject(s).equals(
                    comparableSerializedObject(DATACITE_XML_WITH_DUPLICATE_BODY)))
            );
        }
    }

    private void mockGetDoiResponse(State state) throws ClientException {
        when(doiClient.getDoi(any(), any())).thenReturn(new DoiStateDto(VALID_SAMPLE_DOI.toString(), state));
    }

    @Test
    void shouldThrowIfUnknownError() throws IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, null)) {
            mockDataciteXmlError(publicationIdentifier);

            assertThrows(PublicationApiClientException.class, () -> {
                updateDoiHandler.handleRequest(inputStream, outputStream, context);
            });
        }
    }

    @Test
    void shouldThrowBadGatewayWhenUnknownDoiState() throws IOException, ClientException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, null)) {
            mockGetDoiResponse(null);
            assertThrows(PublicationApiClientException.class, () -> {
                updateDoiHandler.handleRequest(inputStream, outputStream, context);
            });
        }
    }

    @Test
    void shouldThrowBadGatewayWhenCouldNotFetchDoi() throws IOException, ClientException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, null)) {
            when(doiClient.getDoi(any(), any())).thenThrow(new ClientException());
            assertThrows(PublicationApiClientException.class, () -> {
                updateDoiHandler.handleRequest(inputStream, outputStream, context);
            });
        }
    }

    @Test
    void shouldDoNothingWhenDoiToDeleteIsNotFindable() throws IOException, ClientException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        var doi = Doi.fromUri(VALID_SAMPLE_DOI);
        when(doiClient.getMetadata(any(), any())).thenReturn(DATACITE_XML_BODY);
        mockGetDoiResponse(State.REGISTERED);
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, null)) {
            mockDataciteXmlGone(publicationIdentifier);
            updateDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient, never()).deleteMetadata(
                CUSTOMER_ID_IN_INPUT_EVENT,
                doi
            );
        }
    }

    @Test
    void shouldNotDeleteDoiMetadataIf200OK()
        throws ClientException, IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, null)) {
            mockDataciteXmlBody(publicationIdentifier, DATACITE_XML_BODY);
            updateDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient, never()).deleteMetadata(
                CUSTOMER_ID_IN_INPUT_EVENT,
                Doi.fromUri(VALID_SAMPLE_DOI));
        }
    }

    @Test
    void shouldDeleteDraftDoiWhenPublicationIsGoneAndHasDraftDoi() throws IOException, ClientException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        var doi = Doi.fromUri(VALID_SAMPLE_DOI);
        mockGetDoiResponse(State.DRAFT);
        try (var inputStream = createDoiRequestInputStream(publicationIdentifier, VALID_SAMPLE_DOI,
                                                           CUSTOMER_ID_IN_INPUT_EVENT, null)) {
            mockDataciteXmlGone(publicationIdentifier);
            updateDoiHandler.handleRequest(inputStream, outputStream, context);

            verify(doiClient).deleteDraftDoi(CUSTOMER_ID_IN_INPUT_EVENT, doi);
        }
    }

    private void mockDataciteXmlBody(String publicationIdentifier) {
        stubFor(WireMock.get(urlPathEqualTo("/publication/" + publicationIdentifier))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.datacite.datacite+xml"))
                    .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_OK).withBody(DATACITE_XML_BODY)));
    }

    private InputStream createDoiRequestInputStream(String publicationIdentifier) {
        var doiUpdateRequestEvent = createDoiUpdateRequest(publicationIdentifier);
        var awsEventBridgeEvent = crateAwsEventBridgeEvent(doiUpdateRequestEvent);
        return toInputStream(awsEventBridgeEvent);
    }

    private DoiUpdateRequestEvent createDoiUpdateRequest(String publicationID) {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         VALID_SAMPLE_DOI,
                                         UriWrapper.fromUri(createPublicationId(publicationID)).getUri(),
                                         CUSTOMER_ID_IN_INPUT_EVENT,
                                         null);
    }

    private DoiUpdateRequestEvent createDoiUpdateRequestNotContainingDoi(String publicationID) {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         null,
                                         UriWrapper.fromUri(createPublicationId(publicationID)).getUri(),
                                         CUSTOMER_ID_IN_INPUT_EVENT,
                                         null);
    }

    private void mockNotFoundResponse(String publicationID) {
        stubFor(WireMock.get(urlPathEqualTo("/publication/" + publicationID))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.datacite.datacite+xml"))
                    .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));
    }

    private static String comparableSerializedObject(String s) {
        return toString(toResource(s));
    }

    private static Resource toResource(String s) {
        return JAXB.unmarshal(new StringReader(s), Resource.class);
    }

    private static String toString(Resource resource) {
        var sw = new StringWriter();
        JAXB.marshal(resource, sw);
        return sw.toString();
    }
}
