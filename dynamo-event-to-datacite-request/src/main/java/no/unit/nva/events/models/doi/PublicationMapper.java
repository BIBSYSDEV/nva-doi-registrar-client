package no.unit.nva.events.models.doi;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import no.unit.nva.events.models.doi.dto.Contributor;
import no.unit.nva.events.models.doi.dto.Contributor.Builder;
import no.unit.nva.events.models.doi.dto.Publication;
import no.unit.nva.events.models.doi.dto.Publication.PublicationBuilder;
import no.unit.nva.events.models.doi.dto.PublicationDate;
import no.unit.nva.events.models.doi.dto.PublicationType;
import nva.commons.utils.JsonUtils;

public final class PublicationMapper {

    public static final String ROOT = "/detail/dynamodb";
    public static final String PUBLICATION_TYPE = "Publication";
    private static final JsonPointer CONTRIBUTORS_LIST_POINTER = JsonPointer.compile(
        ROOT + "/newImage/entityDescription/m/contributors/l");
    private static final JsonPointer CONTRIBUTOR_ARP_ID_JSON_POINTER = JsonPointer.compile("/m/identity/m/arpId/s");
    private static final JsonPointer CONTRIBUTOR_NAME_JSON_POINTER = JsonPointer.compile("/m/identity/m/name/s");
    private static final JsonPointer PUBLICATION_IDENTIFIER_POINTER
        = JsonPointer.compile(ROOT + "/newImage/identifier/s");
    private static final JsonPointer PUBLICATION_TYPE_POINTER = JsonPointer.compile(
        ROOT + "/newImage/entityDescription/m/reference/m/publicationInstance/m/type/s");
    private static final JsonPointer PUBLICATION_ENTITY_DESCRIPTION_POINTER = JsonPointer.compile(
        ROOT + "/newImage/entityDescription/m");
    private static final JsonPointer DOI_POINTER = JsonPointer.compile(
        ROOT + "/newImage/entityDescription/m/reference/m/doi/s");
    private static final JsonPointer MAIN_TITLE_POINTER = JsonPointer.compile(
        ROOT + "/newImage/entityDescription/m/mainTitle/s");
    private static final JsonPointer TYPE_POINTER = JsonPointer.compile(ROOT + "/newImage/type/s");
    private static final JsonPointer INSTITUTION_OWNER_POINTER = JsonPointer.compile(ROOT + "/newImage/publisherId/s");

    private PublicationMapper() {

    }

    /**
     * Map to doi.Publication from a dynamo db stream record from nva_publication / nva_resources
     *
     * @param publicationIdPrefix                        prefix for a publication, from running environment
     *                                                   (https://nva.unit.no/publication)
     * @param dynamodbStreamRecordSerializedAsJsonString detail.dynamodb serialized as a string
     * @return Publication doi.Publication
     * @throws IOException on IO exception
     */
    public static Publication fromDynamodbStreamRecord(String publicationIdPrefix,
                                                       String dynamodbStreamRecordSerializedAsJsonString)
        throws IOException {
        var record = JsonUtils.objectMapper.readTree(dynamodbStreamRecordSerializedAsJsonString);

        var typeAttribute = textFromNode(record, TYPE_POINTER);
        if (typeAttribute == null || !typeAttribute.equals(PUBLICATION_TYPE)) {
            throw new IllegalArgumentException("Must be a dynamodb stream record of type Publication");
        }
        var publicationBuilder = PublicationBuilder.newBuilder();
        publicationBuilder
            .withId(URI.create(publicationIdPrefix + textFromNode(record, PUBLICATION_IDENTIFIER_POINTER)))
            .withType(PublicationType.findByName(textFromNode(record, PUBLICATION_TYPE_POINTER)))
            .withPublicationDate(new PublicationDate(record.at(PUBLICATION_ENTITY_DESCRIPTION_POINTER)))
            .withTitle(textFromNode(record, MAIN_TITLE_POINTER))
            .withInstitutionOwner(URI.create(textFromNode(record, INSTITUTION_OWNER_POINTER)))
            .withContributor(toStream(record.at(CONTRIBUTORS_LIST_POINTER))
                .map(PublicationMapper::extractContributor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        extractDoiUrl(record).ifPresent(publicationBuilder::withDoi);
        return publicationBuilder.build();
    }

    private static Optional<URI> extractDoiUrl(JsonNode record) {
        return Optional.ofNullable(textFromNode(record, DOI_POINTER))
            .map(URI::create);
    }

    private static Stream<JsonNode> toStream(JsonNode node) {
        return StreamSupport.stream(node.spliterator(), false);
    }

    private static Contributor extractContributor(JsonNode jsonNode) {
        String identifier = textFromNode(jsonNode, CONTRIBUTOR_ARP_ID_JSON_POINTER);
        String name = textFromNode(jsonNode, CONTRIBUTOR_NAME_JSON_POINTER);
        Builder builder = new Builder();
        if (identifier != null) {
            builder.withId(URI.create(identifier));
        }
        return nonNull(name) ? builder.withName(name).build() : null;
    }

    private static String textFromNode(JsonNode jsonNode, JsonPointer jsonPointer) {
        JsonNode json = jsonNode.at(jsonPointer);
        return isPopulatedJsonPointer(json) ? json.asText() : null;
    }

    private static boolean isPopulatedJsonPointer(JsonNode json) {
        return !json.isNull() && !json.asText().isBlank();
    }
}
