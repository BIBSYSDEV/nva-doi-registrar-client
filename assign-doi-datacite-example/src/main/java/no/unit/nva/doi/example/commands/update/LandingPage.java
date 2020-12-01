package no.unit.nva.doi.example.commands.update;

import java.net.URI;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.ClientRuntimeException;
import no.unit.nva.doi.datacite.clients.models.Doi;
import no.unit.nva.doi.example.commands.BaseClientSetup;
import no.unit.nva.doi.example.converter.DoiFromIdentifierConverter;
import nva.commons.utils.JacocoGenerated;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "landingpage", description = "Set Landing Page (URL) for DOI")
@JacocoGenerated
public class LandingPage extends BaseClientSetup {

    @Parameters(paramLabel = "DOI_IDENTIFIER",
        description = "prefix/suffix",
        converter = DoiFromIdentifierConverter.class)
    protected Doi doi;

    @Option(names = {"-l", "--landing-page"}, paramLabel = "URL", description = "URL of Landing Page")
    protected URI landingPageUrl;

    @Override
    public Integer call() throws Exception {
        setupClient();
        // Validate input
        assert doi != null;
        assert doi.toIdentifier() != null;
        assert landingPageUrl != null;

        System.out.println(landingPageUrl);
        try {

            getClient().setLandingPage(customerId, doi, landingPageUrl);
            return SUCCESSFUL_EXIT;
        } catch (ClientException | ClientRuntimeException e) {
            e.printStackTrace();
            return UNSUCCESSFUL_EXIT;
        }
    }
}
