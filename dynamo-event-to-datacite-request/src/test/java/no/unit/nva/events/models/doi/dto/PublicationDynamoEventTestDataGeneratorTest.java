package no.unit.nva.events.models.doi.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import org.junit.jupiter.api.Test;

class PublicationDynamoEventTestDataGeneratorTest {

    @Test
    void asDynamoDbEvent() {
        var dynamodbEvent = new PublicationDynamoEventTestDataGenerator().createRandomStreamRecord()
            .createRandomStreamRecord()
            .asDynamoDbEvent();

        assertThat(dynamodbEvent.getRecords(), hasSize(2));

        // Validate one specific key that should always be present in the Record to validate the serialization worked.
        var identifier = dynamodbEvent.getRecords().get(0).getDynamodb().getNewImage().get("identifier").getS();
        assertThat(identifier, notNullValue());
        // See PublicationStreamRecordTestDataGenerator(Test)
    }
}