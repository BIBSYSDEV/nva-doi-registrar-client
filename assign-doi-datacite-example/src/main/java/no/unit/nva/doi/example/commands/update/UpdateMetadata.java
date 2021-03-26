package no.unit.nva.doi.example.commands.update;

import java.io.File;
import java.util.Optional;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.clients.models.Doi;
import no.unit.nva.doi.example.commands.BaseClientSetup;
import no.unit.nva.doi.example.converter.DoiFromIdentifierConverter;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.JacocoGenerated;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "update", description = "Update metadata for DOI")
@JacocoGenerated
public class UpdateMetadata extends BaseClientSetup {

    @Parameters(paramLabel = "DOI_IDENTIFIER",
        description = "prefix/suffix",
        converter = DoiFromIdentifierConverter.class)
    protected Doi doi;

    @Option(names = {"-m", "--metadata"}, paramLabel = "METADATA_FILE", description = "File with DataCite XML Metadata")
    protected File metadata;

    @Override
    public Integer call() throws Exception {
        setupClient();

        // Validate input
        assert doi != null;
        assert doi.toIdentifier() != null;
        assert metadata != null;

        try {
            getClient().updateMetadata(customerId, doi, readMetadata());
            return SUCCESSFUL_EXIT;
        } catch (ClientException | ClientRuntimeException e) {
            e.printStackTrace();
            return UNSUCCESSFUL_EXIT;
        }
    }

    private String readMetadata() {
        return Optional.of(metadata)
            .map(m -> IoUtils.stringFromFile(m.toPath())).orElseThrow();
    }
}
