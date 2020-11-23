package no.unit.nva.datacite.handlers;

import static no.unit.nva.datacite.handlers.LandingPageUtil.getLandingPage;
import static nva.commons.utils.attempt.Try.attempt;
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
import java.nio.file.Path;
import no.unit.nva.doi.DoiClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.models.Doi;
import no.unit.nva.doi.models.ImmutableDoi;
import no.unit.nva.publication.doi.update.dto.DoiUpdateHolder;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.log.LogUtils;
import nva.commons.utils.log.TestAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FindableDoiEventHandlerTest {

    public static final Path PUBLICATION_EVENT = Path.of("doi_request_event.json");
    public static final Path PUBLICATION_EVENT_INVALID_PUBLICATION_ID = Path.of(
        "doi_request_event_invalid_publication_id.json");

    private static final String DEMO_PREFIX = "10.5072";
    public static final String SUCCESSFULLY_HANDLED_REQUEST_FOR_DOI = "Successfully handled request for Doi";
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
        throws ClientException {
        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT);
        findableDoiHandler.handleRequest(inputStream, outputStream, context);
        DoiUpdateHolder response = parseResponse();
        assertThat(response.getItem().getPublicationId(), is(not(nullValue())));
        assertThat(response.getItem().getModifiedDate(), is(notNullValue()));

        URI expectedCustomerId = URI.create(
            "https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934");
        verify(doiClient).updateMetadata(eq(expectedCustomerId), eq(createExpectedDoi()), verifyPartsOfMetadata());
        verify(doiClient).setLandingPage(expectedCustomerId, createExpectedDoi(),
            getLandingPage(response.getItem().getPublicationId()));
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
            Path.of("doi_publication_event_empty_institution_owner.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(FindableDoiEventHandler.CUSTOMER_ID_IS_MISSING_ERROR)));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingItemInHolder() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_item.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(FindableDoiEventHandler.PUBLICATION_IS_MISSING_ERROR)));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnMissingPublicationId() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_request_event_empty_publication_id.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(FindableDoiEventHandler.PUBLICATION_ID_MISSING_ERROR)));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnInvalidDoi() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_invalid_doi.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(FindableDoiEventHandler.DOI_IS_MISSING_OR_INVALID_ERROR)));
        assertThat(exception.getCause().getMessage(),
            is(equalTo(ImmutableDoi.CANNOT_BUILD_DOI_PROXY_IS_NOT_A_VALID_PROXY)));
    }

    @Test
    public void handleRequestThrowsIllegalArgumentExceptionOnEmptyDoi() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_doi.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));

        assertThat(exception.getMessage(), is(equalTo(FindableDoiEventHandler.DOI_IS_MISSING_OR_INVALID_ERROR)));
        assertThat(exception.getCause().getMessage(),
            is(equalTo(FindableDoiEventHandler.DOI_IS_MISSING_OR_INVALID_ERROR)));
    }

    @Test
    void handleRequestThrowsExceptionWhenInputPublicationIdIsInvalid() {
        InputStream inputStream = IoUtils.inputStreamFromResources(PUBLICATION_EVENT_INVALID_PUBLICATION_ID);

        var actualException = assertThrows(IllegalArgumentException.class,
            () -> findableDoiHandler.handleRequest(inputStream, outputStream, context));
        assertThat(actualException.getMessage(),
            is(equalTo(LandingPageUtil.ERROR_PUBLICATION_LANDING_PAGE_COULD_NOT_BE_CONSTRUCTED)));
    }

    private String verifyPartsOfMetadata() {
        return and(
            contains("JOURNAL_ARTICLE"),
            and(
                contains("<title>Conformality loss"),
                contains(
                    "identifierType=\"URL\">https://example.net/unittest/namespace/publication/654321</identifier>")));
    }

    private Doi createExpectedDoi() {
        return Doi.builder()
            .withPrefix("10.1103")
            .withSuffix("physrevd.100.085005")
            .build();
    }

    private DoiUpdateHolder parseResponse() {
        return attempt(() -> JsonUtils.objectMapper.readValue(outputStream.toString(), DoiUpdateHolder.class))
            .orElseThrow();
    }
}
