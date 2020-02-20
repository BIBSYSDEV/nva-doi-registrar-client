package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JacksonXmlRootElement(localName = "resource")
public class Resource {

    @JacksonXmlProperty(localName = "xmlns:xsi", isAttribute = true)
    private String xmlns_xsi = "http://www.w3.org/2001/XMLSchema-instance";
    @JacksonXmlProperty(isAttribute = true)
    private String xmlns = "http://datacite.org/schema/kernel-4";
    @JacksonXmlProperty(localName = "xsi:schemaLocation", isAttribute = true)
    private String xsi_schemaLocation = "http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4/metadata.xsd";
    private Identifier identifier;
    private List<Creator> creators;
    private List<Title> titles;
    private String publisher;
    private String publicationYear;
    private List<Subject> subjects;
    private List<Contributor> contributors;
    private List<Date> dates;
    private String language;
    private ResourceType resourceType;
    private List<AlternateIdentifier> alternateIdentifiers;
    private List<RelatedIdentifier> relatedIdentifiers;
    private List<Size> sizes;
    private List<Format> formats;
    private String version;
    private List<Rights> rightsList;
    private List<Description> descriptions;
    private List<GeoLocation> geoLocations;
    private List<FundingReference> fundingReferences;


    public Identifier getIdentifier() {
        return identifier;
    }

    public Resource identifier(Identifier identifier) {
        this.identifier = identifier;
        return this;
    }

    @JacksonXmlProperty(localName = "creator")
    @JacksonXmlElementWrapper(localName = "creators")
    public List<Creator> getCreators() {
        if (creators == null) {
            creators = new ArrayList<>();
        }
        return creators;
    }

    public Resource creators(Creator creator, Creator... creators) {
        if (creator != null) {
            getCreators().add(creator);
        }
        if (creators != null) {
            getCreators().addAll(Arrays.asList(creators));
        }
        return this;
    }

    public Resource creators(List<Creator> creators) {
        this.creators = creators;
        return this;
    }

    @JacksonXmlProperty(localName = "title")
    @JacksonXmlElementWrapper(localName = "titles")
    public List<Title> getTitles() {
        if (titles == null) {
            titles = new ArrayList<>();
        }
        return titles;
    }

    public Resource titles(Title title, Title... titles) {
        if (title != null) {
            getTitles().add(title);
        }
        if (titles != null) {
            getTitles().addAll(Arrays.asList(titles));
        }
        return this;
    }

    public Resource titles(List<Title> titles) {
        this.titles = titles;
        return this;
    }

    public String getPublisher() {
        return publisher;
    }

