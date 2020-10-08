package no.unit.nva.doi.updatedoi.model.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;

class PublicationDynamodbEventTest {
    private static final ObjectMapper objectMapper = JsonUtils.objectMapper.
        enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

    @Test
    public void testDeserializeExampleEvent() throws IOException {

        InputStream inputStream = IoUtils.inputStreamFromResources(
            new File("java/no/unit/nva/doi/updatedoi/model/event/publication_dynamodb_event_example.json").toPath());
        var publicationDynamodbEvent = objectMapper.readValue(inputStream, PublicationDynamodbEvent.class);

        var detail = publicationDynamodbEvent.getDetail();
        assertThat(detail, not(nullValue()));

        Map<String, AttributeValue> newImage = detail.getDynamodb().getNewImage();
        assertThat(newImage.get("status").getS(), is(equalTo("Draft")));


    }

}