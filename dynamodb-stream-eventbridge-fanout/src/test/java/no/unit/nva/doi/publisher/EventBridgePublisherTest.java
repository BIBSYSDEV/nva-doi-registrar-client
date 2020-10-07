package no.unit.nva.doi.publisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry.Builder;

public class EventBridgePublisherTest {

    private static final String EVENT_BUS = UUID.randomUUID().toString();
    private static final Instant NOW = Instant.now();
    public static final String EXPECTED_DETAIL_TEMPLATE = "{\"eventSourceARN\":\"%s\"}";
    public static final String FAILED_EVENT_NAME = "Failed";
    public static final String SUCCESS_EVENT_NAME = "Success";
    public static final String RECORD_STRING_TEMPLATE = "{\"eventName\":\"%s\",\"eventSourceARN\":\"%s\"}";

    @Mock
    private EventBridgeRetryClient eventBridge;
    @Mock
    private EventPublisher failedEventPublisher;

    private EventPublisher publisher;

    /**
     * Set up environment for test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        publisher = new EventBridgePublisher(eventBridge, failedEventPublisher,
            EVENT_BUS, Clock.fixed(NOW, ZoneId.systemDefault()));
    }

    @Test
    public void publishCanPutEventsToEventBridge() {
        String eventSourceARN = UUID.randomUUID().toString();
        DynamodbEvent event = createDynamodbEvent(eventSourceARN);
        prepareMocksWithSuccessfulPutEvents();

        publisher.publish(event);

        PutEventsRequest expected = createPutEventsRequest(eventSourceARN);
        verify(eventBridge).putEvents(expected);
        verifyNoMoreInteractions(failedEventPublisher);
    }

    @Test
    public void publishFailedEventWhenPutEventsToEventBridgehasFailures() {
        String eventSourceARN = UUID.randomUUID().toString();
        Builder putEventsRequestEntryBuilder = createPutEventsRequestEntryBuilder(eventSourceARN);
        List<PutEventsRequestEntry> failedEntries = createFailedEntries(putEventsRequestEntryBuilder, eventSourceARN);
        prepareMocksWithFailingPutEventEntries(failedEntries);

        DynamodbStreamRecord successRecord = createDynamodbStreamRecord(eventSourceARN, SUCCESS_EVENT_NAME);
        DynamodbEvent.DynamodbStreamRecord failedRecord = createDynamodbStreamRecord(eventSourceARN, FAILED_EVENT_NAME);
        DynamodbEvent event = createDynamodbEvent(successRecord, failedRecord);
        publisher.publish(event);

        PutEventsRequest expected = createFailingPutEventsRequest(putEventsRequestEntryBuilder, eventSourceARN);
        verify(eventBridge).putEvents(expected);
        DynamodbEvent failedEvent = createDynamodbEvent(failedRecord);
        verify(failedEventPublisher).publish(failedEvent);
    }

    private PutEventsRequest createFailingPutEventsRequest(Builder builder, String eventSourceARN) {
        String successRecordString = String.format(RECORD_STRING_TEMPLATE, SUCCESS_EVENT_NAME, eventSourceARN);
        String failedRecordString = String.format(RECORD_STRING_TEMPLATE, FAILED_EVENT_NAME, eventSourceARN);
        return PutEventsRequest.builder()
            .entries(builder.detail(successRecordString).build(),
                builder.detail(failedRecordString).build())
            .build();
    }

    private Builder createPutEventsRequestEntryBuilder(String eventSourceARN) {
        Builder builder = PutEventsRequestEntry.builder()
            .eventBusName(EVENT_BUS)
            .time(NOW)
            .source(EventBridgePublisher.EVENT_SOURCE)
            .detailType(EventBridgePublisher.EVENT_DETAIL_TYPE)
            .resources(eventSourceARN);
        return builder;
    }

    private List<PutEventsRequestEntry> createFailedEntries(
        PutEventsRequestEntry.Builder builder,
        String eventSourceARN) {
        String failedRecordString = String.format(RECORD_STRING_TEMPLATE, FAILED_EVENT_NAME, eventSourceARN);
        List<PutEventsRequestEntry> failedEntries = Collections.singletonList(builder
            .detail(failedRecordString)
            .build());
        return failedEntries;
    }

    private DynamodbEvent.DynamodbStreamRecord createDynamodbStreamRecord(String eventSourceARN, String eventName) {
        DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventSourceARN(eventSourceARN);
        record.setEventName(eventName);
        return record;
    }

    private PutEventsRequest createPutEventsRequest(String eventSourceARN) {
        String expectedDetail = String.format(EXPECTED_DETAIL_TEMPLATE, eventSourceARN);
        return PutEventsRequest.builder()
            .entries(PutEventsRequestEntry.builder()
                .eventBusName(EVENT_BUS)
                .time(NOW)
                .source(EventBridgePublisher.EVENT_SOURCE)
                .detailType(EventBridgePublisher.EVENT_DETAIL_TYPE)
                .detail(expectedDetail)
                .resources(eventSourceARN)
                .build())
            .build();
    }

    private DynamodbEvent createDynamodbEvent(String eventSourceARN) {
        DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventSourceARN(eventSourceARN);
        return createDynamodbEvent(record);
    }

    private DynamodbEvent createDynamodbEvent(DynamodbStreamRecord... records) {
        DynamodbEvent event = new DynamodbEvent();
        event.setRecords(Arrays.asList(records));
        return event;
    }

    private void prepareMocksWithSuccessfulPutEvents() {
        when(eventBridge.putEvents(any(PutEventsRequest.class))).thenReturn(Collections.emptyList());
    }

    private void prepareMocksWithFailingPutEventEntries(List<PutEventsRequestEntry> failedEntries) {
        when(eventBridge.putEvents(any(PutEventsRequest.class))).thenReturn(failedEntries);
    }
}
