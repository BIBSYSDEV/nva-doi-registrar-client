package no.unit.nva.doi.publisher;

import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsPublisherTest {
    private static final String QUEUE_URL = UUID.randomUUID().toString();
    @Mock
    private SqsClient sqs;

    private EventPublisher publisher;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        publisher = new SqsEventPublisher(sqs, QUEUE_URL);
    }

    @Test
    public void publish() {
        DynamodbEvent event = new DynamodbEvent();
        DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventName("test");
        event.setRecords(Collections.singletonList(record));

        publisher.publish(event);

        String expectedBody = "{\"records\":[{\"eventName\":\"test\"}]}";
        SendMessageRequest expected = SendMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .messageBody(expectedBody)
                .build();
        verify(sqs).sendMessage(expected);
    }
}
