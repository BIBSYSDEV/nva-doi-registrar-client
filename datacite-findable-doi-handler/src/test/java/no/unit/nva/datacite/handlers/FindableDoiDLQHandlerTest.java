package no.unit.nva.datacite.handlers;

import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FindableDoiDLQHandlerTest {

    private final Context context = mock(Context.class);
    private FindableDoiDLQHandler handler;

    @BeforeEach
    void init() {
        this.handler = new FindableDoiDLQHandler();
    }

    @Test
    void dummyTest() {
        var sqsEvent = createDummySqsEvent();
        handler.handleRequest(sqsEvent, context);
    }

    private SQSEvent createDummySqsEvent() {
        return new SQSEvent();
    }
}
