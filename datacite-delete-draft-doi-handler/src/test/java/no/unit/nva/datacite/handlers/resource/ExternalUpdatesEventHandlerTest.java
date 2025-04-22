package no.unit.nva.datacite.handlers.resource;

import static no.unit.nva.doi.datacite.restclient.models.State.DRAFT;
import static no.unit.nva.testutils.RandomDataGenerator.randomDoi;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.datacite.restclient.models.State;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class ExternalUpdatesEventHandlerTest {

    private static final String MESSAGE_BODY_TEMPLATE =
        stringFromResources(Path.of("sqsMessageBodyTemplate.json"));

    private Environment environment;
    private DoiClient doiClient;

    @BeforeEach
    public void beforeEach() {
        environment = mock(Environment.class);
        doiClient = mock(DoiClient.class);
    }

    @Test
    void shouldReturnWithoutIssuesIfNoMessagesInEvent() {

        var handler = new ExternalUpdatesEventHandler(environment, new FakeS3Client(), doiClient);

        assertDoesNotThrow(() -> handler.handleRequest(new SQSEvent(), new FakeContext()));
    }

    @Test
    void shouldFailOnMessageFromUnknownTopic() {
        var s3Uri = randomUri();
        var messageBody = stringFromResources(Path.of("sqsMessageWithUnexpectedTopic.json"));
        var eventReference = "ignoreMe";
        var fixture = prepareForTesting(s3Uri, eventReference, messageBody);

        assertThrows(
            EventHandlingException.class,
            () -> fixture.handler().handleRequest(fixture.sqsEvent(), new FakeContext()));
    }

    @Test
    void shouldFailOnUnknownActionInS3Event() {
        var s3Uri = randomUri();
        var messageBody = generateMessageBody(s3Uri);
        var eventReference = stringFromResources(Path.of("s3EventReferenceWithUnexpectedAction.json"));
        var fixture = prepareForTesting(s3Uri, eventReference, messageBody);

        assertThrows(
            EventHandlingException.class,
            () -> fixture.handler().handleRequest(fixture.sqsEvent(), new FakeContext()));
    }

    @Test
    void shouldFailWhenNotAbleToParseS3EventData() {
        var s3Uri = randomUri();
        var messageBody = generateMessageBody(s3Uri);
        var unparsableS3EventReference =
            stringFromResources(Path.of("unparsableS3EventReference.json"));
        var fixture =
            prepareForTesting(s3Uri, unparsableS3EventReference, messageBody);

        assertThrows(
            EventHandlingException.class,
            () -> fixture.handler().handleRequest(fixture.sqsEvent(), new FakeContext()));
    }

    @Test
    void shouldFailWhenNotAbleToParseEventReference() {
        var s3Uri = randomUri();
        var invalidMessageBody = stringFromResources(Path.of("unparsableSqsMessageBody.json"));
        var eventReference = "ignoreMe";
        var fixture =
            prepareForTesting(s3Uri, eventReference, invalidMessageBody);

        assertThrows(
            EventHandlingException.class,
            () -> fixture.handler().handleRequest(fixture.sqsEvent(), new FakeContext()));
    }

    @Test
    void shouldDeleteDraftedDoiWhenS3EventContainsRemoveActionWithDoiInDraftState() throws ClientException {
        var s3Uri = randomUri();
        var messageBody = generateMessageBody(s3Uri);
        var customerId = UriWrapper.fromUri("https://apihost/customer")
                             .addChild(SortableIdentifier.next().toString())
                             .getUri();
        var doi = randomDoi();
        var eventReference = generateEventReference(customerId, doi);
        var fixture = prepareForTesting(s3Uri, eventReference, messageBody);
        var draftDoi = new DoiStateDto(doi.toString(), DRAFT);
        doReturn(draftDoi).when(doiClient).getDoi(eq(customerId), eq(Doi.fromUri(doi)));

        assertDoesNotThrow(() -> fixture.handler().handleRequest(fixture.sqsEvent(), new FakeContext()));

        verify(doiClient, times(1)).deleteDraftDoi(eq(customerId), any());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, value = State.class, names = "DRAFT")
    void shouldNotDeleteDoiWhenS3EventContainsRemoveActionWithDoiInNonDraftState(State state) throws ClientException {
        var s3Uri = randomUri();
        var messageBody = generateMessageBody(s3Uri);
        var customerId = UriWrapper.fromUri("https://apihost/customer")
                             .addChild(SortableIdentifier.next().toString())
                             .getUri();
        var doi = randomDoi();
        var eventReference = generateEventReference(customerId, doi);
        var fixture = prepareForTesting(s3Uri, eventReference, messageBody);
        var actualDoiState = new DoiStateDto(doi.toString(), state);
        doReturn(actualDoiState).when(doiClient).getDoi(eq(customerId), eq(Doi.fromUri(doi)));

        assertDoesNotThrow(() -> fixture.handler().handleRequest(fixture.sqsEvent(), new FakeContext()));

        verify(doiClient, times(0)).deleteDraftDoi(eq(customerId), any());
    }

    private String generateEventReference(URI customerId, URI doi) {
        var eventReferenceTemplate = stringFromResources(Path.of("eventReferenceTemplate.json"));
        return String.format(eventReferenceTemplate, customerId, doi);
    }

    private static String generateMessageBody(URI uri) {
        return String.format(MESSAGE_BODY_TEMPLATE, uri);
    }

    private Fixture prepareForTesting(
        URI uri, String eventReference, String invalidMessageBody) {
        var filename = UriWrapper.fromUri(uri).getLastPathElement();
        var s3Client =
            FakeS3Client.fromContentsMap(
                Map.of(
                    filename,
                    new ByteArrayInputStream(eventReference.getBytes(StandardCharsets.UTF_8))));
        var handler = new ExternalUpdatesEventHandler(environment, s3Client, doiClient);

        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(invalidMessageBody);

        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        return new Fixture(handler, sqsEvent);
    }

    private record Fixture(ExternalUpdatesEventHandler handler, SQSEvent sqsEvent) {

    }
}
