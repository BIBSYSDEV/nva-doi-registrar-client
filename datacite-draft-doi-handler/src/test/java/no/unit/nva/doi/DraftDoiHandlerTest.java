package no.unit.nva.doi;

import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DraftDoiHandlerTest {

    public static final String DOI_IDENTIFIER = "10.1052/identifier";
    private DoiClient doiClient;
    private DraftDoiHandler handler;
    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void setUp() throws ClientException {
        doiClient = getDoiClientMock();
        handler = new DraftDoiHandler(doiClient);
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    public void handleRequestReturnsDoiUpdateDtoWithPublicationUriWhenInputIsValid() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_valid.json"));
        handler.handleRequest(inputStream,outputStream,context);
        DoiUpdateDto response = parseResponse();
        assertThat(response.getPublicationId(), is(not(nullValue())));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingEventItem() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_item.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));

        MatcherAssert.assertThat(exception.getMessage(), is(DraftDoiHandler.PUBLICATION_IS_MISSING_ERROR));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingCustomerId() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_institution_owner.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));

        MatcherAssert.assertThat(exception.getMessage(), is(DraftDoiHandler.CUSTOMER_ID_IS_MISSING_ERROR));
    }

    private DoiUpdateDto parseResponse() {
        return attempt(() -> JsonUtils.objectMapper.readValue(outputStream.toString(), DoiUpdateDto.class))
            .orElseThrow();
    }

    private DoiClient getDoiClientMock() throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        Doi doi = Doi.builder().withIdentifier(DOI_IDENTIFIER).build();
        Mockito.when(doiClient.createDoi(Mockito.any())).thenReturn(doi);
        return doiClient;
    }
}
