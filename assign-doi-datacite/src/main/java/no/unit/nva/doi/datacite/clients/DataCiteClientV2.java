package no.unit.nva.doi.datacite.clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigExtractor;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigExtractorImpl;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.Doi;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class DataCiteClientV2 implements DoiClient {

    private final MdsClient mdsClient;
    private final DataCiteRestApiClient dataCiteRestApiClient;

    @JacocoGenerated
    public DataCiteClientV2() {
        this(new CustomerConfigExtractorImpl(new Environment().readEnv("CUSTOMER_SECRETS_SECRET_NAME"),
                                             new Environment().readEnv("CUSTOMER_SECRETS_SECRET_KEY")),
             HttpClient.newBuilder().version(Version.HTTP_2).build(),
             new Environment().readEnv("DATACITE_REST_HOST"),
             new Environment().readEnv("DATACITE_MDS_HOST"),
             new Environment().readEnv("DOI_HOST"));
    }

    public DataCiteClientV2(
        CustomerConfigExtractor customerConfigExtractor,
        HttpClient httpClient,
        String dataciteRestApiUri,
        String dataciteMdsUri,
        String doiHost) {
        this.mdsClient = new MdsClient(dataciteMdsUri,
                                       customerConfigExtractor,
                                       httpClient);
        this.dataCiteRestApiClient = new DataCiteRestApiClient(dataciteRestApiUri,
                                                               doiHost,
                                                               customerConfigExtractor,
                                                               httpClient);
    }

    @Override
    public Doi createDoi(URI customerId) throws ClientException {
        return dataCiteRestApiClient.createDoi(customerId);
    }

    @Override
    public void updateMetadata(URI customerId, Doi doi, String metadataDataCiteXml) throws ClientException {
        mdsClient.updateMetadata(customerId, doi, metadataDataCiteXml);
    }

    @Override
    public void setLandingPage(URI customerId, Doi doi, URI url) throws ClientException {
        mdsClient.setLandingPage(customerId, doi, url);
    }

    @Override
    public void deleteMetadata(URI customerId, Doi doi) throws ClientException {
        mdsClient.deleteMedata(customerId, doi);
    }

    @Override
    public void deleteDraftDoi(URI customerId, Doi doi) throws ClientException {
        mdsClient.deleteDraftDoi(customerId, doi);
    }

    @Override
    public DoiStateDto getDoi(URI customerId, Doi doi) throws ClientException {
        return dataCiteRestApiClient.getDoi(customerId, doi);
    }
}
