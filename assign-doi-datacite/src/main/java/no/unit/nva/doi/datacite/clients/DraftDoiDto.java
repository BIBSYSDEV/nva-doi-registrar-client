package no.unit.nva.doi.datacite.clients;

import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.doi.models.Doi;
import nva.commons.utils.JacocoGenerated;

public class DraftDoiDto {

    public static final String DATA_FIELD = "data";
    public static final String ATTRIBUTES_FIELD = "attributes";
    public static final String DOI_FIELD = "doi";
    public static final String PREFIX_FIELD = "prefix";
    public static final String SUFFIX_FIELD = "suffix";
    public static final String TYPE_FIELD = "type";
    public static final String TYPE_FIELD_VALUE = "dois";

    private static final String PATH_SEPARATOR = "/";
    private String doi;
    private String prefix;
    private String suffix;

    @JacocoGenerated
    public DraftDoiDto() {

    }

    /**
     * Creates a DraftDoiDto from prefix and suffix.
     *
     * @param prefix the doi prefix.
     * @param suffix the doi suffix.
     * @return a DraftDoiDto.
     */
    public static DraftDoiDto create(String prefix, String suffix) {
        DraftDoiDto draftDoi = new DraftDoiDto();
        draftDoi.doi = prefix + PATH_SEPARATOR + suffix;
        draftDoi.prefix = prefix;
        draftDoi.suffix = suffix;
        return draftDoi;
    }

    /**
     * Create DraftDoiDto containing only the prefix.
     *
     * @param prefix the prefix
     * @return a DraftDoiDto.
     */
    public static DraftDoiDto fromPrefix(String prefix) {
        DraftDoiDto draftDoiDto = new DraftDoiDto();
        draftDoiDto.prefix = prefix;
        return draftDoiDto;
    }

    /**
     * Create a DraftDoiDto from a Json string.
     *
     * @param json a json object as it is expected and retuned from POST /dois endpoint in DataCite.
     * @return a DraftDoiDto.
     */
    public static DraftDoiDto fromJson(String json) {
        JsonNode tree = attempt(() -> objectMapper.readTree(json)).orElseThrow();
        DraftDoiDto draftDoiDto = new DraftDoiDto();
        JsonNode attributes = tree.path(DATA_FIELD).path(ATTRIBUTES_FIELD);
        draftDoiDto.prefix = attributes.get(PREFIX_FIELD).textValue();
        draftDoiDto.suffix = attributes.get(SUFFIX_FIELD).textValue();
        draftDoiDto.doi = attributes.get(DOI_FIELD).textValue();
        return draftDoiDto;
    }

    public String toJson() {
        ObjectNode rootNode = createJsonObjectWithNestedElements();
        return attempt(() -> objectMapper.writeValueAsString(rootNode)).orElseThrow();
    }

    public String getDoi() {
        return doi;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Creates a {@link Doi} object.
     *
     * @return a {@link Doi} object.
     */
    public Doi toDoi() {
        return Doi.builder()
            .withSuffix(getSuffix())
            .withPrefix(getPrefix())
            .build();
    }

    private ObjectNode createJsonObjectWithNestedElements() {
        ObjectNode rootNode = objectMapper.createObjectNode();
        ObjectNode data = objectMapper.createObjectNode();
        ObjectNode attributes = objectMapper.createObjectNode();

        rootNode.set(DATA_FIELD, data);
        rootNode.put(TYPE_FIELD, TYPE_FIELD_VALUE);
        data.set(ATTRIBUTES_FIELD, attributes);
        attributes.put(DOI_FIELD, doi);
        attributes.put(PREFIX_FIELD, prefix);
        attributes.put(SUFFIX_FIELD, suffix);
        return rootNode;
    }
}
