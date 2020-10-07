package no.unit.nva.doi.assigndoi.model.datacite.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegisterDoiRequestTest {

    private static final String EXAMPLE_ATTRIBUTES_DOI = "http://valid-doi-goes-here.example.net";
    private static final String REGISTER_DOI_EVENT_TYPE = "register";

    @Test
    public void testAttributesBuilder() {
        var actual = RegisterDoiRequest.AttributesBuilder.newBuilder()
            .withDoi(EXAMPLE_ATTRIBUTES_DOI)
            .build();

        assertThat(actual.getDoi(), is(equalTo(EXAMPLE_ATTRIBUTES_DOI)));
        assertThat(actual.getEventType(), is(equalTo(REGISTER_DOI_EVENT_TYPE)));
    }


}