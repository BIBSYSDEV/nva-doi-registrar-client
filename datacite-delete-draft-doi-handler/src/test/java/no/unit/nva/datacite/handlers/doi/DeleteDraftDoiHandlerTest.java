package no.unit.nva.datacite.handlers.doi;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.datacite.handlers.doi.DeleteDraftDoiHandler.ERROR_DELETING_DRAFT_DOI;
import static no.unit.nva.datacite.handlers.doi.DeleteDraftDoiHandler.ERROR_GETTING_DOI_STATE;
import static no.unit.nva.datacite.handlers.doi.DeleteDraftDoiHandler.EXPECTED_EVENT_WITH_DOI;
import static no.unit.nva.datacite.handlers.doi.DeleteDraftDoiHandler.NOT_DRAFT_DOI_ERROR;
import static no.unit.nva.datacite.handlers.doi.DeleteDraftDoiHandler.PUBLICATION_HAS_NO_PUBLISHER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import no.unit.nva.datacite.commons.DoiUpdateEvent;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeleteDraftDoiHandlerTest {

    private static final String DOI_IDENTIFIER = "10.23/456789";
    private static final String DOI_STATE_DRAFT = "draft";
    private static final String DOI_STATE_FINDABLE = "findable";

    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    void shouldDeleteDoiIfDoiIsInDraftState() throws IOException, ClientException {
        var doiClient = doiClientMock(DOI_STATE_DRAFT);
        var handler = new DeleteDraftDoiHandler(doiClient);

        try (InputStream inputStream = IoUtils.inputStreamFromResources("delete_draft_doi_request_ok.json")) {
            handler.handleRequest(inputStream, outputStream, context);

            verify(doiClient, times(1)).deleteDraftDoi(any(), any());

            DoiUpdateEvent event = dtoObjectMapper.readValue(outputStream.toString(), DoiUpdateEvent.class);
            assertThat(event.getItem().getDoi().isEmpty(), is(equalTo(true)));
        }
    }

    @Test
    void shouldThrowExceptionIfDoiIsNotInDraftState() throws IOException, ClientException {
        var doiClient = doiClientMock(DOI_STATE_FINDABLE);
        var handler = new DeleteDraftDoiHandler(doiClient);

        try (InputStream inputStream = IoUtils.inputStreamFromResources("delete_draft_doi_request_ok.json")) {
            assertThrows(RuntimeException.class,
                         () -> handler.handleRequest(inputStream, outputStream, context),
                         NOT_DRAFT_DOI_ERROR);
        }
    }

    @Test
    void shouldThrowExceptionIfNoPublicationInEvent() throws IOException, ClientException {
        var doiClient = doiClientMock(DOI_STATE_DRAFT);
        var handler = new DeleteDraftDoiHandler(doiClient);

        try (InputStream inputStream
                 = IoUtils.inputStreamFromResources("delete_draft_doi_request_no_item.json")) {
            assertThrows(RuntimeException.class,
                         () -> handler.handleRequest(inputStream, outputStream, context),
                         EXPECTED_EVENT_WITH_DOI);
        }
    }

    @Test
    void shouldThrowExceptionIfNoDoiInEvent() throws IOException, ClientException {
        var doiClient = doiClientMock(DOI_STATE_DRAFT);
        var handler = new DeleteDraftDoiHandler(doiClient);

        try (InputStream inputStream = IoUtils.inputStreamFromResources("delete_draft_doi_request_no_doi.json")) {
            assertThrows(RuntimeException.class,
                         () -> handler.handleRequest(inputStream, outputStream, context),
                         EXPECTED_EVENT_WITH_DOI);
        }
    }

    @Test
    void shouldThrowExceptionIfNotAbleToCheckDoiState() throws IOException, ClientException {
        var doiClient = doiClientMockThrowingExceptionGettingDoi();
        var handler = new DeleteDraftDoiHandler(doiClient);

        try (InputStream inputStream = IoUtils.inputStreamFromResources("delete_draft_doi_request_ok.json")) {
            assertThrows(RuntimeException.class,
                         () -> handler.handleRequest(inputStream, outputStream, context),
                         ERROR_GETTING_DOI_STATE);
        }
    }

    @Test
    void shouldThrowExceptionIfNotAbleToDeleteDraftDoi() throws IOException, ClientException {
        var doiClient = doiClientMockThrowingExceptionDeletingDraftDoi();
        var handler = new DeleteDraftDoiHandler(doiClient);

        try (InputStream inputStream = IoUtils.inputStreamFromResources("delete_draft_doi_request_ok.json")) {
            assertThrows(RuntimeException.class,
                         () -> handler.handleRequest(inputStream, outputStream, context),
                         ERROR_DELETING_DRAFT_DOI);
        }
    }

    @Test
    void shouldThrowExceptionIfNoPublisherIsPresentInPublication() throws IOException, ClientException {
        var doiClient = doiClientMock(DOI_STATE_DRAFT);
        var handler = new DeleteDraftDoiHandler(doiClient);

        try (InputStream inputStream
                 = IoUtils.inputStreamFromResources("delete_draft_doi_request_no_publisher_in_publication.json")) {
            assertThrows(RuntimeException.class,
                         () -> handler.handleRequest(inputStream, outputStream, context),
                         PUBLICATION_HAS_NO_PUBLISHER);
        }
    }

    private DoiClient doiClientMock(String state) throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        when(doiClient.getDoi(any(), any())).thenReturn(doiState(state));
        doNothing().when(doiClient).deleteDraftDoi(any(), any());
        return doiClient;
    }

    private DoiClient doiClientMockThrowingExceptionGettingDoi() throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        when(doiClient.getDoi(any(), any())).thenThrow(new ClientException());

        return doiClient;
    }

    private DoiClient doiClientMockThrowingExceptionDeletingDraftDoi() throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        when(doiClient.getDoi(any(), any())).thenReturn(doiState(DOI_STATE_DRAFT));
        doThrow(new ClientException()).when(doiClient).deleteDraftDoi(any(), any());

        return doiClient;
    }

    private DoiStateDto doiState(String state) {
        return new DoiStateDto(DOI_IDENTIFIER, state);
    }
}
