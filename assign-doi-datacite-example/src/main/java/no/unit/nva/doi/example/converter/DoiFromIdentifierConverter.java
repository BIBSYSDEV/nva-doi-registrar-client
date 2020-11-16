package no.unit.nva.doi.example.converter;

import no.unit.nva.doi.datacite.clients.models.Doi;
import nva.commons.utils.JacocoGenerated;
import picocli.CommandLine.ITypeConverter;

@JacocoGenerated
public class DoiFromIdentifierConverter implements ITypeConverter<Doi> {

    @Override
    public Doi convert(String value) throws Exception {
        return Doi.builder().identifier(value).build();
    }
}
