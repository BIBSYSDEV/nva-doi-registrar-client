package no.unit.nva.doi.datacite.clients;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Objects;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.DeleteDraftDoiException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigExtractor;
import no.unit.nva.doi.models.Doi;
import nva.commons.core.paths.UriWrapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdsClient extends HttpSender {

    public static final int TIMEOUT = 2000;

    public static final String MISSING_DOI_IDENTIFIER_ARGUMENT =
        "Argument for parameter doi cannot be null!";
    public static final String MISSING_DATACITE_XML_ARGUMENT =
        "Argument for parameter dataCiteXml cannot be null!";
    public static final String APPLICATION_XML_CHARSET_UTF_8 =
        "application/xml; charset=UTF-8";
    public static final String DATACITE_PATH_METADATA = "metadata";
    public static final String MISSING_LANDING_PAGE_ARGUMENT =
        "Argument landingPage cannot be null!";
    public static final String DATACITE_PATH_DOI = "doi";
    public static final String LANDING_PAGE_BODY_FORMAT = "doi=%s\nurl=%s";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain;charset=UTF-8";


    private final Logger logger = LoggerFactory.getLogger(MdsClient.class);
    private final String dataciteMdsUri;
    private final CustomerConfigExtractor customerConfigExtractor;

    public MdsClient(String dataciteMdsUri,
                     CustomerConfigExtractor customerConfigExtractor,
                     HttpClient httpClient) {
        super(httpClient);
        this.dataciteMdsUri = dataciteMdsUri;
        this.customerConfigExtractor = customerConfigExtractor;
    }

    public void updateMetadata(URI customerId, Doi doi, String metadataDataCiteXml)
        throws ClientException {
        var customer = customerConfigExtractor.getCustomerConfig(customerId);
        validateUpdateMetadataInput(doi, metadataDataCiteXml);
        var request = createPutMetadataRequest(customer, doi, metadataDataCiteXml);
        sendRequest(request,  HttpStatus.SC_OK);
    }

    public void setLandingPage(URI customerId, Doi doi, URI landingPage) throws ClientException {
        var customer = customerConfigExtractor.getCustomerConfig(customerId);
        validateLandingPageInput(doi, landingPage);
        var request = createLandingPagePostRequest(customer, doi, landingPage);
        sendRequest(request,  HttpStatus.SC_CREATED);
    }

    public void deleteMedata(URI customerId, Doi doi) throws ClientException {
        var customer = customerConfigExtractor.getCustomerConfig(customerId);
        validateDeleteMetadataRequest(doi);
        var request = createDeleteMetadataRequest(customer, doi);
        sendRequest(request, HttpStatus.SC_OK);
    }

    public void deleteDraftDoi(URI customerId, Doi doi) throws ClientException {
        var customer = customerConfigExtractor.getCustomerConfig(customerId);
        validateDeleteDraftDoiRequest(doi);
        var request = createDeleteDraftDoiRequest(customer, doi);
        sendDeleteDraftRequest(request, doi);
    }

    private void sendDeleteDraftRequest(HttpRequest request, Doi doi) throws ClientException {
        var response = attempt(() -> super.getHttpClient().send(request, BodyHandlers.ofString()))
                           .orElseThrow(this::handleFailure);
        if (response.statusCode() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
            logger.error(REQUEST_RESPONDED_WITH_RESPONSE_MESSAGE + response.body());
            throw new DeleteDraftDoiException(doi, response.statusCode());
        }
        if (response.statusCode() != HttpStatus.SC_OK) {
            logger.error(REQUEST_RESPONDED_WITH_RESPONSE_MESSAGE + response.body());
            throw new ClientException(response.toString());
        }
    }

    private HttpRequest createDeleteMetadataRequest(CustomerConfig customer, Doi doi)
        throws CustomerConfigException {
        return HttpRequest.newBuilder()
                   .DELETE()
                   .uri(createUriForUpdatingMetadata(doi))
                   .header(AUTHORIZATION_HEADER, customer.extractBasicAuthenticationString())
                   .timeout(Duration.ofMillis(TIMEOUT))
                   .build();
    }

    private void validateDeleteMetadataRequest(Doi doi) {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
    }

    private HttpRequest createDeleteDraftDoiRequest(CustomerConfig customer, Doi doi)
        throws CustomerConfigException {
        return HttpRequest.newBuilder()
                   .DELETE()
                   .header(AUTHORIZATION_HEADER, customer.extractBasicAuthenticationString())
                   .uri(createUriForAccessingDoi(doi))
                   .timeout(Duration.ofMillis(TIMEOUT))
                   .build();
    }

    private void validateDeleteDraftDoiRequest(Doi doi) {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
    }

    private void validateUpdateMetadataInput(Doi doi, String metadataDataCiteXml) {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        Objects.requireNonNull(metadataDataCiteXml, MISSING_DATACITE_XML_ARGUMENT);
    }

    private HttpRequest createPutMetadataRequest(CustomerConfig customer,
                                                 Doi doi,
                                                 String metadataDataCiteXml)
        throws CustomerConfigException {
        return HttpRequest.newBuilder()
                   .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML_CHARSET_UTF_8)
                   .header(AUTHORIZATION_HEADER, customer.extractBasicAuthenticationString())
                   .uri(createUriForUpdatingMetadata(doi))
                   .timeout(Duration.ofMillis(TIMEOUT))
                   .POST(HttpRequest.BodyPublishers.ofString(metadataDataCiteXml))
                   .build();
    }

    private URI createUriForUpdatingMetadata(Doi doi) {
        return UriWrapper.fromHost(dataciteMdsUri)
                   .addChild(DATACITE_PATH_METADATA)
                   .addChild(doi.toIdentifier())
                   .getUri();
    }

    private HttpRequest createLandingPagePostRequest(CustomerConfig customer,
                                                     Doi doi,
                                                     URI landingPage)
        throws CustomerConfigException {
        return HttpRequest.newBuilder()
                   .header(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN_CHARSET_UTF_8)
                   .header(AUTHORIZATION_HEADER, customer.extractBasicAuthenticationString())
                   .timeout(Duration.ofMillis(TIMEOUT))
                   .uri(createUriForAccessingDoi(doi))
                   .PUT(HttpRequest.BodyPublishers.ofString(
                       createRequestBodyForRegisterUrl(doi, landingPage)))
                   .build();
    }

    private String createRequestBodyForRegisterUrl(Doi doi, URI landingPage) {
        return String.format(LANDING_PAGE_BODY_FORMAT, doi.toIdentifier(), landingPage.toString());
    }

    private URI createUriForAccessingDoi(Doi doi) {
        return UriWrapper.fromHost(dataciteMdsUri)
                   .addChild(DATACITE_PATH_DOI)
                   .addChild(doi.toIdentifier())
                   .getUri();
    }

    private void validateLandingPageInput(Doi doi, URI landingPage) {
        Objects.requireNonNull(doi, MISSING_DOI_IDENTIFIER_ARGUMENT);
        Objects.requireNonNull(landingPage, MISSING_LANDING_PAGE_ARGUMENT);
    }
}
