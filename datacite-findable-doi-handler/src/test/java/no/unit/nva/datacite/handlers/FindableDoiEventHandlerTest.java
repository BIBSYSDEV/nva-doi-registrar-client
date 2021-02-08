package no.unit.nva.datacite.handlers;

import static no.unit.nva.datacite.handlers.FindableDoiEventHandler.MANDATORY_FIELD_ERROR_PREFIX;
import static no.unit.nva.datacite.handlers.FindableDoiEventHandler.PUBLICATION_ID_FIELD_INFO;
import static no.unit.nva.datacite.handlers.FindableDoiEventHandler.PUBLICATION_INSTITUTION_OWNER_FIELD_INFO;
import static no.unit.nva.datacite.handlers.FindableDoiEventHandler.PUBLICATION_IS_MISSING_ERROR;
import static no.unit.nva.doi.LandingPageUtil.ABSOLUTE_RESOURCES_PATH;
import static no.unit.nva.doi.LandingPageUtil.LANDING_PAGE_UTIL;
import static no.unit.nva.doi.LandingPageUtil.URI_SCHEME;
import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.doi.models.ImmutableDoi;
import no.unit.nva.publication.doi.update.dto.DoiUpdateHolder;
import nva.commons.core.JsonUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.logutils.LogUtils;
import nva.commons.logutils.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FindableDoiEventHandlerTest {

    public static final String PUBLICATION_EVENT = "doi_publication_event.json";
    public static final String PUBLICATION_EVENT_INVALID_PUBLICATION_ID =
        "doi_publication_event_invalid_publication_id.json";
    public static final String SUCCESSFULLY_HANDLED_REQUEST_FOR_DOI = "Successfully handled request for Doi";
    public static final String NOT_PUBLISHED_PUBLICATION = "doi_publication_event_publication_not_published.json";

    public static final String PUBLICATION_WITH_WRONG_PUBLICATION_URI =
        "doi_publication_event_wrong_publication_uri.json";
    public static final String INVALID_SORTABLE_IDENTIFIER_ERROR_MESSAGE = "Invalid sortable identifier";

    private static final String EMPTY_FRAGMENT = null;
    private static final String RESOURCES_IDENTIFIER = "017781d2cecf-deeac454-fe20-4ef9-95e7-c993740c412b";
    private final DoiClient doiClient = mock(DoiClient.class);
    private final FindableDoiEventHandler findableDoiHandler = new FindableDoiEventHandler(doiClient);
    private ByteArrayOutputStream outputStream;
    private Context context;

    @BeforeEach
    public void init() {
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
    }

    @Test
    public void handleRequestReturnsDoiUpdateHolderOnSuccessWhenInputIsValid()
        throws ClientException, URISyntaxException {
        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT);
        findableDoiHandler.handleRequest(inputStream, outputStream, context);
        DoiUpdateHolder response = parseResponse();
        assertThat(response.getItem().getPublicationIdentifier(), is(not(nullValue())));
        assertThat(response.getItem().getModifiedDate(), is(notNullValue()));

        URI expectedCustomerId = URI.create(
            "https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934");
        verify(doiClient).updateMetadata(
            eq(expectedCustomerId),
            eq(createExpectedDoi()),
            verifyPartsOfMetadata()
        );
        verify(doiClient).setLandingPage(
            expectedCustomerId,
            createExpectedDoi(),
            constructResourceUri(response)
        );
    }

    @Test
    public void handleRequestSuccessfullyIsLogged() {
        TestAppender testingAppender = LogUtils.getTestingAppender(FindableDoiEventHandler.class);

        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT);
        findableDoiHandler.handleRequest(inputStream, outputStream, context);

        assertThat(testingAppender.getMessages(), containsString(SUCCESSFULLY_HANDLED_REQUEST_FOR_DOI));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingCustomerId() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_institution_owner.json");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), containsString(MANDATORY_FIELD_ERROR_PREFIX));
        assertThat(exception.getMessage(), containsString(PUBLICATION_INSTITUTION_OWNER_FIELD_INFO));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingItemInHolder() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_item.json");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(PUBLICATION_IS_MISSING_ERROR)));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingPublicationId() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_publication_id.json");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), containsString(MANDATORY_FIELD_ERROR_PREFIX));
        assertThat(exception.getMessage(), containsString(PUBLICATION_ID_FIELD_INFO));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnInvalidDoi() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_invalid_doi.json");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(FindableDoiEventHandler.DOI_IS_MISSING_OR_INVALID_ERROR)));
        assertThat(exception.getCause().getMessage(),
            is(equalTo(ImmutableDoi.CANNOT_BUILD_DOI_PROXY_IS_NOT_A_VALID_PROXY)));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnEmptyDoi() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_empty_doi.json");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(containsString(FindableDoiEventHandler.DOI_IS_MISSING_OR_INVALID_ERROR)));
    }

    @Test
    void handleRequestThrowsIllegalArgumentExceptionOnNonApprovedDoiRequestStatus() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            "doi_publication_event_wrong_doirequeststatus.json");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(FindableDoiEventHandler.DOI_REQUEST_STATUS_WRONG_ERROR)));
    }

    @Test
    void handleRequestThrowsExceptionWhenInputPublicationIdIsInvalid() {
        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT_INVALID_PUBLICATION_ID);

        var actualException = assertThrows(RuntimeException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));
        assertThat(actualException.getMessage(),
            containsString(INVALID_SORTABLE_IDENTIFIER_ERROR_MESSAGE));
    }

    @Test
    void handleRequestThrowsExceptionWhenInputPublicationIsNotPublished() {
        InputStream inputStream = IoUtils.inputStreamFromResources(NOT_PUBLISHED_PUBLICATION);

        IllegalStateException actualException = assertThrows(IllegalStateException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));
        assertThat(actualException.getMessage(),
            is(equalTo(FindableDoiEventHandler.CREATING_FINDABLE_DOI_FOR_DRAFT_PUBLICATION_ERROR)));
    }

    private URI constructResourceUri(DoiUpdateHolder response) {
        return LANDING_PAGE_UTIL.constructResourceUri(response.getItem().getPublicationIdentifier().toString());
    }

    private String verifyPartsOfMetadata() throws URISyntaxException {
        String expectedLandingPageUri = constructExpectedLandingPageUri();
        return and(
            contains("JournalArticle"),
            and(
                contains("<title>The resource title"),
                contains(
                    "identifierType=\"URL\">" + expectedLandingPageUri + "</identifier>")));
    }

    private String constructExpectedLandingPageUri() throws URISyntaxException {
        String expectedLandingPageUriPath = ABSOLUTE_RESOURCES_PATH + RESOURCES_IDENTIFIER;
        return new URI(URI_SCHEME, LANDING_PAGE_UTIL.getResourcesHost(), expectedLandingPageUriPath, EMPTY_FRAGMENT)
            .toString();
    }

    private Doi createExpectedDoi() {
        return Doi.builder()
            .withPrefix("10.1000")
            .withSuffix("182")
            .build();
    }

    private DoiUpdateHolder parseResponse() {
        return attempt(() -> JsonUtils.objectMapper.readValue(outputStream.toString(), DoiUpdateHolder.class))
            .orElseThrow();
    }
}
