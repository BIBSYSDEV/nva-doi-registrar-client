package no.unit.nva.doi;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationDtoTestDataGenerator;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DataCiteMetadataDtoMapperTest {

    @Test
    public void canMapPublicationDtoToDynamoRecordDto() {
        Publication publication = new PublicationDtoTestDataGenerator().createRandomStreamRecord().asPublicationDto();

        DataCiteMetadataDto dataCiteMetadataDto = DataCiteMetadataDtoMapper.fromPublication(publication);

        assertThat(dataCiteMetadataDto, doesNotHaveNullOrEmptyFields());
    }

    @Test
    public void canMapEmptyPublicationDtoToEmptyDynamoRecord() {
        Publication publication = Mockito.mock(Publication.class);

        DataCiteMetadataDto dataCiteMetadataDto = DataCiteMetadataDtoMapper.fromPublication(publication);

        assertThat(dataCiteMetadataDto, notNullValue());
    }

}
