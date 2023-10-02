package no.unit.nva.doi.datacite.clients;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigExtractor;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.datacite.restclient.models.DraftDoiDto;
import no.unit.nva.doi.models.Doi;
import nva.commons.core.paths.UriWrapper;
import org.apache.http.HttpStatus;

public class DataCiteRestApiClient extends HttpSender {

    private  static final int TIMEOUT = 2000;

    public static final String ACCEPT = "Accept";
    public static final String JSON_API_CONTENT_TYPE = "application/vnd.api+json";
    public static final String CONTENT_TYPE = "Content-Type";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String DOIS_PATH_PARAMETER = "dois";
    private final URI dataciteRestApiURI;
    private final CustomerConfigExtractor customerConfigExtractor;

    private final String doiHost;

    public DataCiteRestApiClient(URI dataciteRestApiURI,
                                 String doiHost,
                                 CustomerConfigExtractor customerConfigExtractor,
                                 HttpClient httpClient) {
        super(httpClient);
        this.dataciteRestApiURI = dataciteRestApiURI;
        this.customerConfigExtractor = customerConfigExtractor;
        this.doiHost = doiHost;
    }

    public Doi createDoi(URI customerId) throws ClientException {
        var customer = customerConfigExtractor.getCustomerConfig(customerId);
        var request = createPostDoiRequest(customer);
        var response = sendRequest(request, HttpStatus.SC_CREATED);
        return convertResponseToDoi(response);
    }

    public DoiStateDto getDoi(URI customerId, Doi doi) throws ClientException {
        var customer = customerConfigExtractor.getCustomerConfig(customerId);
        var request = createGetDoiRequest(customer, doi);
        var response = sendRequest(request, HttpStatus.SC_OK);
        return DoiStateDto.fromJson(response.body());
    }

    private Doi convertResponseToDoi(HttpResponse<String> response) {
        DraftDoiDto responseBody = DraftDoiDto.fromJson(response.body());
        return responseBody.toDoi().changeHost(doiHost);
    }

    private HttpRequest createGetDoiRequest(CustomerConfig customer, Doi doi)
        throws CustomerConfigException {
        return HttpRequest.newBuilder()
                   .uri(requestTargetUriToDoi(doi))
                   .GET()
                   .header(ACCEPT, JSON_API_CONTENT_TYPE)
                   .timeout(Duration.ofMillis(TIMEOUT))
                   .headers(AUTHORIZATION_HEADER, customer.extractBasicAuthenticationString())
                   .build();
    }

    private URI requestTargetUriToDoi(Doi doi) {
        return attempt(() -> buildUriToDoi(doi)).orElseThrow();
    }

    private URI buildUriToDoi(Doi doi) {
        return UriWrapper.fromUri(dataciteRestApiURI)
                   .addChild(DOIS_PATH_PARAMETER)
                   .addChild(doi.toIdentifier())
                   .getUri();
    }

    private HttpRequest createPostDoiRequest(CustomerConfig customerConfig)
        throws CustomerConfigException {
        return HttpRequest.newBuilder()
                   .uri(doiRequestUri())
                   .header(CONTENT_TYPE, JSON_API_CONTENT_TYPE)
                   .POST(BodyPublishers.ofString(requestBodyContainingTheDoiPrefix(customerConfig)))
                   .headers(AUTHORIZATION_HEADER, customerConfig.extractBasicAuthenticationString())
                   .timeout(Duration.ofMillis(TIMEOUT))
                   .build();
    }

    private String requestBodyContainingTheDoiPrefix(CustomerConfig customerConfig) {
        DraftDoiDto bodyObject = DraftDoiDto.fromPrefix(customerConfig.getDoiPrefix());
        return bodyObject.toJson();
    }

    private URI doiRequestUri() {
        return UriWrapper.fromUri(dataciteRestApiURI).addChild(DOIS_PATH_PARAMETER).getUri();
    }
}
