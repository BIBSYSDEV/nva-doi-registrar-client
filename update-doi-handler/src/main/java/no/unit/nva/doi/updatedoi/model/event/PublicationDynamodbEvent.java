package no.unit.nva.doi.updatedoi.model.event;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * { "version": "0", "id": "7cb06021-4f20-815f-2d6b-6864ac847d24", "detail-type": "dynamodb-stream-event", "source":
 * "aws-dynamodb-stream-eventbridge-fanout", "account": "884807050265", "time": "2020-10-06T07:02:50Z", "region":
 * "eu-west-1", "resources": [ "arn:aws:dynamodb:eu-west-1:884807050265:table/nva_resources/stream/2020-08-19T09:27
 * :44.804"
 * ], "detail" : { Payload from DynamodbEvent.DynamodbStreamRecord.class ..
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class PublicationDynamodbEvent {


    private final String version;
    private final String id;
    private final PublicationDoiRequestDetailType detailType;
    private final String source;
    private final String account;
    private final String time;
    private final String region;
    private final List<String> resources;
    private final DynamodbEvent.DynamodbStreamRecord detail;
    @JsonIgnore
    private final PublicationChangeSet publicationChangeSet;

    @JsonCreator
    public PublicationDynamodbEvent(@JsonProperty("version") String version,
                                    @JsonProperty("id") String id,
                                    @JsonProperty("detail-type") PublicationDoiRequestDetailType detailType,
                                    @JsonProperty("source") String source,
                                    @JsonProperty("account") String account,
                                    @JsonProperty("time") String time,
                                    @JsonProperty("region") String region,
                                    @JsonProperty("resources") List<String> resources,
                                    @JsonProperty("detail") DynamodbStreamRecord detail) {
        this.version = version;
        this.id = id;
        this.detailType = detailType;
        this.source = source;
        this.account = account;
        this.time = time;
        this.region = region;
        this.resources = resources;
        this.detail = detail;
        this.publicationChangeSet = new PublicationChangeSet(detail);
    }

    public String getVersion() {
        return version;
    }

    public String getId() {
        return id;
    }

    public PublicationDoiRequestDetailType getDetailType() {
        return detailType;
    }

    public String getSource() {
        return source;
    }

    public String getAccount() {
        return account;
    }

    public String getTime() {
        return time;
    }

    public String getRegion() {
        return region;
    }

    public List<String> getResources() {
        return resources;
    }

    public DynamodbStreamRecord getDetail() {
        return detail;
    }


    /**
     * Checks if record is a new insert.
     *
     */
    public boolean isInsertEvent() {
        return detail.getEventName().equals("INSERT");
    }

    public boolean isModifyEvent() {
        return detail.getEventName().equals("MODIFY");
    }

    public boolean isRemoveEvent() {
        return detail.getEventName().equals("REMOVE");
    }

    public PublicationChangeSet getPublicationChangeset() {
        return publicationChangeSet;
    }

}
