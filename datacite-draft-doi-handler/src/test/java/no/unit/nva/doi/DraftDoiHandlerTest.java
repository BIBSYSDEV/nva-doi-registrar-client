package no.unit.nva.doi;

import static no.unit.nva.datacite.handlers.DraftDoiHandler.CUSTOMER_ID_IS_MISSING_ERROR;
import static no.unit.nva.datacite.handlers.DraftDoiHandler.NOT_APPROVED_DOI_REQUEST_ERROR;
import static no.unit.nva.datacite.handlers.DraftDoiHandler.PUBLICATION_IS_MISSING_ERROR;
import static nva.commons.utils.IoUtils.stringToStream;
import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

import java.util.concurrent.atomic.AtomicReference;
import no.unit.nva.datacite.handlers.DraftDoiHandler;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.CreateDoiException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import no.unit.nva.publication.doi.update.dto.DoiUpdateDto;
import nva.commons.utils.IoUtils;
import org.hamcrest.MatcherAssert;
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

    private DoiClient doiClient;
    private DraftDoiHandler handler;
    private ByteArrayOutputStream outputStream;
    private Context context;

    private AtomicReference<URI> inputBuffer;

    @BeforeEach
    public void setUp() throws ClientException {
        doiClient = doiClientReturningDoi();
        handler = new DraftDoiHandler(doiClient);
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
        inputBuffer = new AtomicReference<>();
    }

    @Test
    public void handleRequestReturnsDoiUpdateDtoWithPublicationUriWhenInputHasInstitutionOwner() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_valid.json"));
        handler.handleRequest(inputStream, outputStream, context);

        DoiUpdateDto response = parseResponse();
        assertThat(response.getPublicationId(), is(not(nullValue())));
    }

    @Test
    public void handleRequestCallsDoiClientCreateDoiWhenInputHasInstitutionOwner() throws JsonProcessingException {
        String inputString = IoUtils.stringFromResources(
            Path.of("doi_publication_event_valid.json"));

        URI expectedInputToDataCiteClient = extractExpectedPublisherIdFromEventBridgeEvent(inputString);
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
        assertThat(actualCause.getMessage(),containsString(EXPECTED_ERROR_MESSAGE));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingEventItem() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_item.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(PUBLICATION_IS_MISSING_ERROR));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingCustomerId() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_institution_owner.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(CUSTOMER_ID_IS_MISSING_ERROR));
    }

    @Test
    public void handleRequestThrowsIllegalStateExceptionWhenPublicationHasNotBeenApproved() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_publication_not_approved.json"));
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), containsString(NOT_APPROVED_DOI_REQUEST_ERROR));
    }

    @SuppressWarnings("unchecked")
    private URI extractExpectedPublisherIdFromEventBridgeEvent(String inputString) throws JsonProcessingException {

        JsonNode eventObject = objectMapper.readTree(inputString);
        JsonNode responsePayload = eventObject.path(EVENT_DETAIL_FIELD).path(DETAIL_RESPONSE_PAYLOAD_FIELD);
        PublicationHolder publicationHolder = objectMapper.convertValue(responsePayload, PublicationHolder.class);

        return publicationHolder.getItem().getInstitutionOwner();
    }

    private DoiUpdateDto parseResponse() {
        return attempt(() -> objectMapper.readValue(outputStream.toString(), DoiUpdateDto.class))
            .orElseThrow();
    }

    private DoiClient doiClientReturningDoi() throws ClientException {
        DoiClient doiClient = mock(DoiClient.class);
        Doi doi = Doi.builder().withIdentifier(DOI_IDENTIFIER).build();
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