    public Resource publisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public Resource publicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
        return this;
    }

    public Resource subjects(Subject subject, Subject... subjects) {
        if (subject != null) {
            getSubjects().add(subject);
        }
        if (subjects != null) {
            getSubjects().addAll(Arrays.asList(subjects));
        }
        return this;
    }

    @JacksonXmlProperty(localName = "subject")
    @JacksonXmlElementWrapper(localName = "subjects")
    public List<Subject> getSubjects() {
        if (subjects == null) {
            subjects = new ArrayList<>();
        }
        return subjects;
    }

    public Resource subjects(List<Subject> subjects) {
        this.subjects = subjects;
        return this;
    }

    public Resource contributors(Contributor contributor, Contributor... contributors) {
        if (contributor != null) {
            getContributors().add(contributor);
        }
        if (contributors != null) {
            getContributors().addAll(Arrays.asList(contributors));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "contributors")
    @JacksonXmlProperty(localName = "contributor")
    public List<Contributor> getContributors() {
        if (contributors == null) {
            contributors = new ArrayList<>();
        }
        return contributors;
    }

    public Resource contributors(List<Contributor> contributors) {
        this.contributors = contributors;
        return this;
    }

    public Resource dates(Date date, Date... dates) {
        if (date != null) {
            getDates().add(date);
        }
        if (dates != null) {
            getDates().addAll(Arrays.asList(dates));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "dates")
    @JacksonXmlProperty(localName = "date")
    public List<Date> getDates() {
        if (dates == null) {
            dates = new ArrayList<>();
        }
        return dates;
    }

    public Resource dates(List<Date> dates) {
        this.dates = dates;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public Resource language(String language) {
        this.language = language;
        return this;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public Resource resourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public Resource alternateIdentifiers(AlternateIdentifier alternateIdentifier, AlternateIdentifier... alternateIdentifiers) {
        if (alternateIdentifier != null) {
            getAlternateIdentifiers().add(alternateIdentifier);
        }
        if (alternateIdentifiers != null) {
            getAlternateIdentifiers().addAll(Arrays.asList(alternateIdentifiers));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "alternateIdentifiers")
    @JacksonXmlProperty(localName = "alternateIdentifier")
    public List<AlternateIdentifier> getAlternateIdentifiers() {
        if (alternateIdentifiers == null) {
            alternateIdentifiers = new ArrayList<>();
        }
        return alternateIdentifiers;
    }

    public Resource alternateIdentifiers(List<AlternateIdentifier> alternateIdentifiers) {
        this.alternateIdentifiers = alternateIdentifiers;
        return this;
    }

    @JacksonXmlElementWrapper(localName = "relatedIdentifiers")
    @JacksonXmlProperty(localName = "relatedIdentifier")
    public List<RelatedIdentifier> getRelatedIdentifiers() {
        if (relatedIdentifiers == null) {
            relatedIdentifiers = new ArrayList<>();
        }
        return relatedIdentifiers;
    }

    public Resource relatedIdentifiers(List<RelatedIdentifier> relatedIdentifiers) {
        this.relatedIdentifiers = relatedIdentifiers;
        return this;
    }

    public Resource relatedIdentifiers(RelatedIdentifier relatedIdentifier, RelatedIdentifier... relatedIdentifiers) {
        if (relatedIdentifier != null) {
            getRelatedIdentifiers().add(relatedIdentifier);
        }
        if (relatedIdentifiers != null) {
            getRelatedIdentifiers().addAll(Arrays.asList(relatedIdentifiers));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "sizes")
    @JacksonXmlProperty(localName = "size")
    public List<Size> getSizes() {
        if (sizes == null) {
            sizes = new ArrayList<>();
        }
        return sizes;
    }

    public Resource sizes(List<Size> sizes) {
        this.sizes = sizes;
        return this;
    }

    public Resource sizes(Size size, Size... sizes) {
        if (size != null) {
            getSizes().add(size);
        }
        if (sizes != null) {
            getSizes().addAll(Arrays.asList(sizes));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "formats")
    @JacksonXmlProperty(localName = "format")
    public List<Format> getFormats() {
        if (formats == null) {
            formats = new ArrayList<>();
        }
        return formats;
    }

    public Resource formats(List<Format> formats) {
        this.formats = formats;
        return this;
    }

    public Resource formats(Format format, Format... formats) {
        if (format != null) {
            getFormats().add(format);
        }
        if (formats != null) {
            getFormats().addAll(Arrays.asList(formats));
        }
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Resource version(String version) {
        this.version = version;
        return this;
    }

    @JacksonXmlElementWrapper(localName = "rightsList")
    @JacksonXmlProperty(localName = "rights")
    public List<Rights> getRightsList() {
        if (rightsList == null) {
            rightsList = new ArrayList<>();
        }
        return rightsList;
    }

    public Resource rightsList(List<Rights> rightsList) {
        this.rightsList = rightsList;
        return this;
    }

    public Resource rightsList(Rights rights, Rights... rightses) {
        if (rights != null) {
            getRightsList().add(rights);
        }
        if (rightses != null) {
            getRightsList().addAll(Arrays.asList(rightses));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "descriptions")
    @JacksonXmlProperty(localName = "description")
    public List<Description> getDescriptions() {
        if (descriptions == null) {
            descriptions = new ArrayList<>();
        }
        return descriptions;
    }

    public Resource descriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
        return this;
    }

    public Resource descriptions(Description description, Description... descriptions) {
        if (description != null) {
            getDescriptions().add(description);
        }
        if (descriptions != null) {
            getDescriptions().addAll(Arrays.asList(descriptions));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "geoLocations")
    @JacksonXmlProperty(localName = "geoLocation")
    public List<GeoLocation> getGeoLocations() {
        if (geoLocations == null) {
            geoLocations = new ArrayList<>();
        }
        return geoLocations;
    }

    public Resource geoLocations(List<GeoLocation> geoLocations) {
        this.geoLocations = geoLocations;
        return this;
    }

    public Resource geoLocations(GeoLocation geoLocation, GeoLocation... geoLocations) {
        if (geoLocation != null) {
            getGeoLocations().add((geoLocation));
        }
        if (geoLocations != null) {
            getGeoLocations().addAll(Arrays.asList(geoLocations));
        }
        return this;
    }

    @JacksonXmlElementWrapper(localName = "fundingReferences")
    @JacksonXmlProperty(localName = "fundingReference")
    public List<FundingReference> getFundingReferences() {
        if (fundingReferences == null) {
            fundingReferences = new ArrayList<>();
        }
        return fundingReferences;
    }

    public Resource fundingReferences(List<FundingReference> fundingReferences) {
        this.fundingReferences = fundingReferences;
        return this;
    }

    public Resource fundingReferences(FundingReference fundingReference, FundingReference... fundingReferences) {
        if (fundingReference != null) {
            getFundingReferences().add(fundingReference);
        }
        if (fundingReferences != null) {
            getFundingReferences().addAll(Arrays.asList(fundingReferences));
        }
        return this;
    }
}
