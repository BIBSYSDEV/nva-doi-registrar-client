package no.unit.nva.doi.publisher;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

public class EventBridgeRetryClient {

    private final EventBridgeClient eventBridge;
    private final int maxAttempt;

    private static final Logger logger = LoggerFactory.getLogger(EventBridgeRetryClient.class);

    /**
     * Constructor for EventBridgeRetryClient.
     *
     * @param eventBridge   eventBridge
     * @param maxAttempt    maxAttempt
     */
    public EventBridgeRetryClient(EventBridgeClient eventBridge, int maxAttempt) {
        this.eventBridge = eventBridge;
        this.maxAttempt = maxAttempt;
    }

    /**
     * Put events on EventBridge EventBus.
     *
     * @param request   request
     * @return  list of PutEventsRequestEntry
     */
    public List<PutEventsRequestEntry> putEvents(final PutEventsRequest request) {
        PutEventsRequest requestCopy = request;
        int attemptCount = 0;
        while (attemptCount < maxAttempt) {
            logger.debug("Attempt {} to put events {}", attemptCount + 1, requestCopy);
            PutEventsResponse response = eventBridge.putEvents(requestCopy);

            if (response.failedEntryCount() == 0) {
                return Collections.emptyList();
            }

            List<PutEventsRequestEntry> requestEntries = requestCopy.entries();
            List<PutEventsResultEntry> resultEntries = response.entries();

            List<PutEventsRequestEntry> failedEntries = IntStream
                .range(0, resultEntries.size())
                .filter(i -> resultEntries.get(i).errorCode() != null)
                .mapToObj(requestEntries::get)
                .collect(Collectors.toList());

            requestCopy = PutEventsRequest.builder()
                .entries(failedEntries)
                .build();

            attemptCount++;
        }
        return requestCopy.entries();
    }
}
