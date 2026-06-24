package no.unit.nva.datacite.handlers.resource;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.datacite.restclient.models.State;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceDraftedForDeletionEventHandlerTest {

  static final String DELETE_DRAFT_PUBLICATION_WITHOUT_DOI_JSON =
      "delete_draft_publication_without_doi.json";
  static final String DELETE_DRAFT_PUBLICATION_WITH_DOI_JSON =
      "delete_draft_publication_with_doi.json";
  static final String DOI_IDENTIFIER = "10.23/456789";

  private ResourceDraftedForDeletionEventHandler handler;
  private ByteArrayOutputStream outputStream;
  private Context context;

  @BeforeEach
  void setUp() throws ClientException {
    handler = new ResourceDraftedForDeletionEventHandler(doiClientReturningDoi(State.DRAFT));
    outputStream = new ByteArrayOutputStream();
    context = mock(Context.class);
  }

  @Test
  void handleRequestReturnsOutputWithoutDoiOnInputWithDoi() throws IOException {
    try (var inputStream =
        IoUtils.inputStreamFromResources(DELETE_DRAFT_PUBLICATION_WITH_DOI_JSON)) {
      handler.handleRequest(inputStream, outputStream, context);

      var event =
          dtoObjectMapper.readValue(
              outputStream.toString(StandardCharsets.UTF_8), ResourceDraftedForDeletionEvent.class);

      assertThat(event.hasDoi(), is(equalTo(false)));
    }
  }

  @Test
  void handleRequestThrowsExceptionOnInputWithoutDoi() throws IOException {
    try (var inputStream =
        IoUtils.inputStreamFromResources(DELETE_DRAFT_PUBLICATION_WITHOUT_DOI_JSON)) {

      assertThrows(
          RuntimeException.class,
          () -> handler.handleRequest(inputStream, outputStream, context),
          ResourceDraftedForDeletionEventHandler.EXPECTED_EVENT_WITH_DOI);
    }
  }

  @Test
  void handleRequestThrowsExceptionWhenRemoteServiceFails() throws ClientException, IOException {
    handler = new ResourceDraftedForDeletionEventHandler(doiClientReturningError());

    try (var inputStream =
        IoUtils.inputStreamFromResources(DELETE_DRAFT_PUBLICATION_WITH_DOI_JSON)) {
      assertThrows(
          RuntimeException.class,
          () -> handler.handleRequest(inputStream, outputStream, context),
          ResourceDraftedForDeletionEventHandler.ERROR_DELETING_DRAFT_DOI);
    }
  }

  @Test
  void handleRequestThrowsExceptionWhenDoiIsNotInDraftState() throws ClientException, IOException {
    handler = new ResourceDraftedForDeletionEventHandler(doiClientReturningDoi(State.FINDABLE));

    try (var inputStream =
        IoUtils.inputStreamFromResources(DELETE_DRAFT_PUBLICATION_WITH_DOI_JSON)) {
      assertThrows(
          RuntimeException.class,
          () -> handler.handleRequest(inputStream, outputStream, context),
          ResourceDraftedForDeletionEventHandler.NOT_DRAFT_DOI_ERROR);
    }
  }

  private DoiClient doiClientReturningError() throws ClientException {
    DoiClient doiClient = mock(DoiClient.class);
    when(doiClient.getDoi(any())).thenAnswer(invocation -> doiState(State.DRAFT));
    doThrow(new RuntimeException(ResourceDraftedForDeletionEventHandler.ERROR_DELETING_DRAFT_DOI))
        .when(doiClient)
        .deleteDraftDoi(any());
    return doiClient;
  }

  private DoiClient doiClientReturningDoi(State state) throws ClientException {
    DoiClient doiClient = mock(DoiClient.class);
    when(doiClient.getDoi(any())).thenAnswer(invocation -> doiState(state));
    return doiClient;
  }

  private DoiStateDto doiState(State state) {
    return new DoiStateDto(DOI_IDENTIFIER, state);
  }
}
