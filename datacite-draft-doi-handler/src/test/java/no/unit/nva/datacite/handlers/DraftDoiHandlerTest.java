package no.unit.nva.datacite.handlers;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.datacite.handlers.DraftDoiHandler.CUSTOMER_ID_IS_MISSING_ERROR;
import static no.unit.nva.datacite.handlers.DraftDoiHandler.PUBLICATION_HAS_A_DOI_ALREADY;
import static no.unit.nva.datacite.handlers.DraftDoiHandler.PUBLICATION_ID_IS_MISSING_ERROR;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringToStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import no.unit.nva.datacite.commons.DoiUpdateEvent;
import no.unit.nva.datacite.commons.DoiUpdateRequestEvent;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.CreateDoiException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.invocation.InvocationOnMock;

public class DraftDoiHandlerTest {

    public static final String DOI_IDENTIFIER = "10.1052/identifier";
    public static final String EVENT_DETAIL_FIELD = "detail";
    public static final String DETAIL_RESPONSE_PAYLOAD_FIELD = "responsePayload";
    public static final String EXPECTED_ERROR_MESSAGE = "DoiClientExceptedErrorMessage";
    public static final String SAMPLE_DOI_PREFIX = "10.1234";
    public static final int SAMPLE_STATUS_CODE = 500;
    public static final String PUBLICATION_IDENTIFIER_IN_RESOURCE_FILES =
        "017772f8ce52-4d10352d-7974-472e-be34-484c5f4f194d";

    private DraftDoiHandler handler;
    private ByteArrayOutputStream outputStream;
    private Context context;

    private AtomicReference<URI> inputBuffer;

    @BeforeEach
    public void setUp() throws ClientException {
        var doiClient = doiClientReturningDoi();
        handler = new DraftDoiHandler(doiClient);
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
        inputBuffer = new AtomicReference<>();
    }

    @Test
    public void shouldThrowExceptionWhenDoiIsPresentInEvent() throws IOException {
        try (InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_doi_present.json")) {
            Executable action = () -> handler.handleRequest(inputStream, outputStream, context);
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, action);
            assertThat(exception.getMessage(), is(PUBLICATION_HAS_A_DOI_ALREADY));
        }
    }

    @Test
    public void handleRequestReturnsDoiUpdateDtoWithPublicationUriWhenInputHasInstitutionOwner() {
        InputStream inputStream = IoUtils.inputStreamFromResources("doi_publication_event_valid.json");
        handler.handleRequest(inputStream, outputStream, context);
        DoiUpdateEvent response = parseResponse();
        SortableIdentifier actualPublicationIdentifier = response.getItem().getPublicationIdentifier();
        assertThat(actualPublicationIdentifier.toString(), is(PUBLICATION_IDENTIFIER_IN_RESOURCE_FILES));
    }

    @Test
    public void handleRequestCallsDoiClientCreateDoiWhenInputHasInstitutionOwner() throws IOException {
        String inputString = IoUtils.stringFromResources(
            Path.of("doi_publication_event_valid.json"));

        URI expectedInputToDataCiteClient = extractCustomerIdFromEventBridgeEvent(inputString);
        handler.handleRequest(stringToStream(inputString), outputStream, context);
        URI actualInputToDataCiteCient = inputBuffer.get();
        assertThat(actualInputToDataCiteCient, is(equalTo(expectedInputToDataCiteClient)));
    }

    @Test
    public void handleRequestThrowsExceptionContainingTheCauseWhenDoiClientThrowsException()
        throws ClientException {
        String inputString = IoUtils.stringFromResources(Path.of("doi_publication_event_valid.json"));

        DraftDoiHandler handlerReceivingException = new DraftDoiHandler(doiClientThrowingException());
        Executable action = () -> handlerReceivingException.handleRequest(stringToStream(inputString), outputStream,
                                                                          context);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        Throwable actualCause = exception.getCause();
        assertThat(actualCause.getMessage(), containsString(EXPECTED_ERROR_MESSAGE));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingEventItem() throws IOException {
        try (InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_item.json")) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                              () -> handler.handleRequest(inputStream, outputStream,
                                                                                          context));
            assertThat(exception.getMessage(), is(PUBLICATION_ID_IS_MISSING_ERROR));
        }
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingCustomerId() throws IOException {
        try (InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_institution_owner.json")) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                              () -> handler.handleRequest(inputStream, outputStream,
                                                                                          context));

            assertThat(exception.getMessage(), is(CUSTOMER_ID_IS_MISSING_ERROR));
        }
    }

    private URI extractCustomerIdFromEventBridgeEvent(String inputString) throws IOException {

        JsonNode eventObject = dtoObjectMapper.readTree(inputString);
        JsonNode responsePayload = eventObject.path(EVENT_DETAIL_FIELD).path(DETAIL_RESPONSE_PAYLOAD_FIELD);
        DoiUpdateRequestEvent doiUpdateRequestEvent =
            dtoObjectMapper.convertValue(responsePayload, DoiUpdateRequestEvent.class);

        return doiUpdateRequestEvent.getCustomerId();
    }

    private DoiUpdateEvent parseResponse() {
        return attempt(() -> dtoObjectMapper.readValue(outputStream.toString(), DoiUpdateEvent.class))
                   .orElseThrow();
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
            throw new CreateDoiException(SAMPLE_DOI_PREFIX, SAMPLE_STATUS_CODE, EXPECTED_ERROR_MESSAGE);
        });
        return doiClient;
    }
}