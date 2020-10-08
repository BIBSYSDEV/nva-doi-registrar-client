package no.unit.nva.doi.updatedoi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import no.unit.nva.doi.updatedoi.model.event.PublicationDynamodbEvent;
import nva.commons.utils.JsonUtils;

public class EventHandler implements RequestStreamHandler {

    private static final ObjectMapper objectMapper = JsonUtils.objectMapper;
    public static final String DOI_REQUEST_SOURCE = "doiRequest";

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        var event = objectMapper.readValue(input, PublicationDynamodbEvent.class);

        if (event.getSource().equals(DOI_REQUEST_SOURCE)) {
            var publicationChangeset = event.getPublicationChangeset();

            // TODO: map PublicationChangeSet a PublicationDynamomdbDTO ? Hm. Data jealousy between services?
            if (event.isInsertEvent() || event.isModifyEvent())  {
                if (publicationChangeset.isPublishedState() && publicationChangeset.isDoiRequestedStatusRequested()) {
                    // request Findable DOI
                } else if (publicationChangeset.isDraft() && publicationChangeset.isDoiRequestedStatusRequested()) {
                    // request Draft DOI
                } else if (publicationChangeset.isPublishedState() && publicationChangeset.wasDraft()) {
                    // migrate from Draft to Findable
                } else {
                    // log unknown.
                }
            }

            if (event.isRemoveEvent() && publicationChangeset.isDoiRequestedStatusAssigned()) {
                    // migrate DRAFT_DOI   OR   FINDABLE DOI to a REGISTERED DOI.  // (never delete data)
            }
            if (event.isModifyEvent() && !publicationChangeset.isDoiRequestedStatusAssigned() && publicationChangeset.wasDoiRequestedStatusAssigned()) {
                // migrate DRAFT_DOI   OR   FINDABLE DOI to a REGISTERED DOI.  // (never delete data)
            }

        } else {
            // bad request
        }
    }
}
