package no.unit.nva.doi.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

public class EventBridgeRetryClientTest {

    private static final int MAX_ATTEMPT = 3;
    @Mock
    private EventBridgeClient eventBridge;

    private EventBridgeRetryClient client;

    /**
     * Set up environment for test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        client = new EventBridgeRetryClient(eventBridge, MAX_ATTEMPT);
    }

    @Test
    public void putEvents_noFailure() {
        PutEventsResponse response = PutEventsResponse.builder()
            .failedEntryCount(0)
            .build();
        when(eventBridge.putEvents(any(PutEventsRequest.class))).thenReturn(response);

        PutEventsRequest request = PutEventsRequest.builder()
            .build();
        List<PutEventsRequestEntry> result = client.putEvents(request);

        assertEquals(0, result.size());
        verify(eventBridge).putEvents(request);
    }

    @Test
    public void putEvents_retryThenNoFailure() {
        List<PutEventsRequestEntry> requestEntries = new ArrayList<>();
        requestEntries.add(PutEventsRequestEntry.builder()
            .detail("success entry")
            .build());
        PutEventsRequestEntry failedEntry = PutEventsRequestEntry.builder()
            .detail("failed entry")
            .build();
        requestEntries.add(failedEntry);
        List<PutEventsResultEntry> resultEntries = new ArrayList<>();
        resultEntries.add(PutEventsResultEntry.builder().build());
        resultEntries.add(PutEventsResultEntry.builder()
            .errorCode("failed")
            .build());
        PutEventsResponse firstResponse = PutEventsResponse.builder()
            .failedEntryCount(1)
            .entries(resultEntries)
            .build();
        PutEventsResponse secondResponse = PutEventsResponse.builder()
            .failedEntryCount(0)
            .build();
        when(eventBridge.putEvents(any(PutEventsRequest.class)))
            .thenReturn(firstResponse)
            .thenReturn(secondResponse);

        PutEventsRequest request = PutEventsRequest.builder()
            .entries(requestEntries)
            .build();
        List<PutEventsRequestEntry> result = client.putEvents(request);

        assertEquals(0, result.size());
        ArgumentCaptor<PutEventsRequest> putEventsRequestArgumentCaptor = ArgumentCaptor.forClass(
            PutEventsRequest.class);
        verify(eventBridge, times(2)).putEvents(putEventsRequestArgumentCaptor.capture());
        List<PutEventsRequest> expected = new ArrayList<>();
        expected.add(request);
        PutEventsRequest secondRequest = PutEventsRequest.builder()
            .entries(Collections.singletonList(failedEntry))
            .build();
        expected.add(secondRequest);
        assertEquals(putEventsRequestArgumentCaptor.getAllValues(), expected);
    }

    @Test
    public void putEvents_maxAttempt() {
        List<PutEventsRequestEntry> requestEntries = new ArrayList<>();
        requestEntries.add(PutEventsRequestEntry.builder()
            .detail("success entry")
            .build());
        PutEventsRequestEntry failedEntry = PutEventsRequestEntry.builder()
            .detail("failed entry")
            .build();
        requestEntries.add(failedEntry);

        List<PutEventsResultEntry> resultEntries = new ArrayList<>();
        resultEntries.add(PutEventsResultEntry.builder().build());
        PutEventsResultEntry failedResponseEntry = PutEventsResultEntry.builder()
            .errorCode("failed")
            .build();
        resultEntries.add(failedResponseEntry);
        PutEventsResponse response = PutEventsResponse.builder()
            .failedEntryCount(1)
            .entries(resultEntries)
            .build();
        PutEventsResponse failedResponse = PutEventsResponse.builder()
            .failedEntryCount(1)
            .entries(Collections.singletonList(failedResponseEntry))
            .build();
        when(eventBridge.putEvents(any(PutEventsRequest.class)))
            .thenReturn(response)
            .thenReturn(failedResponse)
            .thenReturn(failedResponse);

        PutEventsRequest request = PutEventsRequest.builder()
            .entries(requestEntries)
            .build();
        List<PutEventsRequestEntry> result = client.putEvents(request);

        PutEventsRequest failedRequest = PutEventsRequest.builder()
            .entries(Collections.singletonList(failedEntry))
            .build();
        assertEquals(result, failedRequest.entries());
        ArgumentCaptor<PutEventsRequest> putEventsRequestArgumentCaptor = ArgumentCaptor.forClass(
            PutEventsRequest.class);
        verify(eventBridge, times(3)).putEvents(putEventsRequestArgumentCaptor.capture());
        List<PutEventsRequest> expected = new ArrayList<>();
        expected.add(request);
        expected.add(failedRequest);
        expected.add(failedRequest);
        assertEquals(putEventsRequestArgumentCaptor.getAllValues(), expected);
    }
}
