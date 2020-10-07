package no.unit.nva.doi.assigndoi.model.datacite.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.net.URI;
import no.unit.nva.doi.assigndoi.model.datacite.request.FindableDoiRequest;
import no.unit.nva.doi.assigndoi.model.datacite.request.FindableDoiRequest.Attributes;
import org.junit.jupiter.api.Test;

class FindableDoiRequestTest {

    private final static String EXAMPLE_ATTRIBUTE_DOI = "10.5438/0012";
    public static final String EXAMPLE_ATTRIBUTE_XML =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxyZXNvdXJjZSB4bWxucz0iaHR0cDovL2RhdGFjaXRlLm9yZy9zY2hlbWEva2VybmVsLTQiIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTpzY2hlbWFMb2NhdGlvbj0iaHR0cDovL2RhdGFjaXRlLm9yZy9zY2hlbWEva2VybmVsLTQgaHR0cDovL3NjaGVtYS5kYXRhY2l0ZS5vcmcvbWV0YS9rZXJuZWwtNC9tZXRhZGF0YS54c2QiPg0KCTxpZGVudGlmaWVyIGlkZW50aWZpZXJUeXBlPSJET0kiPjEwLjU0MzgvMDAxMjwvaWRlbnRpZmllcj4NCgk8Y3JlYXRvcnM";
    private static final URI EXAMPLE_ATTRIBUTE_URL = URI.create(
        Attributes.SCHEMA_DATACITE_KERNEL_4_0);

    @Test
    public void testAttributeBuilder() {
        var actual = FindableDoiRequest.AttributesBuilder.newBuilder()
            .withDoi(EXAMPLE_ATTRIBUTE_DOI)
            .withXML(EXAMPLE_ATTRIBUTE_XML)
            .withURL(EXAMPLE_ATTRIBUTE_URL)
            .build();

        assertThat(actual.getDoi(), is(equalTo(EXAMPLE_ATTRIBUTE_DOI)));
        assertThat(actual.getXml(), is(equalTo(EXAMPLE_ATTRIBUTE_XML)));
        assertThat(actual.getUrl(), is(equalTo(EXAMPLE_ATTRIBUTE_URL)));
    }
}