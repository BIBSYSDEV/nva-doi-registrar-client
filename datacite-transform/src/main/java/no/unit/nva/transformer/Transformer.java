package no.unit.nva.transformer;

import jakarta.xml.bind.JAXB;
import java.io.StringWriter;
import java.util.stream.Collectors;

import no.unit.nva.transformer.dto.CreatorDto;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import org.datacide.schema.kernel_4.Resource;
import org.datacide.schema.kernel_4.Resource.Publisher;
import org.datacide.schema.kernel_4.Resource.Titles;
import org.datacide.schema.kernel_4.Resource.Titles.Title;

public class Transformer {

    private final Resource resource;
    /**
     * Transforms a DataCiteMetadataDto to a Datacite Record.
     * @param dataCiteMetadataDto A DataCiteMetadataDto instance.
     */
    public Transformer(DataCiteMetadataDto dataCiteMetadataDto)  {
        this.resource = new Resource();
        fromDataCiteMetadataDto(dataCiteMetadataDto);
    }

    private void fromDataCiteMetadataDto(DataCiteMetadataDto dataCiteMetadataDto) {
        setResourceIdentifier(dataCiteMetadataDto);
        setResourceCreators(dataCiteMetadataDto);
        setResourceTitle(dataCiteMetadataDto);
        setPublisher(dataCiteMetadataDto);
        setPublicationYear(dataCiteMetadataDto);
        setResourceType(dataCiteMetadataDto);
    }

    private void setResourceType(DataCiteMetadataDto dataCiteMetadataDto) {
        resource.setResourceType(dataCiteMetadataDto.getResourceType().toResourceType());
    }

    private void setPublicationYear(DataCiteMetadataDto dataCiteMetadataDto) {
        resource.setPublicationYear(dataCiteMetadataDto.getPublicationYear());
    }

    private void setPublisher(DataCiteMetadataDto dataCiteMetadataDto) {
        Publisher publisher = new Publisher();
        publisher.setValue(dataCiteMetadataDto.getPublisher().getValue());
        resource.setPublisher(publisher);
    }

    private void setResourceTitle(DataCiteMetadataDto dataCiteMetadataDto) {
        Title title = new Title();
        title.setValue(dataCiteMetadataDto.getTitle().getValue());
        Titles titles = new Titles();
        titles.getTitle().add(title);
        resource.setTitles(titles);
    }

    private void setResourceIdentifier(DataCiteMetadataDto dataCiteMetadataDto) {
        resource.setIdentifier(dataCiteMetadataDto.getIdentifier().asIdentifier());
    }

    private void setResourceCreators(DataCiteMetadataDto dataCiteMetadataDto) {
        var creators = new Resource.Creators();
        dataCiteMetadataDto.getCreator().stream()
                .map(CreatorDto::toCreator)
                .collect(Collectors.toList())
                .forEach(creator -> creators.getCreator().add(creator));
        resource.setCreators(creators);
    }

    /**
     * Produces an XML string representation of the Datacite record.
     * @return String XML.
     */
    public String asXml() {
        StringWriter stringWriter = new StringWriter();
        JAXB.marshal(resource, stringWriter);
        return stringWriter.toString();
    }
}
