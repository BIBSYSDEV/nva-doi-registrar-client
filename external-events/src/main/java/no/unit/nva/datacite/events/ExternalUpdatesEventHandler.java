package no.unit.nva.datacite.events;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.DataCiteClientV2;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.events.models.EventReference;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.SingletonCollector;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

public class ExternalUpdatesEventHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(ExternalUpdatesEventHandler.class);
    private static final TypeReference<AwsEventBridgeEvent<AwsEventBridgeDetail<EventReference>>>
        SQS_VALUE_TYPE_REF = new TypeReference<>() {};
    private static final Set<String> HANDLED_TOPICS = Set.of("PublicationService.Resource.Deleted");
    private static final String EVENTS_BUCKET_NAME_ENV = "EVENTS_BUCKET_NAME";

    private final S3Driver s3Driver;
    private final DoiManager doiManager;

    @JacocoGenerated
    public ExternalUpdatesEventHandler() {
        this(
            new Environment(),
            S3Driver.defaultS3Client().build(),
            new DoiManager(defaultDoiClient()));
    }

    protected ExternalUpdatesEventHandler(
        Environment environment, S3Client s3Client, DoiManager doiManager) {
        this.s3Driver = new S3Driver(s3Client, environment.readEnv(EVENTS_BUCKET_NAME_ENV));
        this.doiManager = doiManager;
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
        if (!HANDLED_TOPICS.contains(eventReference.getTopic())) {
            return;
        }

        var updateEvent = getEventBodyFromS3(eventReference);
        if (updateEvent.isDeletionOfResourceWithDoi()) {
            deleteDoiIfDrafted(updateEvent.oldData());
        }
    }

    private void deleteDoiIfDrafted(Resource deletedResource) {
        var doi = Doi.fromUri(deletedResource.doi());
        try {
            doiManager.deleteDoiIfOnlyDrafted(doi);
        } catch (ClientException e) {
            var message = String.format("Failed to check state or delete doi %s", doi);
            throw new EventHandlingException(message, e);
        }

        var resourceIdentifier = deletedResource.identifier();
        logger.info("Deleted draft DOI {} as resource {} was deleted.", doi.getUri(), resourceIdentifier);
    }

    private ResourceUpdateEvent getEventBodyFromS3(EventReference eventReference) {
        var event = s3Driver.readEvent(eventReference.getUri());
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(event, ResourceUpdateEvent.class))
                   .orElseThrow(this::logAndThrow);
    }

    private RuntimeException logAndThrow(Failure<ResourceUpdateEvent> updateEventFailure) {
        final Throwable cause = updateEventFailure.getException();
        logger.error("Unable to parse s3 event reference", cause);
        throw new EventHandlingException(
            "Failed to parse s3 event reference!", cause);
    }

    private static EventReference parseEventReference(SQSMessage sqs) {
        var event = attempt(() -> JsonUtils.dtoObjectMapper.readValue(sqs.getBody(), SQS_VALUE_TYPE_REF))
                        .orElseThrow(failure ->
                                         new EventHandlingException("Failed to parse event body",
                                                                    failure.getException()));

        return Optional.ofNullable(event)
                   .stream()
                   .map(AwsEventBridgeEvent::getDetail)
                   .map(AwsEventBridgeDetail::getResponsePayload)
                   .collect(SingletonCollector.tryCollect())
                   .orElseThrow(new EventHandlingException("Failed to extract response payload from event body"));
    }

    @JacocoGenerated
    private static DoiClient defaultDoiClient() {
        return new DataCiteClientV2();
    }
}
