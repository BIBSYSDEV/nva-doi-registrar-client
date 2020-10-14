package no.unit.nva.events.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;

import com.amazonaws.services.eventbridge.AmazonEventBridge;
import com.amazonaws.services.eventbridge.AmazonEventBridgeClientBuilder;
import com.amazonaws.services.eventbridge.model.PutEventsRequest;
import com.amazonaws.services.eventbridge.model.PutEventsRequestEntry;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Optional;
import no.unit.nva.events.examples.DataciteDoiRequest;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for publishing custom messages at will. Will be deleted
 */
public class EventProducer implements RequestStreamHandler {

    public static final String EVENT_BUS_ENV_VAR = "EVENT_BUS";
    public static final String SOURCE = "SomeSource";
    public static final String LOG_HANDLER_HAS_RUN = "Event Producer has been called!!!";
    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    private final Environment environment;
    private final AmazonEventBridge eventBridgeClient;

    @JacocoGenerated
    public EventProducer() {
        this(new Environment(), AmazonEventBridgeClientBuilder.defaultClient());
    }

    @JacocoGenerated
    public EventProducer(Environment environment, AmazonEventBridge eventBridgeClient) {
        this.environment = environment;
        this.eventBridgeClient = eventBridgeClient;
    }

    @JacocoGenerated
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        DataciteDoiRequest sentDirectly = newDataciteDoiRequest();
        logger.info(LOG_HANDLER_HAS_RUN);
        putEventDirectlyToEventBridge(sentDirectly);
        DataciteDoiRequest sentThroughLambdaDestination =
            sentDirectly.copy().withPublicationId(URI.create("https://localhost/fromOutputStream")).build();
        writeOutput(sentThroughLambdaDestination, output);
    }

    @JacocoGenerated
    private <I> void writeOutput(I event, OutputStream outputStream)
        throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String responseJson = Optional.ofNullable(objectMapper.writeValueAsString(event))
                .map(StringUtils::replaceWhiteSpacesWithSpace)
                .map(StringUtils::removeMultipleWhiteSpaces)
                .orElseThrow();
            logger.info(responseJson);
            writer.write(responseJson);
        }
    }

    @JacocoGenerated
    private void putEventDirectlyToEventBridge(DataciteDoiRequest dataciteDoiRequest) {
        logger.info("Putting event directly to eventbridge");
        PutEventsRequestEntry putEventsRequestEntry = new PutEventsRequestEntry()
            .withDetail(dataciteDoiRequest.toString())
            .withEventBusName(environment.readEnv(EVENT_BUS_ENV_VAR))
            .withSource(SOURCE)
            .withDetailType(dataciteDoiRequest.getType());

        PutEventsRequest putEventsRequest = new PutEventsRequest().withEntries(putEventsRequestEntry);
        eventBridgeClient.putEvents(putEventsRequest);
    }

    @JacocoGenerated
    private DataciteDoiRequest newDataciteDoiRequest() {
        return DataciteDoiRequest.newBuilder()
            .withExistingDoi(URI.create("http://somedoi.org"))
            .withPublicationId(URI.create("https://somepublication.com"))
            .withXml("Somexml")
            .withType("MyType")
            .build();
    }
}
