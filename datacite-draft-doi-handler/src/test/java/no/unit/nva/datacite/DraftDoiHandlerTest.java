package no.unit.nva.datacite;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import no.unit.nva.datacite.model.DoiUpdateDto;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Contributor;
import no.unit.nva.publication.doi.dto.DoiRequest;
import no.unit.nva.publication.doi.dto.DoiRequestStatus;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.Publication.Builder;
import no.unit.nva.publication.doi.dto.PublicationDate;
import no.unit.nva.publication.doi.dto.PublicationStatus;
import no.unit.nva.publication.doi.dto.PublicationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DraftDoiHandlerTest {

    private static final String EXAMPLE_ID = "https://example.net/nva/publicationIdentifier";
    private static final String EXAMPLE_DOI_ID = "http://doi.org/11541.2/124530";
    private static final String EXAMPLE_INSTITUTION_OWNER = "???";
    private static final String EXAMPLE_TITLE = "The Matrix";
    private static final String EXAMPLE_CONTRIBUTOR_ID = "https://example.net/contributor/id/4000";
    private static final String EXAMPLE_CONTRIBUTOR_NAME = "Brinx";
    private static final String EXAMPLE_CONTRIBUTOR_ARPID = "989114";

    private Context context;
    private AwsEventBridgeEvent<Publication> event;

    @BeforeEach
    public void setUp() {
        context = Mockito.mock(Context.class);
        event = Mockito.mock(AwsEventBridgeEvent.class);
    }

    @Test
    public void draftDoiHandlerReturnsDoiUpdateDtoOnValidPublicationDto() {
        DraftDoiHandler handler = new DraftDoiHandler();

        Publication publication = createBuilderWithAllFieldsSet().build();

        DoiUpdateDto doiUpdateDto = handler.processInput(publication, event, context);

        assertNotNull(doiUpdateDto.getDoi());
    }

    private Builder createBuilderWithAllFieldsSet() {
        Instant now = Instant.now();
        return Builder.newBuilder()
            .withId(URI.create(EXAMPLE_ID))
            .withDoi(URI.create(EXAMPLE_DOI_ID))
            .withInstitutionOwner(URI.create(EXAMPLE_INSTITUTION_OWNER))
            .withPublicationDate(new PublicationDate("1999", "07", "09"))
            .withType(PublicationType.BOOK_ANTHOLOGY)
            .withMainTitle(EXAMPLE_TITLE)
            .withStatus(PublicationStatus.DRAFT)
            .withDoiRequest(new DoiRequest(DoiRequestStatus.APPROVED, now))
            .withModifiedDate(now)
            .withContributor(List.of(new Contributor.Builder()
                .withId(URI.create(EXAMPLE_CONTRIBUTOR_ID))
                .withArpId(EXAMPLE_CONTRIBUTOR_ARPID)
                .withName(EXAMPLE_CONTRIBUTOR_NAME)
                .build()));
    }
}
