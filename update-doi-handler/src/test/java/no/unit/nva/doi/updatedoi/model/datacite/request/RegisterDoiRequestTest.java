package no.unit.nva.doi.updatedoi.model.datacite.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import no.unit.nva.doi.updatedoi.model.datacite.request.RegisterDoiRequest.Attributes;
import no.unit.nva.doi.updatedoi.model.datacite.request.RegisterDoiRequest.Builder;
import no.unit.nva.doi.updatedoi.model.datacite.request.RegisterDoiRequest.Data;
import org.junit.jupiter.api.Test;

class RegisterDoiRequestTest {

    private static final String EXAMPLE_ATTRIBUTES_DOI = "http://valid-doi-goes-here.example.net";
    private static final String REGISTER_DOI_EVENT_TYPE = "register";
    private static final String EXAMPLE_DATA_ID = "10.5438/0012";
    public static final String EXAMPLE_DATA_TYPE = "dois"; // TODO: Is this a constant from somewhere?

    @Test
    public void testAttributesBuilder() {
        var actual = RegisterDoiRequest.AttributesBuilder.newBuilder()
            .withDoi(EXAMPLE_ATTRIBUTES_DOI)
            .build();

        assertThat(actual.getDoi(), is(equalTo(EXAMPLE_ATTRIBUTES_DOI)));
        assertThat(actual.getEventType(), is(equalTo(REGISTER_DOI_EVENT_TYPE)));
    }
    
    @Test
    public void testRegisterDoiRequestBuilder() {
        var actual = Builder.newBuilder()
            .withId(EXAMPLE_DATA_ID)
            .withType(EXAMPLE_DATA_TYPE)
            .withAttributes(RegisterDoiRequest.AttributesBuilder.newBuilder()
                .withDoi(EXAMPLE_ATTRIBUTES_DOI)
                .build())
            .build();

        Data expected = new Data(EXAMPLE_DATA_ID, EXAMPLE_DATA_TYPE, new Attributes(EXAMPLE_ATTRIBUTES_DOI));
        assertThat(actual.getData(), is(equalTo(expected)));
    }


}