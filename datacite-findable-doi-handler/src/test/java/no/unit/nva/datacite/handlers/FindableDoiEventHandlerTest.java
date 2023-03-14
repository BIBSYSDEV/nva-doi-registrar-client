package no.unit.nva.datacite.handlers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static no.unit.nva.datacite.handlers.FindableDoiEventHandler.MANDATORY_FIELD_ERROR_PREFIX;
import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.datacite.commons.DoiUpdateEvent;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import nva.commons.logutils.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpsEnabled = true)
public class FindableDoiEventHandlerTest {

    public static final String SUCCESSFULLY_HANDLED_REQUEST_FOR_DOI = "Successfully handled request for Doi";

    public static final String PUBLICATION_ID_CUSTOMER_ID = "publicationID, customerID";

    private static final URI CUSTOMER_ID_IN_INPUT_EVENT =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934")
            .getUri();

    private static final URI VALID_SAMPLE_DOI = UriWrapper.fromUri("https://doi.org/10.1000/182").getUri();
    private static final String DATACITE_XML_BODY = IoUtils.stringFromResources(Path.of("datacite.xml"));
    private final DoiClient doiClient = mock(DoiClient.class);
    private FindableDoiEventHandler findableDoiHandler;
    private ByteArrayOutputStream outputStream;
    private Context context;

    private String baseUrl;

    @BeforeEach
    public void init(WireMockRuntimeInfo wireMockRuntimeInfo) {
        baseUrl = wireMockRuntimeInfo.getHttpBaseUrl();
        var httpClient = WiremockHttpClient.create();
        findableDoiHandler = new FindableDoiEventHandler(doiClient, new DataCiteMetadataResolver(httpClient));
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    void handleRequestThrowsIllegalArgumentExceptionOnMissingCustomerId() throws IOException {
        try (InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_customer_id_and_publication_id.json")) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                              () -> findableDoiHandler.handleRequest(inputStream,
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
                         () -> findableDoiHandler.handleRequest(inputStream,
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
                         () -> findableDoiHandler.handleRequest(inputStream,
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
            findableDoiHandler.handleRequest(inputStream, outputStream, context);
            DoiUpdateEvent response = parseResponse();
            assertThat(response.getItem().getPublicationIdentifier(), is(not(nullValue())));
            assertThat(response.getItem().getModifiedDate(), is(notNullValue()));

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
            var eventEmitted = readOutputStream(outputStream);
            assertThat(eventEmitted.getTopic(), is(equalTo("DoiRegistrarService.Doi.Updated")));
            assertThat(eventEmitted.getItem(), allOf(hasProperty("doi", equalTo(Optional.of(VALID_SAMPLE_DOI))),
                                                     hasProperty(
                                                         "publicationIdentifier",
                                                         is(equalTo(new SortableIdentifier(publicationIdentifier))))));
        }
    }

    @Test
    void handleRequestSuccessfullyIsLogged() {
        TestAppender testingAppender = LogUtils.getTestingAppender(FindableDoiEventHandler.class);
        var publicationIdentifier = SortableIdentifier.next().toString();
        var inputStream = createDoiRequestInputStream(publicationIdentifier);
        mockDataciteXmlBody(publicationIdentifier);
        findableDoiHandler.handleRequest(inputStream, outputStream, context);
        assertThat(testingAppender.getMessages(), containsString(SUCCESSFULLY_HANDLED_REQUEST_FOR_DOI));
    }

    @Test
    void whenDoiIsNotPresentInEventDraftDoiIsCreatedAndMadeFindableInOneStep() throws IOException, ClientException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        var doiUpdateRequestNotContaininDoi = createDoiUpdateRequestNotContainingDoi(publicationIdentifier);
        var awsEventBridgeEvent = crateAwsEventBridgeEvent(doiUpdateRequestNotContaininDoi);
        try (var inputStream = toInputStream(awsEventBridgeEvent)) {
            mockDataciteXmlBody(publicationIdentifier);
            mockCreateDoiResponse();
            findableDoiHandler.handleRequest(inputStream, outputStream, context);
            
            URI expectedCustomerId = CUSTOMER_ID_IN_INPUT_EVENT;
            Doi expectedDoi = Doi.fromUri(VALID_SAMPLE_DOI);
            verify(doiClient).createDoi(
                eq(expectedCustomerId));
            verify(doiClient).updateMetadata(
                eq(expectedCustomerId),
                eq(expectedDoi),
                eq(DATACITE_XML_BODY));
            verify(doiClient).setLandingPage(
                expectedCustomerId,
                expectedDoi,
                UriWrapper.fromUri(createPublicationId(publicationIdentifier)).getUri()
            );
            var eventEmitted = readOutputStream(outputStream);
            assertThat(eventEmitted.getTopic(), is(equalTo("DoiRegistrarService.Doi.Updated")));
            assertThat(eventEmitted.getItem(), allOf(hasProperty("doi", equalTo(Optional.of(VALID_SAMPLE_DOI))),
                                                     hasProperty(
                                                         "publicationIdentifier",
                                                         is(equalTo(new SortableIdentifier(publicationIdentifier))))));
        }
    }

    private static AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> crateAwsEventBridgeEvent(
        DoiUpdateRequestEvent doiUpdateRequestEvent) {
        var request = new AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>>();
        var awsEventBridgeDetail = new AwsEventBridgeDetail<DoiUpdateRequestEvent>();
        awsEventBridgeDetail.setResponsePayload(doiUpdateRequestEvent);
        request.setDetail(awsEventBridgeDetail);
        return request;
    }

    private void mockCreateDoiResponse() throws ClientException {
        when(doiClient.createDoi(any()))
            .thenAnswer(invocation -> Doi.fromUri(VALID_SAMPLE_DOI));
    }

    private DoiUpdateEvent readOutputStream(ByteArrayOutputStream outputStream) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(outputStream.toByteArray())) {
            return JsonUtils.dtoObjectMapper.readValue(bais, DoiUpdateEvent.class);
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

    private InputStream toInputStream(AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> request) {
        return attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(request)).map(IoUtils::stringToStream)
                   .orElseThrow();
    }

    private DoiUpdateRequestEvent createDoiUpdateRequest(String publicationID) {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         VALID_SAMPLE_DOI,
                                         UriWrapper.fromUri(createPublicationId(publicationID)).getUri(),
                                         CUSTOMER_ID_IN_INPUT_EVENT);
    }

    private DoiUpdateRequestEvent createDoiUpdateRequestNotContainingDoi(String publicationID) {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         null,
                                         UriWrapper.fromUri(createPublicationId(publicationID)).getUri(),
                                         CUSTOMER_ID_IN_INPUT_EVENT);
    }

    private String createPublicationId(String publicationIdentifier) {
        return baseUrl + "/publication/" + publicationIdentifier;
    }

    private void mockNotFoundResponse(String publicationID) {
        stubFor(WireMock.get(urlPathEqualTo("/publication/" + publicationID))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.datacite.datacite+xml"))
                    .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));
    }

    private DoiUpdateEvent parseResponse() {
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(outputStream.toString(), DoiUpdateEvent.class))
                   .orElseThrow();
    }
}
