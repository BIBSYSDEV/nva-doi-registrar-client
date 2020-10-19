package no.unit.nva.events.models.doi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import no.unit.nva.events.models.doi.dto.Contributor;
import no.unit.nva.events.models.doi.dto.PublicationDate;
import no.unit.nva.events.models.doi.dto.PublicationType;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;

class PublicationMapperTest {

    public static final String EXAMPLE_PREFIX = "http://example.net/nva/publication/";
    public static final PublicationDate EXAMPLE_PUBLICATION_DATE = new PublicationDate("1999", null, null);
    private static final String EXAMPLE_IDENTIFIER = "dabb64fd-53c2-48ac-a5b7-a45a9572a3b1";
    private static final URI EXAMPLE_ID = URI.create(EXAMPLE_PREFIX + EXAMPLE_IDENTIFIER);
    private static final PublicationType EXAMPLE_PUBLICATION_TYPE = PublicationType.JOURNAL_ARTICLE;
    private static final String EXAMPLE_PUBLICATION_MAIN_TITLE = "Toward unique identifiers";
    private static final URI EXAMPLE_DOI = URI.create("https://doi.org/10.1109/5.771073");
    private static final URI EXAMPLE_INSTITUTION_OWNER = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934");
    private static final String EXAMPLE_CONTRIBUTOR_NAME = "Paskin, N.";
    public static final ObjectMapper objectMapper = JsonUtils.objectMapper;
    public static final String UKNOWN_DYNAMODB_STREAMRECORD_TYPE = "UknownType";
    public static final String ERROR_MUST_BE_PUBLICATION_TYPE = "Must be a dynamodb stream record of type Publication";

    @Test
    void fromDynamodbStreamRecord() throws IOException {
        var publication = PublicationMapper.fromDynamodbStreamRecord(EXAMPLE_PREFIX, IoUtils.stringFromResources(
            Path.of("validPublicationStreamRecordDetailExample.json")));
        assertThat(publication.getId(), is(equalTo(EXAMPLE_ID)));
        assertThat(publication.getType(), is(equalTo(EXAMPLE_PUBLICATION_TYPE)));
        assertThat(publication.getMainTitle(), is(equalTo(EXAMPLE_PUBLICATION_MAIN_TITLE)));
        assertThat(publication.getPublicationDate(), is(equalTo(EXAMPLE_PUBLICATION_DATE)));
        assertThat(publication.getDoi(), is(equalTo(EXAMPLE_DOI)));
        assertThat(publication.getInstitutionOwner(), is(equalTo(EXAMPLE_INSTITUTION_OWNER)));
        assertThat(publication.getContributor(), hasSize(1));
        assertThat(publication.getContributor(), hasItem(new Contributor(null, EXAMPLE_CONTRIBUTOR_NAME)));
    }

    @Test
    void unknownDynamodbStreamRecordType_ThrowsIllegalArgumentException() throws IOException {
        var rootNode = objectMapper.createObjectNode();
        var dynamodbStreamRecordType = rootNode
            .putObject("detail")
            .putObject("dynamodb")
            .putObject("newImage")
            .putObject("type");
        dynamodbStreamRecordType
            .put("s", UKNOWN_DYNAMODB_STREAMRECORD_TYPE);
        var actualException = assertThrows(IllegalArgumentException.class, () ->
            PublicationMapper.fromDynamodbStreamRecord(EXAMPLE_PREFIX, objectMapper.writeValueAsString(rootNode)));
        assertThat(actualException.getMessage(), containsString(ERROR_MUST_BE_PUBLICATION_TYPE));
    }
}