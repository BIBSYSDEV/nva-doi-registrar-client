package no.unit.nva.datacite;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import no.unit.nva.publication.doi.dto.Contributor;
import no.unit.nva.publication.doi.dto.DoiRequest;
import no.unit.nva.publication.doi.dto.DoiRequestStatus;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.Publication.Builder;
import no.unit.nva.publication.doi.dto.PublicationDate;
import no.unit.nva.publication.doi.dto.PublicationStatus;
import no.unit.nva.publication.doi.dto.PublicationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataciteTransformServiceTest {

    private static final String EXAMPLE_ID = "https://example.net/nva/publicationIdentifier";
    private static final String EXAMPLE_DOI_ID = "http://doi.org/11541.2/124530";
    private static final String EXAMPLE_INSTITUTION_OWNER = "???";
    private static final String EXAMPLE_TITLE = "The Matrix";
    private static final String EXAMPLE_CONTRIBUTOR_ID = "https://example.net/contributor/id/4000";
    private static final String EXAMPLE_CONTRIBUTOR_NAME = "Brinx";
    private static final String EXAMPLE_CONTRIBUTOR_ARPID = "989114";
    public static final Publication INVALID_PUBLICATION = null;

    @Test
    public void dataciteTransformerServiceReturnsXmlStringFromPublicationDto() {
        DataciteTransformService service = new DataciteTransformService();
        Publication publication = createBuilderWithAllFieldsSet().build();

        String xml = service.getXml(publication);

        Assertions.assertNotNull(xml);
    }


    @Test
    public void dataciteTransformerServiceThrowsRuntimeExceptionOnInvalidPublicationDto() {
        DataciteTransformService service = new DataciteTransformService();

        Assertions.assertThrows(RuntimeException.class, () -> service.getXml(INVALID_PUBLICATION));
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
