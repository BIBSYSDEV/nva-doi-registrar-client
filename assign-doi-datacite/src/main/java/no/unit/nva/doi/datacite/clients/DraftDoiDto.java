package no.unit.nva.doi.datacite.clients;

import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public static DraftDoiDto create(String prefix, String suffix) {
        DraftDoiDto draftDoi = new DraftDoiDto();
        draftDoi.doi = prefix + PATH_SEPARATOR + suffix;
        draftDoi.prefix = prefix;
        draftDoi.suffix = suffix;
        return draftDoi;
    }

    public static DraftDoiDto fromPrefix(String prefix) {
        DraftDoiDto draftDoiDto = new DraftDoiDto();
        draftDoiDto.prefix = prefix;
        return draftDoiDto;
    }

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


    private ObjectNode createJsonObjectWithNestedElements() {
        ObjectNode rootNode = objectMapper.createObjectNode();
        ObjectNode data = objectMapper.createObjectNode();
        ObjectNode attributes = objectMapper.createObjectNode();

        rootNode.set(DATA_FIELD, data);
        rootNode.put(TYPE_FIELD,TYPE_FIELD_VALUE);
        data.set(ATTRIBUTES_FIELD, attributes);
        attributes.put(DOI_FIELD, doi);
        attributes.put(PREFIX_FIELD, prefix);
        attributes.put(SUFFIX_FIELD, suffix);
        return rootNode;
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
}
