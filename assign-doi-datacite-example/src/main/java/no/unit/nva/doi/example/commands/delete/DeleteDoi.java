package no.unit.nva.doi.example.commands.delete;

import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.clients.models.Doi;
import no.unit.nva.doi.example.commands.BaseClientSetup;
import no.unit.nva.doi.example.converter.DoiFromIdentifierConverter;
import nva.commons.utils.JacocoGenerated;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "doi", description = "Delete Draft DOI")
@JacocoGenerated
public class DeleteDoi extends BaseClientSetup {

    @Parameters(paramLabel = "DOI_IDENTIFIER",
        description = "prefix/suffix",
        converter = DoiFromIdentifierConverter.class)
    protected Doi doi;

    @Override
    public Integer call() throws Exception {
        setupClient();
        try {
            getClient().deleteDraftDoi(customerId, doi);
            return SUCCESSFUL_EXIT;
        } catch (ClientException | ClientRuntimeException e) {
            e.printStackTrace();
            return UNSUCCESSFUL_EXIT;
        }
    }
}
