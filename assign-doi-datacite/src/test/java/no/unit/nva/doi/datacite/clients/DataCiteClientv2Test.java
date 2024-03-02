package no.unit.nva.doi.datacite.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.unit.nva.doi.datacite.clients.DataCiteRestApiClient.CONTENT_TYPE;
import static no.unit.nva.doi.datacite.clients.DataCiteRestApiClient.JSON_API_CONTENT_TYPE;
import static no.unit.nva.doi.datacite.clients.MdsClient.APPLICATION_XML_CHARSET_UTF_8;
import static no.unit.nva.doi.datacite.clients.MdsClient.LANDING_PAGE_BODY_FORMAT;
import static no.unit.nva.doi.datacite.clients.MdsClient.MISSING_DATACITE_XML_ARGUMENT;
import static no.unit.nva.doi.datacite.clients.MdsClient.MISSING_DOI_IDENTIFIER_ARGUMENT;
import static no.unit.nva.doi.datacite.clients.MdsClient.MISSING_LANDING_PAGE_ARGUMENT;
import static no.unit.nva.doi.datacite.clients.MdsClient.TEXT_PLAIN_CHARSET_UTF_8;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.clients.exception.DeleteDraftDoiException;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfig;
import no.unit.nva.doi.datacite.customerconfigs.CustomerConfigException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.datacite.restclient.models.DraftDoiDto;
import no.unit.nva.doi.datacite.utils.FakeCustomerExtractor;
import no.unit.nva.doi.datacite.utils.FakeCustomerExtractorThrowingException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.useragent.UserAgent;
import nva.commons.logutils.LogUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@WireMockTest
public class DataCiteClientv2Test {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_VND_API_JSON = "application/vnd.api+json";
    private static final String CUSTOMER_PASSWORD = "somePassword";
    private static final String CUSTOMER_USERNAME = "someUsername";
    private static final String DOIS_PATH_PREFIX = "/dois";
    private static final String DOI_PREFIX = "10.1234";
    private static final String EXAMPLE_DOI_SUFFIX = "1942810412-sadsfgffds";
    private static final String PASSWORD_NOT_SET = null;
    private static final String USERNAME_NOT_SET = null;
    private static final String DOI_PREFIX_NOT_SET = null;
    private static final String DOI_HOST = "example.doi.host.org";
    private static final String GET_DOI_RESPONSE_JSON = "getDoiResponse.json";
    private static final String EXAMPLE_DOI_FROM_FILE = "10.23/456789";
    private static final String DRAFT = "draft";
    private static final char FORWARD_SLASH = '/';
    private static final String HTTP_RESPONSE_OK = "OK";

    private static final String metadataPathPrefix = FORWARD_SLASH + MdsClient.DATACITE_PATH_METADATA;
    private static final String doiPath = FORWARD_SLASH + MdsClient.DATACITE_PATH_DOI;

    private static final URI EXAMPLE_LANDING_PAGE = URI.create("https://example.net/nva/publication/203124124");
    private static final String EXPECTED_USER_AGENT_REST = "DataCiteRestApiClient-api.localhost.nva.aws.unit.no/1.0 "
            + "(https://github.com/BIBSYSDEV/nva-doi-registrar-client; mailto:support@sikt.no)";
    private static final String EXPECTED_USER_AGENT_MDS = "MdsClient-api.localhost.nva.aws.unit.no/1.0 "
            + "(https://github.com/BIBSYSDEV/nva-doi-registrar-client; mailto:support@sikt.no)";
    private DataCiteClientV2 client;

    private FakeCustomerExtractor customerConfigExtractor;

    static Stream<Named<CustomerConfig>> providedBadCustomerConfigs() {
        return Stream.of(Named.of("No customer details", new CustomerConfig(randomUri(),
                                                         PASSWORD_NOT_SET,
                                                         USERNAME_NOT_SET,
                                                         DOI_PREFIX_NOT_SET)),
                         Named.of("No username or DOI prefix", new CustomerConfig(randomUri(),
                                                         randomString(),
                                                         USERNAME_NOT_SET,
                                                         DOI_PREFIX_NOT_SET)),
                         Named.of("No password or DOI prefix", new CustomerConfig(randomUri(),
                                                         PASSWORD_NOT_SET,
                                                         randomString(),
                                                         DOI_PREFIX_NOT_SET
                         )),
                         Named.of("No password or username", new CustomerConfig(randomUri(),
                                                         PASSWORD_NOT_SET,
                                                         USERNAME_NOT_SET,
                                                         randomString())));
    }

