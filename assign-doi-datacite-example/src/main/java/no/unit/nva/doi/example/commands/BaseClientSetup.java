package no.unit.nva.doi.example.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.DoiClientFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteConfigurationFactory;
import no.unit.nva.doi.datacite.connectionfactories.DataCiteMdsConfigValidationFailedException;
import no.unit.nva.doi.datacite.connectionfactories.PasswordAuthenticationFactory;
import no.unit.nva.doi.datacite.mdsclient.DataCiteMdsConnectionFactory;
import nva.commons.core.JacocoGenerated;
import picocli.CommandLine.Option;

@JacocoGenerated
public abstract class BaseClientSetup implements Callable<Integer> {

    public static final String MDS_TEST_DATACITE_ORG_FROM_RUNNING_ENVIRONMENT = "mds.test.datacite.org";
    public static final int MDS_PORT_FROM_RUNNING_ENVIRONMENT = 443;
    protected static final int SUCCESSFUL_EXIT = 0;
    protected static final Integer UNSUCCESSFUL_EXIT = 1;

    private DoiClient client;

    @Option(names = {"--config"}, paramLabel = "CONFIG_FILE", description = "MDS configuration file")
    protected File config;

    @Option(names = {"--customer"}, paramLabel = "CUSTOMER_ID", description = "NVA customer id")
    protected URI customerId;


    @Override
    public abstract Integer call() throws Exception;

    protected void setupClient() throws IOException, DataCiteMdsConfigValidationFailedException {
        assert config != null;
        assert customerId != null;
        var configFactory = createConfigFactory();
        configFactory.getConfig(customerId); // Throws exception on errors.
        var mdsConnectionFactory = createMdsConnectionFactory(configFactory);
        client = DoiClientFactory.getClient(configFactory, mdsConnectionFactory);
    }

    public DoiClient getClient() {
        return client;
    }

    private DataCiteConfigurationFactory createConfigFactory() throws IOException {
        InputStream jsonConfig = Files.newInputStream(config.toPath(), StandardOpenOption.READ);
        return new DataCiteConfigurationFactory(jsonConfig);
    }

    private DataCiteMdsConnectionFactory createMdsConnectionFactory(DataCiteConfigurationFactory configFactory) {
        PasswordAuthenticationFactory authenticationFactory = new PasswordAuthenticationFactory(configFactory);
        return new DataCiteMdsConnectionFactory(authenticationFactory,
            MDS_TEST_DATACITE_ORG_FROM_RUNNING_ENVIRONMENT,
            MDS_PORT_FROM_RUNNING_ENVIRONMENT);
    }
}
