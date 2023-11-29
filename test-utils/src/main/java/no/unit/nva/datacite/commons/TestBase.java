package no.unit.nva.datacite.commons;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static nva.commons.core.attempt.Try.attempt;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestBase {

    public static final String PUBLICATION_PATH = "/publication/";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String APPLICATION_VND_DATACITE_DATACITE_XML = "application/vnd.datacite.datacite+xml";
    private String baseUrl;

    protected void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected void mockDataciteXmlBody(String publicationIdentifier, String body) {
        stubFor(WireMock.get(urlPathEqualTo(PUBLICATION_PATH + publicationIdentifier))
                    .withHeader(ACCEPT_HEADER, WireMock.equalTo(APPLICATION_VND_DATACITE_DATACITE_XML))
                    .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_OK).withBody(body)));
    }

    protected void mockDataciteXmlGone(String publicationIdentifier) {
        stubFor(WireMock.get(urlPathEqualTo(PUBLICATION_PATH + publicationIdentifier))
                    .withHeader(ACCEPT_HEADER, WireMock.equalTo(APPLICATION_VND_DATACITE_DATACITE_XML))
                    .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_GONE)));
    }

    protected void mockDataciteXmlError(String publicationIdentifier) {
        stubFor(WireMock.get(urlPathEqualTo(PUBLICATION_PATH + publicationIdentifier))
                    .withHeader(ACCEPT_HEADER, WireMock.equalTo(APPLICATION_VND_DATACITE_DATACITE_XML))
                    .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_BAD_REQUEST)));
    }

    protected String createPublicationId(String publicationIdentifier) {
        return baseUrl + PUBLICATION_PATH + publicationIdentifier;
    }

    protected InputStream createDoiRequestInputStream(String publicationIdentifier, URI doi, URI customerId) {
        var doiUpdateRequestEvent = createDoiUpdateRequest(publicationIdentifier, doi, customerId);
        var awsEventBridgeEvent = crateAwsEventBridgeEvent(doiUpdateRequestEvent);
        return toInputStream(awsEventBridgeEvent);
    }

    protected InputStream toInputStream(AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> request) {
        return attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(request)).map(IoUtils::stringToStream)
                   .orElseThrow();
    }

    protected static AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>> crateAwsEventBridgeEvent(
        DoiUpdateRequestEvent doiUpdateRequestEvent) {
        var request = new AwsEventBridgeEvent<AwsEventBridgeDetail<DoiUpdateRequestEvent>>();
        var awsEventBridgeDetail = new AwsEventBridgeDetail<DoiUpdateRequestEvent>();
        awsEventBridgeDetail.setResponsePayload(doiUpdateRequestEvent);
        request.setDetail(awsEventBridgeDetail);
        return request;
    }

    protected DoiUpdateRequestEvent createDoiUpdateRequest(String publicationID, URI doi, URI customerId) {
        return new DoiUpdateRequestEvent("PublicationService.Doi.UpdateRequest",
                                         doi,
                                         UriWrapper.fromUri(createPublicationId(publicationID)).getUri(),
                                         customerId);
    }
}
