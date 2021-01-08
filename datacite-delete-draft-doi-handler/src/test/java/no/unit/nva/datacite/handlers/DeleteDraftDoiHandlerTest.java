package no.unit.nva.datacite.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.publication.events.DeletePublicationEvent;
import nva.commons.utils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteDraftDoiHandlerTest {

    public static final String DELETE_DRAFT_PUBLICATION_WITHOUT_DOI_JSON = "delete_draft_publication_without_doi.json";
    public static final String DELETE_DRAFT_PUBLICATION_WITH_DOI_JSON = "delete_draft_publication_with_doi.json";

    public static final String DOI_IDENTIFIER = "10.23/456789";
    public static final String NOT_DRAFT = "findable";

    private DoiClient doiClient;
    private DeleteDraftDoiHandler handler;
    private ByteArrayOutputStream outputStream;
    private Context context;

    private AtomicReference<URI> inputBuffer;

    @BeforeEach
    public void setUp() throws ClientException {
        doiClient = doiClientReturningDoi(DeleteDraftDoiHandler.DRAFT);
        handler = new DeleteDraftDoiHandler(doiClient);
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
        inputBuffer = new AtomicReference<>();
    }

    @Test
    public void handleRequestReturnsOutputWithoutDoiOnInputWithDoi() throws JsonProcessingException {
        InputStream inputStream = IoUtils.inputStreamFromResources(DELETE_DRAFT_PUBLICATION_WITH_DOI_JSON);

        handler.handleRequest(inputStream, outputStream, context);

        DeletePublicationEvent event = objectMapper.readValue(outputStream.toString(), DeletePublicationEvent.class);

        assertThat(event.hasDoi(), is(equalTo(false)));
    }

    @Test
    public void handleRequestThrowsExceptionOnInputWithoutDoi() {
        InputStream inputStream = IoUtils.inputStreamFromResources(DELETE_DRAFT_PUBLICATION_WITHOUT_DOI_JSON);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));
        assertThat(exception.getMessage(), is(equalTo(DeleteDraftDoiHandler.EXPECTED_EVENT_WITH_DOI)));
    }

    @Test
    public void handleRequestThrowsExceptionWhenDoiIsNotInDraftState() throws ClientException {
        doiClient = doiClientReturningDoi(NOT_DRAFT);
        handler = new DeleteDraftDoiHandler(doiClient);

        InputStream inputStream = IoUtils.inputStreamFromResources(DELETE_DRAFT_PUBLICATION_WITH_DOI_JSON);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));
        assertThat(exception.getMessage(), is(equalTo(DeleteDraftDoiHandler.NOT_DRAFT_DOI_ERROR)));
    }

    private DoiClient doiClientReturningDoi(String state) throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        Doi doi = Doi.builder().withIdentifier(DOI_IDENTIFIER).build();
        when(doiClient.createDoi(any()))
                .thenAnswer(invocation -> saveInputAndReturnSampleDoi(doi, invocation));
        when(doiClient.getDoi(any(),any()))
                .thenAnswer(invocation -> doiState(DOI_IDENTIFIER, state));
        return doiClient;
    }

    private DoiStateDto doiState(String doiIdentifier, String state) {
        return new DoiStateDto(doiIdentifier, state);
    }

    private Doi saveInputAndReturnSampleDoi(Doi doi, InvocationOnMock invocation) {
        URI customerId = invocation.getArgument(0);
        inputBuffer.set(customerId);
        return doi;
    }

}
