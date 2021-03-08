package no.unit.nva.doi.example.commands.create;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.clients.models.Doi;
import no.unit.nva.doi.example.commands.BaseClientSetup;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.JacocoGenerated;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create", description = "Create DOI with metadata.")
@JacocoGenerated
public class Create extends BaseClientSetup implements Callable<Integer> {

    @Option(names = {"-m", "--metadata"}, paramLabel = "METADATA_FILE", description = "File with DataCite XML Metadata")
    protected File metadata;

    @Parameters(paramLabel = "DOI_PREFIX", description = "DOI prefix to create DOI under")
    protected String doiPrefix;

    @Override
    public Integer call() throws Exception {
        setupClient();

        // Validate input
        assert doiPrefix != null;
        assert !doiPrefix.isBlank();
        assert metadata != null;

        try {
            Doi doi = getClient().createDoi(customerId, readMetadata());
            System.out.println(doi.toIdentifier());
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
