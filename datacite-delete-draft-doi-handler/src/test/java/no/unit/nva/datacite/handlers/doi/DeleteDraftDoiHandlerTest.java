package no.unit.nva.datacite.handlers.doi;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomDoi;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import no.unit.nva.datacite.handlers.model.DeleteDraftDoiRequest;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.DeleteDraftDoiException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public class DeleteDraftDoiHandlerTest {

    public static final String DRAFT_STATE = "draft";
    public static final String PUBLISHED_STATE = "PUBLISHED";
    private final Environment environment = mock(Environment.class);
    private Context context;
    private ByteArrayOutputStream output;

    @BeforeEach
    public void setUp() {
        context = mock(Context.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        output = new ByteArrayOutputStream();
    }

    @Test
    public void shouldDeleteDraftDoiSuccessfully() throws ClientException, IOException {
        var doi = randomDoi();
        var request = createRequest(doi);
        var handler = new DeleteDraftDoiHandler(doiClientReturningDoi(doi, DRAFT_STATE), environment);
        handler.handleRequest(request, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_ACCEPTED)));
    }

    @Test
    public void shouldReturnBadGatewayWhenBadResponseFromDataCiteVerifyingDoiStatus()
        throws ClientException, IOException {
        var doi = randomDoi();
        var handler = new DeleteDraftDoiHandler(doiClientThrowingException(doi), environment);
        handler.handleRequest(createRequest(doi), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    @Test
    public void shouldReturnBadGatewayWhenDoiIsNotADraft()
        throws IOException, ClientException {
        var doi = randomDoi();
        var handler = new DeleteDraftDoiHandler(doiClientReturningDoi(doi, PUBLISHED_STATE), environment);
        handler.handleRequest(createRequest(doi), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_METHOD)));
    }

    @Test
    public void shouldReturnBadGatewayWhenDoiClientFailsOnDraftDoiDeletion()
        throws ClientException, IOException {
        var doi = randomDoi();
        var handler = new DeleteDraftDoiHandler(doiClientThrowingExceptionWhenDeleting(doi), environment);
        handler.handleRequest(createRequest(doi), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    private DoiClient doiClientThrowingException(URI doi) throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        when(doiClient.getDoi(any(), any())).thenAnswer(invocation -> {
            throw new DeleteDraftDoiException(Doi.fromUri(doi), HttpURLConnection.HTTP_BAD_GATEWAY);
        });
        return doiClient;
    }

    private DoiClient doiClientThrowingExceptionWhenDeleting(URI doi) throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        when(doiClient.getDoi(any(), any()))
            .thenAnswer(invocation -> new DoiStateDto(String.valueOf(doi), "draft"));
        doThrow(new DeleteDraftDoiException(Doi.fromUri(doi), HttpURLConnection.HTTP_BAD_GATEWAY))
            .when(doiClient).deleteDraftDoi(any(), any());
        return doiClient;
    }

    private DoiClient doiClientReturningDoi(URI doi, String state) throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        when(doiClient.getDoi(any(), any()))
            .thenAnswer(invocation -> new DoiStateDto(String.valueOf(doi), state));
        return doiClient;
    }

    private InputStream createRequest(URI doi) throws JsonProcessingException {
        return new HandlerRequestBuilder<DeleteDraftDoiRequest>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                   .withBody(new DeleteDraftDoiRequest(doi, randomUri(), randomUri()))
                   .build();
    }
}
