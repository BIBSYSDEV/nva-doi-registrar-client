package no.unit.nva.doi;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import no.unit.nva.doi.model.DoiUpdateDto;
import no.unit.nva.events.models.AwsEventBridgeDetail;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.publication.doi.dto.Publication;
import no.unit.nva.publication.doi.dto.PublicationDtoTestDataGenerator;
import no.unit.nva.publication.doi.dto.PublicationHolder;
import nva.commons.utils.IoUtils;
import nva.commons.utils.JsonUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DraftDoiHandlerTest {

    public static final String DOI_PUBLICATION = "doi.publication";
    public static final String ERROR_MESSAGE = "error";

    private DraftDoiHandler handler;
    private ByteArrayOutputStream outputStream;
    private Context context;
    private AwsEventBridgeEvent<AwsEventBridgeDetail<PublicationHolder>> event;

    @BeforeEach
    public void setUp() {
        handler = new DraftDoiHandler();
        outputStream = new ByteArrayOutputStream();
        context = mock(Context.class);
        event = mock(AwsEventBridgeEvent.class);
    }


    @Test
    public void handleRequestReturnsDoiUpdateDtoWithPublicationUriWhenInputIsValid() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_valid.json"));
        handler.handleRequest(inputStream,outputStream,context);
        DoiUpdateDto response = parseResponse();
        assertThat(response.getPublicationId(), is(not(nullValue())));
    }

    private DoiUpdateDto parseResponse() {
        return attempt(() -> JsonUtils.objectMapper.readValue(outputStream.toString(), DoiUpdateDto.class))
            .orElseThrow();
    }

    @Test
    public void handleRequestThrowsExceptionOnMissingEventItem() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_item.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));

        MatcherAssert.assertThat(exception.getMessage(), is(DraftDoiHandler.PUBLICATION_IS_MISSING_ERROR));
    }

    @Test
    public void handleRequestThrowsExceptionOnMissingCustomerId() {
        InputStream inputStream = IoUtils.inputStreamFromResources(
            Path.of("doi_publication_event_empty_institution_owner.json"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> handler.handleRequest(inputStream, outputStream, context));

        MatcherAssert.assertThat(exception.getMessage(), is(DraftDoiHandler.CUSTOMER_ID_IS_MISSING_ERROR));
    }

    @Test
    public void draftDoiHandlerReturnsDoiUpdateDtoOnValidPublicationDto() {
        Publication publication = PublicationDtoTestDataGenerator.createPublication();
        PublicationHolder publicationHolder = new PublicationHolder(DOI_PUBLICATION, publication);

        DoiUpdateDto doiUpdateDto = handler.processInputPayload(publicationHolder, event, context);

        assertThat(doiUpdateDto, doesNotHaveNullOrEmptyFields());
    }

    @Test
    public void draftDoiHandlerThrowsExceptionOnInvalidPublicationDto() {
        PublicationHolder publicationHolder = getFailingPublicationHolder();

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> handler.processInputPayload(publicationHolder, event, context)
        );

        assertThat(exception.getMessage(), equalTo(ERROR_MESSAGE));
    }

    private PublicationHolder getFailingPublicationHolder() {
        PublicationHolder publicationHolder = mock(PublicationHolder.class);
        when(publicationHolder.getItem()).thenThrow(new RuntimeException(ERROR_MESSAGE));
        return publicationHolder;
    }
}
