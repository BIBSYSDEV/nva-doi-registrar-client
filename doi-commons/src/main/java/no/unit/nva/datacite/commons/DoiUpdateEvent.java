package no.unit.nva.datacite.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;

public class DoiUpdateEvent implements JsonSerializable {

    public static final String DOI_UPDATED_EVENT_TOPIC = "DoiRegistrarService.Doi.Updated";
    public static final String TOPIC = "topic";
    public static final String ITEM = "item";

    @JsonProperty(TOPIC)
    private final String topic;
    @JsonProperty(ITEM)
    private final DoiUpdateDto item;

    @JsonCreator
    public DoiUpdateEvent(@JsonProperty(TOPIC) String topic, @JsonProperty(ITEM) DoiUpdateDto item) {
        this.topic = topic;
        this.item = item;
    }

    public String getTopic() {
        return topic;
    }

    public boolean hasItem() {
        return item != null;
    }

    public DoiUpdateDto getItem() {
        return item;
    }
}