    private Doi createDoiWithDemoPrefixAndExampleSuffix() {
        return createDoi("example.doi.host.org", DOI_PREFIX, EXAMPLE_DOI_SUFFIX);
    }

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {
        this.customerConfigExtractor = new FakeCustomerExtractor();
        this.client = new DataCiteClientV2(
            customerConfigExtractor,
            WiremockHttpClient.create(),
            runtimeInfo.getHttpBaseUrl(),
            runtimeInfo.getHttpBaseUrl(),
            DOI_HOST
        );
    }

    @Test
    void shouldThrowExceptionWhenCreatingDraftDoiForCustomerThatDoesNotExist(
        WireMockRuntimeInfo runtimeInfo) {
        client = new DataCiteClientV2(
            new FakeCustomerExtractorThrowingException(),
            WiremockHttpClient.create(),
            runtimeInfo.getHttpBaseUrl(),
            runtimeInfo.getHttpBaseUrl(),
            DOI_HOST
        );
        var customerUri = randomUri();
        assertThrows(CustomerConfigException.class,
                     () -> client.createDoi(customerUri));
    }

    @ParameterizedTest()
    @DisplayName("Should throw exception when creating DOI for customer that is not configured")
    @MethodSource("providedBadCustomerConfigs")
    void shouldThrowExceptionWhenCreatingDoiForCustomerThatIsNotConfigured(CustomerConfig customer) {
        customerConfigExtractor.setCustomerConfig(customer);
        var exception = assertThrows(CustomerConfigException.class,
                                     () -> client.createDoi(customer.getCustomerId()));
        assertThat(exception.getMessage(), containsString(customer.getCustomerId().toString()));
    }

    @Test
    void shouldThrowClientExceptionWhenHttpClientThrowsIoException(WireMockRuntimeInfo runtimeInfo)
        throws IOException,
               InterruptedException {
        var httpClientMock = mock(HttpClient.class);
        var exceptionMessage = "Something horrible happened";
        when(httpClientMock.send(any(), any())).thenThrow(new IOException(exceptionMessage));
        client = new DataCiteClientV2(customerConfigExtractor, httpClientMock,
                                      runtimeInfo.getHttpBaseUrl(),
                                      runtimeInfo.getHttpBaseUrl(),
                                      DOI_HOST);
        var customerId = createValidCustomer();
        var exception = assertThrows(ClientException.class, () -> client.createDoi(customerId));
        assertThat(exception.getMessage(), containsString(exceptionMessage));
    }

