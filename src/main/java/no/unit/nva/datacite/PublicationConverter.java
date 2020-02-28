package no.unit.nva.datacite;

import no.unit.nva.datacite.model.generated.Contributor;
import no.unit.nva.datacite.model.generated.Creator;
import no.unit.nva.datacite.model.generated.Date;
import no.unit.nva.datacite.model.generated.Description;
import no.unit.nva.datacite.model.generated.NameIdentifierORCID;
import no.unit.nva.datacite.model.generated.Resource;
import no.unit.nva.datacite.model.generated.ResourceType;
import no.unit.nva.datacite.model.generated.Rights;
import no.unit.nva.datacite.model.generated.Subject;
import no.unit.nva.datacite.model.generated.Title;
import no.unit.nva.model.Publication;


public class PublicationConverter {

    public Resource toResource(Publication publication) {

        Resource resource = new Resource()
                .creators(
                        new Creator()
                                .creatorName("Miller, Elizabeth")
                                .givenName("Elizabeth")
                                .familyName("Miller")
                                .nameIdentifier(new NameIdentifierORCID("0000-0001-5000-0007"))
                                .affiliation("DataCite")
                )
                .titles(
                        new Title("en-us", null, "Full DataCite XML Example"),
                        new Title("en-us", "Subtitle", "Demonstration of DataCite Properties.")
                )
                .publisher("DataCite")
                .publicationYear("2014")
                .subjects(new Subject("en-us", "000 computer science"))
                .contributors(
                        new Contributor()
                                .contributorType("ProjectLeader")
                                .contributorName("Starr, Joan")
                                .nameIdentifier(new NameIdentifierORCID("0000-0002-7285-027X"))
                                .affiliation("California Digital Library")
                )
                .dates(new Date("Updated", "2014-10-17"))
                .language("en-us")
                .resourceType(new ResourceType("XML", "Software"))
                .rightsList(
                        new Rights()
                                .rights(publication.getLicense().getIdentifier())
                                .rightsURI(publication.getLicense().getLink().toString())
                )
                .descriptions(
                        new Description()
                                .lang("en-us")
                                .descriptionType("Abstract")
                                .description("")
                );
        return resource;
    }

}
