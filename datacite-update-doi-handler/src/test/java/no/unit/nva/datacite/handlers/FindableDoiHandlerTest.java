package no.unit.nva.datacite.handlers;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.doi.datacite.clients.DataCiteRestApiClient.ACCEPT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import no.unit.nva.datacite.commons.DataCiteMetadataResolver;
import no.unit.nva.datacite.commons.TestBase;
import org.apache.hc.core5.http.ContentType;
import no.unit.nva.datacite.handlers.model.DoiResponse;
import no.unit.nva.datacite.handlers.model.UpdateDoiRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpsEnabled = true)
public class FindableDoiHandlerTest extends TestBase {

    private static final String DATACITE_XML_BODY = IoUtils.stringFromResources(Path.of("datacite.xml"));

    private static final URI VALID_SAMPLE_DOI = UriWrapper.fromUri("https://doi.org/10.1000/182").getUri();
    private static final URI CUSTOMER_ID_IN_INPUT_EVENT =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934").getUri();
    private final Environment environment = mock(Environment.class);
    private final DoiClient doiClient = mock(DoiClient.class);

    private Context context;
    private ByteArrayOutputStream output;
    private FindableDoiHandler handler;

    @BeforeEach
    public void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
        setBaseUrl(wireMockRuntimeInfo.getHttpBaseUrl());
        when(environment.readEnv("API_HOST")).thenReturn(wireMockRuntimeInfo.getHttpsBaseUrl());
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FindableDoiHandler(doiClient, new DataCiteMetadataResolver(WiremockHttpClient.create()),
                                         new Environment());
    }

    @Test
    void shouldReturnFindableDoi() throws IOException, ClientException {
        var publicationIdentifier = SortableIdentifier.next().toString();
        mockDataciteXmlBody(publicationIdentifier, DATACITE_XML_BODY);
        handler.handleRequest(createRequest(publicationIdentifier), output, context);
        var response = GatewayResponse.fromOutputStream(output, DoiResponse.class);
        var expectedDoi = Doi.fromUri(VALID_SAMPLE_DOI);
        verify(doiClient).updateMetadata(
            eq(expectedDoi),
            eq(DATACITE_XML_BODY));
        verify(doiClient).setLandingPage(
            expectedDoi,
            UriWrapper.fromUri(createPublicationId(publicationIdentifier)).getUri()
        );
        assertThat(expectedDoi.getUri(), is(equalTo(response.getBodyObject(DoiResponse.class).getDoi())));
    }

    private InputStream createRequest(String publicationId) throws JsonProcessingException {
        return new HandlerRequestBuilder<UpdateDoiRequest>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, ContentType.APPLICATION_JSON.getMimeType()))
                   .withBody(createDoiUpdateRequest(publicationId))
                   .build();
    }

    private UpdateDoiRequest createDoiUpdateRequest(String publicationID) {
        return new UpdateDoiRequest(
            VALID_SAMPLE_DOI,
            UriWrapper.fromUri(createPublicationId(publicationID)).getUri(),
            CUSTOMER_ID_IN_INPUT_EVENT);
    }
}