    @Test
    void shouldThrowDoiClientExceptionWhenDataciteRespondsWithException() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var customerUri = createValidCustomer();
        var responseBody = "someResponseBody";
        stubHttpClientException(responseBody);
        var exception = assertThrows(ClientException.class, () -> client.createDoi(customerUri));
        assertThat(exception.getMessage(), containsString("403"));
        assertThat(logAppender.getMessages(), containsString(responseBody));
    }

    @Test
    void shouldReturnDoiWhenSuccessfullyCreatedPostDoiRequest() throws ClientException {
        var customerUri = createValidCustomer();
        var randomSuffix = UUID.randomUUID().toString();
        var draftDoiDto = DraftDoiDto.create(DOI_PREFIX, randomSuffix);
        stubSuccessfulResponse(draftDoiDto);
        var actual = client.createDoi(customerUri);
        verify(1,
               postRequestedFor(urlEqualTo("/dois"))
                   .withBasicAuth(new BasicCredentials(CUSTOMER_USERNAME, CUSTOMER_PASSWORD))
                   .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo(JSON_API_CONTENT_TYPE))
                   .withHeader(UserAgent.USER_AGENT, WireMock.equalTo(EXPECTED_USER_AGENT_REST)));
        assertThat(actual, is(instanceOf(Doi.class)));
        var expectedCreatedServerDoi = createDoi("example.doi.host.org", DOI_PREFIX, randomSuffix);
        assertThat(actual.toIdentifier(), is(equalTo(expectedCreatedServerDoi.toIdentifier())));
        assertThat(actual.getUri(), is(equalTo(expectedCreatedServerDoi.getUri())));
    }

    @Test
    void shouldReturnDoiWhenClientRespondsWithSuccess() throws ClientException {
        var customerUri = createValidCustomer();
        String getDoiResponseJson = IoUtils.stringFromResources(Path.of(GET_DOI_RESPONSE_JSON));
        var requestedDoi = Doi.fromDoiIdentifier(EXAMPLE_DOI_FROM_FILE);
        stubGetDoiResponse(getDoiResponseJson, requestedDoi);

        DoiStateDto actual = client.getDoi(customerUri, requestedDoi);
        assertThat(actual, is(instanceOf(DoiStateDto.class)));
        assertThat(actual.getDoi(), is(equalTo(requestedDoi.toIdentifier())));
        assertThat(actual.getState(), is(equalTo(DRAFT)));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingDoiWithoutSendingDoi() {
        var customerUri = createValidCustomer();
        Doi notADoi = null;
        var xml = randomString();
        var exception = assertThrows(NullPointerException.class,
                                     () -> client.updateMetadata(customerUri, notADoi, xml));
        assertThat(exception.getMessage(), containsString(MISSING_DOI_IDENTIFIER_ARGUMENT));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingDoiWithoutSendingXml() {
        var customerUri = createValidCustomer();
        var requestedDoi = Doi.fromDoiIdentifier(EXAMPLE_DOI_FROM_FILE);
        String notValidXml = null;
        var exception = assertThrows(NullPointerException.class,
                                     () -> client.updateMetadata(customerUri, requestedDoi, notValidXml));
        assertThat(exception.getMessage(), containsString(MISSING_DATACITE_XML_ARGUMENT));
    }

    @Test
    void shouldUpdateMetadataSuccessfullyWhenSupplyingCorrectInputAndClientIsWorking() throws ClientException {
        var customerId = createValidCustomer();
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForUpdatingMetadata = createMetadataDoiIdentifierPath(doi);
        stubUpdateMetadataResponse(expectedPathForUpdatingMetadata);
        client.updateMetadata(customerId, doi, getValidMetadataPayload());
        verifyUpdateMetadataResponse(expectedPathForUpdatingMetadata);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAttemptingTosSetLandingPageToNull() {
        var customerId = createValidCustomer();
        var requestedDoi = Doi.fromDoiIdentifier(EXAMPLE_DOI_FROM_FILE);
        URI notValidLandingPage = null;
        var exception = assertThrows(NullPointerException.class,
                                     () -> client.setLandingPage(customerId, requestedDoi, notValidLandingPage));
        assertThat(exception.getMessage(), containsString(MISSING_LANDING_PAGE_ARGUMENT));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAttemptingToSetLandingPageWithoutDoi() {
        var customerId = createValidCustomer();
        var landingPage = randomUri();
        Doi notValidDoi = null;
        var exception = assertThrows(NullPointerException.class,
                                     () -> client.setLandingPage(customerId, notValidDoi, landingPage));
        assertThat(exception.getMessage(), containsString(MISSING_DOI_IDENTIFIER_ARGUMENT));
    }

    @Test
    void shouldSetLandingPageWhenInputParametersAreValidAndHttpClientReturnsOk() throws ClientException {
        var customerId = createValidCustomer();
        var doi = createDoiWithDemoPrefixAndExampleSuffix();

        stubSetLandingPageResponse(doi);

        client.setLandingPage(customerId, doi, EXAMPLE_LANDING_PAGE);

        verifySetLandingResponse(doi);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAttemptingToDeleteMetadataForEmptyDoi() {
        var customerId = createValidCustomer();
        Doi notValidDoi = null;
        var exception = assertThrows(NullPointerException.class,
                                     () -> client.deleteMetadata(customerId, notValidDoi));
        assertThat(exception.getMessage(), containsString(MISSING_DOI_IDENTIFIER_ARGUMENT));
    }

    @Test
    void shouldDeleteMetadataWhenInputParametersIsValidAndHttpClientReturnsOk() throws ClientException {
        var customerId = createValidCustomer();
        var doi = createDoiWithDemoPrefixAndExampleSuffix();
        var expectedPathForDeletingMetadata = createMetadataDoiIdentifierPath(doi);
        stubDeleteMetadataResponse(expectedPathForDeletingMetadata);

        client.deleteMetadata(customerId, doi);

        verifyDeleteMetadataResponse(expectedPathForDeletingMetadata);
    }

    @Test
    void shouldGetMetadata() throws ClientException {
        var customerId = createValidCustomer();
        var doi = createDoiWithDemoPrefixAndExampleSuffix();
        var expectedPathForGetMetadata = createMetadataDoiIdentifierPath(doi);
        stubGetMetadataResponse(expectedPathForGetMetadata);

        var metadata = client.getMetadata(customerId, doi);
        assertThat(metadata, containsString("http://datacite.org/schema/kernel-4"));
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAttemptingToDeleteDraftDoiWithoutSupplyingADoi() {
        var customerId = createValidCustomer();
        Doi notValidDoi = null;
        var exception = assertThrows(NullPointerException.class,
                                     () -> client.deleteDraftDoi(customerId, notValidDoi));
        assertThat(exception.getMessage(), containsString(MISSING_DOI_IDENTIFIER_ARGUMENT));
    }

    @Test
    void shouldDeleteDraftDoiWhenInputParametersAreValidAndHttpClientReturnsOk() throws ClientException {
        var customerId = createValidCustomer();
        var doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingDoiInDraftStatus = createDoiIdentifierPath(doi);
        stubDeleteDraftApiResponse(expectedPathForDeletingDoiInDraftStatus);

        client.deleteDraftDoi(customerId, doi);

        verifyDeleteDoiResponse(expectedPathForDeletingDoiInDraftStatus);
    }

    @Test
    void shouldThrowExceptionIfDeleteDraftDoiRequestRespondsWithErrorCode() {
        var customerId = createValidCustomer();
        var doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingDoiInDraftStatus = createDoiIdentifierPath(doi);
        studDeleteDraftApiResponseThatFails(expectedPathForDeletingDoiInDraftStatus);
        assertThrows(ClientException.class,
                     () -> client.deleteDraftDoi(customerId, doi));
    }

    @Test
    void deleteDraftDoiForCustomerWhereDoiIsFindableThrowsApiExceptionAsClientException() {
        var customerId = createValidCustomer();
        Doi doi = createDoiWithDemoPrefixAndExampleSuffix();
        String expectedPathForDeletingDoiInDraftStatus = createDoiIdentifierPath(doi);
        stubDeleteDraftApiResponseForFindableDoi(expectedPathForDeletingDoiInDraftStatus);

        var actualException = assertThrows(DeleteDraftDoiException.class,
                                           () -> client.deleteDraftDoi(customerId, doi));
        assertThat(actualException, isA(ClientException.class));
        assertThat(actualException.getMessage(),
                   containsString(doi.toIdentifier()));
        assertThat(actualException.getMessage(),
                   containsString(String.valueOf(HttpStatus.SC_METHOD_NOT_ALLOWED)));
    }

    private void studDeleteDraftApiResponseThatFails(String expectedPathForDeletingDoiInDraftStatus) {
        stubFor(delete(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.SC_UNAUTHORIZED)));
    }

    private void stubDeleteDraftApiResponseForFindableDoi(String expectedPathForDeletingDoiInDraftStatus) {
        stubFor(delete(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.SC_METHOD_NOT_ALLOWED)));
    }

    private void verifyDeleteDoiResponse(String expectedPathForDeletingDoiInDraftStatus) {
        verify(deleteRequestedFor(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
                   .withBasicAuth(getExpectedAuthenticatedCredentials()));
    }

    private void stubDeleteDraftApiResponse(String expectedPathForDeletingDoiInDraftStatus) {
        stubFor(delete(urlEqualTo(expectedPathForDeletingDoiInDraftStatus))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(HTTP_RESPONSE_OK)));
    }

    private void verifyDeleteMetadataResponse(String expectedPathForDeletingMetadata) {
        verify(deleteRequestedFor(urlEqualTo(expectedPathForDeletingMetadata))
                   .withBasicAuth(getExpectedAuthenticatedCredentials()));
    }

    private void stubDeleteMetadataResponse(String expectedPathForDeletingMetadata) {
        stubFor(delete(urlEqualTo(expectedPathForDeletingMetadata))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.SC_OK)
                                    .withBody(HTTP_RESPONSE_OK)));
    }

    private void stubGetMetadataResponse(String expectedPathGetMetadata) {
        stubFor(get(urlEqualTo(expectedPathGetMetadata))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.SC_OK)
                                    .withBody(getValidMetadataPayload())
                                    .withHeader(CONTENT_TYPE,
                                        APPLICATION_XML_CHARSET_UTF_8)));
    }

    private void verifySetLandingResponse(Doi requestedDoi) {
        verify(putRequestedFor(urlEqualTo(createDoiIdentifierPath(requestedDoi)))
                   .withBasicAuth(getExpectedAuthenticatedCredentials())
                   .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(TEXT_PLAIN_CHARSET_UTF_8))
                   .withRequestBody(WireMock.equalTo(String.format(
                       LANDING_PAGE_BODY_FORMAT,
                       requestedDoi.toIdentifier(), EXAMPLE_LANDING_PAGE)))
                   .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo(TEXT_PLAIN_CHARSET_UTF_8)));
    }

    private void stubSetLandingPageResponse(Doi requestedDoi) {
        stubFor(put(urlEqualTo(createDoiIdentifierPath(requestedDoi)))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withHeader(HEADER_CONTENT_TYPE, TEXT_PLAIN_CHARSET_UTF_8)
                                    .withStatus(HttpStatus.SC_OK).withBody(HTTP_RESPONSE_OK)));
    }

    private String createDoiIdentifierPath(Doi requestedDoi) {
        return doiPath + FORWARD_SLASH + requestedDoi.toIdentifier();
    }

    private void verifyUpdateMetadataResponse(String expectedPath) {
        verify(postRequestedFor(urlEqualTo(expectedPath))
                   .withBasicAuth(getExpectedAuthenticatedCredentials())
                   .withRequestBody(WireMock.equalTo(getValidMetadataPayload()))
                   .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo(APPLICATION_XML_CHARSET_UTF_8))
                   .withHeader(UserAgent.USER_AGENT, WireMock.equalTo(EXPECTED_USER_AGENT_MDS)));
    }

    private BasicCredentials getExpectedAuthenticatedCredentials() {
        return new BasicCredentials(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);
    }

    private String getValidMetadataPayload() {
        return IoUtils.stringFromResources(Path.of("dataciteXmlResourceExample.xml"));
    }

    private void stubUpdateMetadataResponse(String expectedPathForUpdatingMetadata) {
        stubFor(post(urlEqualTo(expectedPathForUpdatingMetadata))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.SC_OK)
                                    .withBody(HTTP_RESPONSE_OK)));
    }

    private String createMetadataDoiIdentifierPath(Doi doi) {
        return metadataPathPrefix + FORWARD_SLASH + doi.toIdentifier();
    }

    private void stubGetDoiResponse(String getDoiResponseJson, Doi requestedDoi) {
        stubFor(get(urlEqualTo(createDoisIdentifierPath(requestedDoi)))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withHeader(CONTENT_TYPE, APPLICATION_VND_API_JSON)
                                    .withStatus(HttpStatus.SC_OK)
                                    .withBody(getDoiResponseJson)));
    }

    private String createDoisIdentifierPath(Doi requestedDoi) {
        return DOIS_PATH_PREFIX + FORWARD_SLASH + requestedDoi.toIdentifier();
    }

    private Doi createDoi(String host, String prefix, String suffix) {
        return Doi.fromPrefixAndSuffix(host, prefix, suffix);
    }

    private void stubSuccessfulResponse(DraftDoiDto draftDoiDto) {
        stubFor(post(urlEqualTo(DOIS_PATH_PREFIX))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.SC_CREATED)
                                    .withBody(draftDoiDto.toJson())));
    }

    private void stubHttpClientException(String expectedBody) {
        stubFor(post(urlEqualTo(DOIS_PATH_PREFIX))
                    .withBasicAuth(CUSTOMER_USERNAME, CUSTOMER_PASSWORD)
                    .willReturn(aResponse()
                                    .withStatus(HttpURLConnection.HTTP_FORBIDDEN)
                                    .withBody(expectedBody)));
    }

    private URI createValidCustomer() {
        var customerId = randomUri();
        var customerConfig = new CustomerConfig(customerId,
                                                CUSTOMER_PASSWORD,
                                                CUSTOMER_USERNAME,
                                                DOI_PREFIX);
        customerConfigExtractor.setCustomerConfig(customerConfig);
        return customerId;
    }
}
