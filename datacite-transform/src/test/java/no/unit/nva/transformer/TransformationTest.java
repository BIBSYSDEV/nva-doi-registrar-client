package no.unit.nva.transformer;

import static no.unit.nva.transformer.dto.CreatorDto.SEPARATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import java.util.List;
import javax.xml.bind.JAXBException;
import no.unit.nva.transformer.dto.CreatorDto;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import no.unit.nva.transformer.dto.IdentifierDto;
import no.unit.nva.transformer.dto.PublisherDto;
import no.unit.nva.transformer.dto.ResourceTypeDto;
import no.unit.nva.transformer.dto.TitleDto;
import org.junit.jupiter.api.Test;

class TransformationTest {

    public static final String ANY_URI = "https://example.org/123";
    public static final String ANY_NAME = "Wallace, Cornelius";
    public static final String ANY_TITLE = "A long, slow depressing march";
    public static final String ANY_YEAR = "2007";
    public static final String ANY_PUBLISHER = "Hubert's Æsopian university";
    public static final String ANY_RESOURCE_TYPE = "Article";
    public static final String RESOURCE_TYPE_OTHER = "Other";

    @Test
    void dynamoRecordDtoReturnsTransformedXmlWhenInputIsValid() throws JAXBException {
        var record = generateRecord(ANY_NAME,
                                    ANY_RESOURCE_TYPE);
        var actual = record.asXml();

        assertThat(actual, containsString(ANY_URI));
        assertThat(actual, containsString(ANY_NAME));
        assertThat(actual, containsString(ANY_TITLE));
        assertThat(actual, containsString(ANY_YEAR));
        assertThat(actual, containsString(ANY_PUBLISHER));
        assertThat(actual, containsString(ANY_RESOURCE_TYPE));
    }

    @Test
    void dynamoRecordDtoReturnsTransformedXmlWithSplitName()  {
        var surname = "Higgs";
        var forename = "Boson";
        var name = String.join(SEPARATOR, surname, forename);
        var record = generateRecord(name,
                                    ANY_RESOURCE_TYPE);
        var actual = record.asXml();
        assertThat(actual, containsString(name));
        assertThat(actual, containsString(enclosedString(surname)));
        assertThat(actual, containsString(enclosedString(forename)));
    }

    @Test
    void dynamoRecordDtoReturnsTransformedXmlWithoutSplitName() {
        String rank = "Bosun";
        String surname = "Higgs";

        var name = rank + " " + surname;
        var record = generateRecord(name,
                                    ANY_RESOURCE_TYPE);
        var actual = record.asXml();
        assertThat(actual, containsString(name));
        assertThat(actual, not(containsString(enclosedString(rank))));
        assertThat(actual, not(containsString(enclosedString(surname))));
    }

    @Test
    void dynamoRecordDtoReturnsTransformedXmlWithResourceTypeGeneralOtherWhenResourceTypeIsNull() throws JAXBException {
        var record = generateRecord(ANY_NAME,
                                    null);
        var actual = record.asXml();
        assertThat(actual, containsString(RESOURCE_TYPE_OTHER));
    }

    private String enclosedString(String string) {
        return ">" + string + "<";
    }

    private DataCiteMetadataDto generateRecord(String creator,
                                               String resourceType) {
        return new DataCiteMetadataDto.Builder()
                .withIdentifier(getIdentifier())
                .withCreator(getCreator(creator))
                .withTitle(getTitle())
                .withPublicationYear(TransformationTest.ANY_YEAR)
                .withPublisher(getPublisher())
                .withResourceType(getResourceType(resourceType))
                .build();
    }

    private ResourceTypeDto getResourceType(String resourceType) {
        return new ResourceTypeDto.Builder()
                .withValue(resourceType)
                .build();
    }

    private PublisherDto getPublisher() {
        return new PublisherDto.Builder()
                .withValue(TransformationTest.ANY_PUBLISHER)
                .build();
    }

    private TitleDto getTitle() {
        return new TitleDto.Builder()
                .withValue(TransformationTest.ANY_TITLE)
                .build();
    }

    private IdentifierDto getIdentifier() {
        return new IdentifierDto.Builder()
                .withValue(TransformationTest.ANY_URI)
                .build();
    }

    private List<CreatorDto> getCreator(String name) {
        return List.of(new CreatorDto.Builder()
                .withCreatorName(name).build());
    }
}