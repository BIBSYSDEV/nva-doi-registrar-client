package no.unit.nva.doi;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import no.unit.nva.publication.doi.dto.Contributor;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationDate;
import no.unit.nva.publication.doi.dto.PublicationType;
import no.unit.nva.transformer.dto.CreatorDto;
import no.unit.nva.transformer.dto.DataCiteMetadataDto;
import no.unit.nva.transformer.dto.IdentifierDto;
import no.unit.nva.transformer.dto.PublisherDto;
import no.unit.nva.transformer.dto.ResourceTypeDto;
import no.unit.nva.transformer.dto.TitleDto;

public final class DataCiteMetadataDtoMapper {

    private DataCiteMetadataDtoMapper() {
    }

    /**
     * Maps a Publication to DataCiteMetadataDto. For use in the nva doi partner data Transformer.
     *
     * @param publication   publication
     * @return  dynamoRecordDto
     */
    public static DataCiteMetadataDto fromPublication(Publication publication) {
        return new DataCiteMetadataDto.Builder()
            .withCreator(toCreatorDtoList(publication.getContributor()))
            .withIdentifier(toIdentifierDto(publication.getId()))
            .withPublicationYear(toPublicationYear(publication.getPublicationDate()))
            .withPublisher(toPublisherDto(publication.getInstitutionOwner()))
            .withTitle(toTitleDto(publication.getMainTitle()))
            .withResourceType(toResourceTypeDto(publication.getType()))
            .build();
    }

    private static String toPublicationYear(PublicationDate publicationDate) {
        if (publicationDate == null) {
            return null;
        }
        return publicationDate.getYear();
    }

    private static ResourceTypeDto toResourceTypeDto(PublicationType value) {
        if (value == null) {
            return null;
        }
        return new ResourceTypeDto.Builder()
            .withValue(value.name())
            .build();
    }

    private static TitleDto toTitleDto(String value) {
        if (value == null) {
            return null;
        }
        return new TitleDto.Builder()
            .withValue(value)
            .build();
    }

    private static PublisherDto toPublisherDto(URI value) {
        if (value == null) {
            return null;
        }
        return new PublisherDto.Builder()
            .withValue(value.toString())
            .build();
    }

    private static IdentifierDto toIdentifierDto(URI value) {
        if (value == null) {
            return null;
        }
        return new IdentifierDto.Builder()
            .withValue(value.toString())
            .build();
    }

    private static List<CreatorDto> toCreatorDtoList(List<Contributor> contributors) {
        if (contributors == null) {
            return null;
        }
        return contributors.stream()
            .map(DataCiteMetadataDtoMapper::toCreatorDto)
            .collect(Collectors.toList());
    }

    private static CreatorDto toCreatorDto(Contributor contributor) {
        return new CreatorDto.Builder()
            .withCreatorName(contributor.getName())
            .build();
    }

}
