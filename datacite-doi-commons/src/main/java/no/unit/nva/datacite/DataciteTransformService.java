package no.unit.nva.datacite;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import no.unit.nva.publication.doi.dto.Contributor;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationType;
import no.unit.nva.transformer.Transformer;
import no.unit.nva.transformer.dto.CreatorDto;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import no.unit.nva.transformer.dto.IdentifierDto;
import no.unit.nva.transformer.dto.PublisherDto;
import no.unit.nva.transformer.dto.ResourceTypeDto;
import no.unit.nva.transformer.dto.TitleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataciteTransformService implements TransformService {

    private static final Logger logger = LoggerFactory.getLogger(DataciteTransformService.class);

    @Override
    public String getXml(Publication publication) {
        DataCiteMetadataDto dataCiteMetadataDto = fromPublication(publication);
        try {
            Transformer transformer = new Transformer(dataCiteMetadataDto);
            return transformer.asXml();
        } catch (JAXBException e) {
            logger.error("Error transforming publication ({}) to datacite XML", publication.getId(), e);
            throw new RuntimeException(e);
        }
    }

    private DataCiteMetadataDto fromPublication(Publication input) {
        return new DataCiteMetadataDto.Builder()
            .withCreator(toCreatorDtoList(input.getContributor()))
            .withIdentifier(toIdentifierDto(input.getId()))
            .withPublicationYear(input.getPublicationDate().getYear())
            .withPublisher(toPublisherDto(input.getInstitutionOwner()))
            .withTitle(toTitleDto(input.getMainTitle()))
            .withResourceType(toResourceTypeDto(input.getType()))
            .build();
    }

    private ResourceTypeDto toResourceTypeDto(PublicationType value) {
        return new ResourceTypeDto.Builder()
            .withValue(value.name())
            .build();
    }

    private TitleDto toTitleDto(String value) {
        return new TitleDto.Builder()
            .withValue(value)
            .build();
    }

    private PublisherDto toPublisherDto(URI value) {
        return new PublisherDto.Builder()
            .withValue(value.toString())
            .build();
    }

    private IdentifierDto toIdentifierDto(URI value) {
        return new IdentifierDto.Builder()
            .withValue(value.toString())
            .build();
    }

    private List<CreatorDto> toCreatorDtoList(List<Contributor> contributors) {
        return contributors.stream()
            .map(this::toCreatorDto)
            .collect(Collectors.toList());
    }

    private CreatorDto toCreatorDto(Contributor contributor) {
        return new CreatorDto.Builder()
            .withCreatorName(contributor.getName())
            .build();
    }

}
