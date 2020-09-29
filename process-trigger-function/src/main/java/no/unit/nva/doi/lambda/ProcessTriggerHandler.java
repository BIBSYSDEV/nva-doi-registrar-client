package no.unit.nva.doi.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import java.time.Duration;
import no.unit.nva.doi.publisher.EventBridgePublisher;
import no.unit.nva.doi.publisher.EventBridgeRetryClient;
import no.unit.nva.doi.publisher.EventPublisher;
import no.unit.nva.doi.publisher.SqsEventPublisher;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public class ProcessTriggerHandler implements RequestHandler<DynamodbEvent, Void> {

    public static final String AWS_REGION = "AWS_REGION";
    private final EventPublisher eventPublisher;

    public ProcessTriggerHandler() {
        this(defaultEventBridgePublisher());
    }

    private static EventPublisher defaultEventBridgePublisher() {
        return new EventBridgePublisher(
            defaultEventBridgeRetryClient(),
            defaultFailedEventPublisher(),
            Env.getEventBusName()
        );
    }

    private static EventPublisher defaultFailedEventPublisher() {
        return new SqsEventPublisher(defaultSqsClient(), Env.getDlqUrl());
    }

    private static SqsClient defaultSqsClient() {
        return SqsClient.builder()
            .region(Region.of(System.getenv(AWS_REGION)))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();
    }

    private static EventBridgeRetryClient defaultEventBridgeRetryClient() {
        return new EventBridgeRetryClient(defaultEventBridgeClient(), Env.getMaxAttempt());
    }

    private static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder()
            .region(Region.of(System.getenv(AWS_REGION)))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .overrideConfiguration(ClientOverrideConfiguration.builder()
                .apiCallAttemptTimeout(Duration.ofSeconds(1))
                .retryPolicy(RetryPolicy.builder().numRetries(10).build())
                .build())
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();
    }

    public ProcessTriggerHandler(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {

        eventPublisher.publish(event);

        return null;
    }
}
