package no.unit.nva.datacite.handlers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.google.common.net.HttpHeaders.ACCEPT;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.datacite.handlers.FindableDoiHandler.CUSTOMER_ID_IS_MISSING_ERROR_MESSAGE;
import static no.unit.nva.datacite.handlers.FindableDoiHandler.PUBLICATION_ID_IS_MISSING_ERROR_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.datacite.handlers.model.DoiResponse;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public class FindableDoiHandlerTest {

    private static final String DATACITE_XML_BODY = IoUtils.stringFromResources(Path.of("datacite.xml"));

    private static final URI VALID_SAMPLE_DOI = UriWrapper.fromUri("https://doi.org/10.1000/182").getUri();
    private static final URI CUSTOMER_ID_IN_INPUT_EVENT =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934").getUri();
    private static final URI NULL_CUSTOMER_ID_IN_INPUT_EVENT = null;
    private final Environment environment = mock(Environment.class);
    private final DoiClient doiClient = mock(DoiClient.class);
    private String baseUrl;
    private Context context;
    private ByteArrayOutputStream output;
    private FindableDoiHandler handler;

    @BeforeEach
    public void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
        baseUrl = wireMockRuntimeInfo.getHttpBaseUrl();
        when(environment.readEnv("API_HOST")).thenReturn(wireMockRuntimeInfo.getHttpsBaseUrl());
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FindableDoiHandler(doiClient, new DataCiteMetadataResolver(WiremockHttpClient.create()));
    }

    @Test
    void handleRequestThrowsIllegalArgumentExceptionOnMissingCustomerId() throws IOException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        handler.handleRequest(createRequestWithoutCustomer(publicationIdentifier), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        assertThat(response.getBodyObject(Problem.class).getDetail(),
                   containsString(CUSTOMER_ID_IS_MISSING_ERROR_MESSAGE));
    }

    @Test
    void handleRequestThrowsIllegalArgumentExceptionOnMissingPublicationId() throws IOException {
        handler.handleRequest(createRequestWithoutPublicationId(), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        assertThat(response.getBodyObject(Problem.class).getDetail(),
                   containsString(PUBLICATION_ID_IS_MISSING_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnFindableDoi() throws IOException, ClientException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        mockDataciteXmlBody(publicationIdentifier);
        handler.handleRequest(createRequest(publicationIdentifier), output, context);
        var response = GatewayResponse.fromOutputStream(output, DoiResponse.class);
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
        assertThat(expectedDoi.getUri(), is(equalTo(response.getBodyObject(DoiResponse.class).getDoi())));
    }

    private void mockDataciteXmlBody(String publicationIdentifier) {
        stubFor(WireMock.get(urlPathEqualTo("/publication/" + publicationIdentifier))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.datacite.datacite+xml"))
                    .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_OK).withBody(DATACITE_XML_BODY)));
    }

    private DoiUpdateRequestEvent createDoiUpdateRequestWithoutCustomer(String publicationId) {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         VALID_SAMPLE_DOI,
                                         UriWrapper.fromUri(createPublicationId(publicationId)).getUri(),
                                         NULL_CUSTOMER_ID_IN_INPUT_EVENT);
    }

    private DoiUpdateRequestEvent createDoiUpdateRequestWithoutPublicationId() {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         VALID_SAMPLE_DOI,
                                         null,
                                         CUSTOMER_ID_IN_INPUT_EVENT);
    }

    private DoiUpdateRequestEvent createDoiUpdateRequest(String publicationID) {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         VALID_SAMPLE_DOI,
                                         UriWrapper.fromUri(createPublicationId(publicationID)).getUri(),
                                         CUSTOMER_ID_IN_INPUT_EVENT);
    }

    private String createPublicationId(String publicationIdentifier) {
        return baseUrl + "/publication/" + publicationIdentifier;
    }

    private InputStream createRequestWithoutCustomer(String publicationId) throws JsonProcessingException {
        return new HandlerRequestBuilder<DoiUpdateRequestEvent>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                   .withBody(createDoiUpdateRequestWithoutCustomer(publicationId))
                   .build();
    }

    private InputStream createRequestWithoutPublicationId() throws JsonProcessingException {
        return new HandlerRequestBuilder<DoiUpdateRequestEvent>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                   .withBody(createDoiUpdateRequestWithoutPublicationId())
                   .build();
    }

    private InputStream createRequest(String publicationId) throws JsonProcessingException {
        return new HandlerRequestBuilder<DoiUpdateRequestEvent>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                   .withBody(createDoiUpdateRequest(publicationId))
                   .build();
    }
}



