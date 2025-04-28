package no.unit.nva.datacite.handlers;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.ApiGatewayHandler.ALLOWED_ORIGIN_ENV;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.concurrent.atomic.AtomicReference;
import no.unit.nva.datacite.model.DoiResponse;
import no.unit.nva.datacite.model.ReserveDoiRequest;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public class ReserveDraftDoiHandlerTest {

    public static final String DOI_IDENTIFIER = "10.1052/identifier";

    public static final String EXPECTED_ERROR_MESSAGE = "DoiClientExceptedErrorMessage";

    public static final int SAMPLE_STATUS_CODE = 500;

    public static final String SAMPLE_DOI_PREFIX = "10.1234";
    private static final String COGNITO_AUTHORIZER_URLS = "COGNITO_AUTHORIZER_URLS";
    private static final String API_HOST = "API_HOST";
    private final Environment environment = mock(Environment.class);
    private Context context;
    private AtomicReference<URI> inputBuffer;
    private ByteArrayOutputStream output;
    private ReserveDraftDoiHandler handler;

    @BeforeEach
    public void setUp() {
        context = mock(Context.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn("*");
        when(environment.readEnv(API_HOST)).thenReturn("localhost");
        when(environment.readEnv(COGNITO_AUTHORIZER_URLS)).thenReturn("http://localhost:3000");
        output = new ByteArrayOutputStream();
        inputBuffer = new AtomicReference<>();
    }

    @Test
    void shouldReturnBadGatewayWhenBadResponseFromDataCite() throws IOException, ClientException {
        var customerId = randomUri();
        var request = createRequest(customerId);
        handler = new ReserveDraftDoiHandler(doiClientThrowingException(), environment);
        handler.handleRequest(request, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    @Test
    void shouldReturnDoiSuccessfully() throws IOException, ClientException {
        var customerId = randomUri();
        var expectedDoi = URI.create("https://doi.org/" + DOI_IDENTIFIER);
        var request = createRequest(customerId);
        handler = new ReserveDraftDoiHandler(doiClientReturningDoi(), environment);
        handler.handleRequest(request, output, context);
        var response = GatewayResponse.fromOutputStream(output, DoiResponse.class);
        var actualDoi = response.getBodyObject(DoiResponse.class);
        assertThat(actualDoi.getDoi(), is(equalTo(expectedDoi)));
    }

    private InputStream createRequest(URI customerId) throws JsonProcessingException {
        return new HandlerRequestBuilder<ReserveDoiRequest>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                   .withBody(new ReserveDoiRequest(customerId))
                   .build();
    }

    private DoiClient doiClientReturningDoi() throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        Doi doi = Doi.fromDoiIdentifier(DOI_IDENTIFIER);
        when(doiClient.createDoi(any()))
            .thenAnswer(invocation -> saveInputAndReturnSampleDoi(doi, invocation));
        return doiClient;
    }

    private Doi saveInputAndReturnSampleDoi(Doi doi, InvocationOnMock invocation) {
        URI customerId = invocation.getArgument(0);
        inputBuffer.set(customerId);
        return doi;
    }

    private DoiClient doiClientThrowingException() throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        when(doiClient.createDoi(any())).thenAnswer(invocation -> {
            throw new ClientException("Some exception");
        });
        return doiClient;
    }

}
