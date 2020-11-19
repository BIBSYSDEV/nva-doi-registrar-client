package no.unit.nva.datacite.handlers;

import static no.unit.nva.datacite.handlers.LandingPageUtil.getLandingPage;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.models.Doi;
import no.unit.nva.doi.datacite.clients.models.ImmutableDoi;
import no.unit.nva.publication.doi.update.dto.DoiUpdateHolder;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FindableDoiEventHandlerTest {

    public static final Path PUBLICATION_EVENT = Path.of("doi_request_event.json");
    private static final String DEMO_PREFIX = "10.5072";
    private final DoiClient doiClient = mock(DoiClient.class);
    private final FindableDoiEventHandler findableDoiHandler = new FindableDoiEventHandler(doiClient);
    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void init() {
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    public void handleRequestReturnsDoiUpdateHolderWithDtoContainingPublicationUriWhenInputIsValid()
        throws ClientException {
        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT);
        findableDoiHandler.handleRequest(inputStream, outputStream, context);
        DoiUpdateHolder response = parseResponse();
        assertThat(response.getItem().getPublicationId(), is(not(nullValue())));

        URI expectedCustomerId = URI.create(
            "https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934");
        verify(doiClient).setLandingPage(expectedCustomerId, createExpectedDoi(),
            getLandingPage(response.getItem().getPublicationId()));
    }

    @Test
    public void handleRequestSuccessfullyIsLogged() {
        TestAppender testingAppender = LogUtils.getTestingAppender(FindableDoiEventHandler.class);

        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT);
        findableDoiHandler.handleRequest(inputStream, outputStream, context);

        assertThat(testingAppender.getMessages(), containsString("Successfully handled request for Doi"));
    }

    @Test
    public void handleRequestTransformsPublicationToDataciteXmlFormatWhenInputIsPublicationWithAprrovedDoiRequest() {
        throw new RuntimeException();
    }

    private ImmutableDoi createExpectedDoi() {
        return Doi.builder()
            .withPrefix(DEMO_PREFIX)
            .withSuffix("need-pr-BIBSYSDEV-nva-publication-api-pull-96")
            .build();
    }

    private DoiUpdateHolder parseResponse() {
        return attempt(() -> JsonUtils.objectMapper.readValue(outputStream.toString(), DoiUpdateHolder.class))
            .orElseThrow();
    }
}
