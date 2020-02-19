package no.unit.nva.datacite.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ResourceTest {

    @Test
    public void testResourceToXml() throws JsonProcessingException {
        Resource resource =
                new Resource()
                        .identifier(new Identifier().identifier("10.18711/zaq1xsw2"))
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
                        .alternateIdentifiers(
                                new AlternateIdentifier()
                                        .alternateIdentifier("http://schema.datacite.org/schema/meta/kernel-3.1/example/datacite-example-full-v3.1.xml")
                                        .alternateIdentifierType("URL")
                        )
                        .relatedIdentifiers(
                                new RelatedIdentifier("http://data.datacite.org/application/citeproc+json/10.5072/example-full")
                                        .relatedIdentifierType("URL")
                                        .relationType("HasMetadata")
                                        .relatedMetadataScheme("citeproc+json")
                                        .schemeURI("https://github.com/citation-style-language/schema/raw/master/csl-data.json")
                        )
                        .sizes(new Size("3KB"))
                        .formats(new Format("application/xml"))
                        .version("3.1")
                        .rightsList(
                                new Rights()
                                        .rights("CC0 1.0 Universal")
                                        .rightsURI("http://creativecommons.org/publicdomain/zero/1.0/")
                        )
                        .descriptions(
                                new Description()
                                        .lang("en-us")
                                        .descriptionType("Abstract")
                                        .description("XML example of all DataCite Metadata Schema v4.0 properties.")
                        )
                        .geoLocations(
                                new GeoLocation("Atlantic Ocean")
                                        .geoLocationPoint(
                                                new GeoLocationPoint("-67.302", "31.233")
                                        )
                                        .geoLocationBox(
                                                new GeoLocationBox()
                                                        .westBoundLongitude("-71.032")
                                                        .eastBoundLongitude("-68.211")
                                                        .southBoundLatitude("41.090")
                                                        .northBoundLatitude("42.893")

                                        )
                        )
                        .fundingReferences(
                                new FundingReference("European Commission")
                                        .funderIdentifier(
                                                new FunderIdentifier("http://doi.org/10.13039/501100000780")
                                                        .funderIdentifierType("Crossref Funder ID")
                                        )
                        );
        assertNotNull(new XmlMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .writeValueAsString(resource));
    }

}