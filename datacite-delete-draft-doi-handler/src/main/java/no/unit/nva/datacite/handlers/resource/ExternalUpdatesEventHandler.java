package no.unit.nva.datacite.handlers.resource;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClientV2;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.State;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

public class ExternalUpdatesEventHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(ExternalUpdatesEventHandler.class);
    private static final TypeReference<AwsEventBridgeEvent<AwsEventBridgeDetail<EventReference>>>
        SQS_VALUE_TYPE_REF = new TypeReference<>() {
    };
    private static final String RESOURCE_DELETED_TOPIC = "PublicationService.Resource.Deleted";
    private static final String EVENTS_BUCKET_NAME_ENV = "EVENTS_BUCKET_NAME";
    private static final String REMOVE_ACTION = "REMOVE";

    private final S3Driver s3Driver;
    private final DoiClient doiClient;

    @JacocoGenerated
    public ExternalUpdatesEventHandler() {
        this(
            new Environment(),
            S3Driver.defaultS3Client().build(),
            defaultDoiClient());
    }

    protected ExternalUpdatesEventHandler(
        Environment environment, S3Client s3Client, DoiClient doiClient) {
        this.s3Driver = new S3Driver(s3Client, environment.readEnv(EVENTS_BUCKET_NAME_ENV));
        this.doiClient = doiClient;
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        Optional.ofNullable(sqsEvent.getRecords()).stream()
            .flatMap(List::stream)
            .map(ExternalUpdatesEventHandler::parseEventReference)
            .filter(Objects::nonNull)
            .forEach(this::processPayload);
        return null;
    }

    private void processPayload(EventReference eventReference) {
        if (!RESOURCE_DELETED_TOPIC.equals(eventReference.getTopic())) {
            logger.error(
                "Got external update event with message on unknown topic {}", eventReference.getTopic());
            throw new EventHandlingException(
                "Received external update event with message on unknown topic!");
        }

        var event = s3Driver.readEvent(eventReference.getUri());
        var updateEvent =
            attempt(() -> JsonUtils.dtoObjectMapper.readValue(event, UpdateEvent.class))
                .orElseThrow(this::logAndThrow);

        if (!REMOVE_ACTION.equals(updateEvent.action())) {
            logger.error("Received unknown action in s3 event: {}", updateEvent.action());
            throw new EventHandlingException("Unknown action in s3 event data. Expected REMOVE!");
        }

        var customerId = updateEvent.oldData().publisher().id();
        if (nonNull(updateEvent.oldData().doi())) {
            var doi = Doi.fromUri(updateEvent.oldData().doi());
            try {
                var doiState = doiClient.getDoi(customerId, doi);
                if (State.DRAFT.equals(doiState.getState())) {
                    doiClient.deleteDraftDoi(customerId, doi);
                } else {
                    logger.info("DOI {} is not in draft state, skipping deletion", doi);
                }
            } catch (ClientException e) {
                throw new EventHandlingException("Failed to look up doi", e);
            }
        }
    }

    private RuntimeException logAndThrow(Failure<UpdateEvent> updateEventFailure) {
        logger.error("Unable to parse s3 event reference", updateEventFailure.getException());
        throw new EventHandlingException(
            "Failed to parse s3 event reference!", updateEventFailure.getException());
    }

    private static EventReference parseEventReference(SQSMessage sqs) {
        logger.info("Processing sqsEvent: {}", sqs.getBody());
        try {
            var event = JsonUtils.dtoObjectMapper.readValue(sqs.getBody(), SQS_VALUE_TYPE_REF);

            return event.getDetail().getResponsePayload();
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse event body", e);
            throw new EventHandlingException("Failed to parse message body", e);
        }
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        return new DataCiteClientV2();
    }
}
