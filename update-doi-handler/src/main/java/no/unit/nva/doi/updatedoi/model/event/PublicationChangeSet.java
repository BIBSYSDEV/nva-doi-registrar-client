package no.unit.nva.doi.updatedoi.model.event;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;

/**
 * Should be replaced by a Publication business object which eats a DynamodbDTO which again eats a DynamodbStreamRecord.
 */
public class PublicationChangeSet {

    public static final String STATUS = "status";
    public static final String EMPTY_STRING = "";
    public static final String PUBLICATION_STATUS_DRAFT = "Draft";
    private static final String PUBLICATION_STATUS_PUBLISHED = "Published";
    private static final String PUBLICATION_DOISTATUS_ASSIGNED = "ASSIGNED"; // ?? public enum DoiRequestStatus { REQUESTED, APPROVED, REJECTED; } for Publication. Where is ASSIGNED set?
    private static final String PUBLICATION_DOISTATUS_REQUESTED = "REQUESTED";
    private final StreamRecord record;
    private final String eventName;

    public PublicationChangeSet(DynamodbStreamRecord detail) {
        this.eventName = detail.getEventName();
        this.record = detail.getDynamodb();
    }

    public String getNewStatus() {
        return record.getNewImage().get(STATUS).getS();
    }

    public String getOldStatus() {
        return record.getOldImage().get(STATUS).getS();
    }


    public boolean isDraft() {
        return getNewStatus().equals(PUBLICATION_STATUS_DRAFT);
    }

    public boolean wasDraft() {
        return getOldStatus().equals(PUBLICATION_STATUS_DRAFT);
    }

    public boolean isPublishedState() {
        return record.getNewImage().get(STATUS).getS().equals(PUBLICATION_STATUS_PUBLISHED);
    }

    public boolean isDoiRequestedStatusRequested() {
        return record.getNewImage().get(STATUS).getS().equals(PUBLICATION_DOISTATUS_REQUESTED);
    }

    public boolean isDoiRequestedStatusAssigned() {
        return record.getNewImage().get(STATUS).getS().equals(PUBLICATION_DOISTATUS_ASSIGNED);
    }

    public boolean wasDoiRequestedStatusAssigned() {
        return record.getOldImage().get(STATUS).getS().equals(PUBLICATION_DOISTATUS_ASSIGNED);
    }

    //public PublicationDoiRequestDetailType
}
