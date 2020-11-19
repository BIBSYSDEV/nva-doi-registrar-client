package no.unit.nva.doi.example.commands.delete;

import nva.commons.utils.JacocoGenerated;
import picocli.CommandLine.Command;

@Command(
    name = "delete", description = "Delete doi or metadata",
    subcommands = {
        DeleteDoi.class,
        DeleteMetadata.class,
    })
@JacocoGenerated
public class Delete {
}
