package no.unit.nva.datacite;

import static org.hamcrest.MatcherAssert.assertThat;

import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationDtoTestDataGenerator;
import no.unit.nva.transformer.dto.DynamoRecordDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class DynamoRecordDtoMapperTest {

    @Test
    public void canMapPublicationDtoToDynamoRecordDto() {
        Publication publication = PublicationDtoTestDataGenerator.createPublication();

        DynamoRecordDto dynamoRecordDto = DynamoRecordDtoMapper.fromPublication(publication);

        assertThat(dynamoRecordDto, Matchers.notNullValue());
    }

}
