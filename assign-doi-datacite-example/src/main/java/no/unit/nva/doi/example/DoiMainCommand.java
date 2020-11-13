package no.unit.nva.doi.example;

import no.unit.nva.doi.example.commands.delete.Delete;
import no.unit.nva.doi.example.commands.create.Create;
import no.unit.nva.doi.example.commands.update.LandingPage;
import no.unit.nva.doi.example.commands.update.UpdateMetadata;
import nva.commons.utils.JacocoGenerated;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(subcommands = {
    Create.class,
    UpdateMetadata.class,
    LandingPage.class,
    Delete.class,
})
@JacocoGenerated
public class DoiMainCommand {

    public static void main(String[] args) {
        new CommandLine(new DoiMainCommand()).execute(args);
    }
}
