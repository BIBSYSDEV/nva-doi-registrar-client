package no.unit.nva.datacite.handlers;

import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import no.unit.nva.datacite.DoiClient;
import no.unit.nva.datacite.model.DoiUpdateDto;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FindableDoiEventHandlerTest {

    public static final Path PUBLICATION_EVENT = Path.of("doi_request_event.json");

    private final FindableDoiEventHandler findableDoiHandler = new FindableDoiEventHandler();

    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void init() {
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    public void handleRequestReturnsDoiUpdateDtoWithPublicationUriWhenInputIsValid() {
        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT);
        findableDoiHandler.handleRequest(inputStream, outputStream, context);
        DoiUpdateDto response = parseResponse();
        assertThat(response.getPublicationId(), is(not(nullValue())));
    }

    @Test
    public void handleRequestTransformsPublicationToDataciteXmlFormatWhenInputIsPublicationWithAprrovedDoiRequest() {
        throw new RuntimeException();
    }

    private DoiUpdateDto parseResponse() {
        return attempt(() -> JsonUtils.objectMapper.readValue(outputStream.toString(), DoiUpdateDto.class))
            .orElseThrow();
    }
}
