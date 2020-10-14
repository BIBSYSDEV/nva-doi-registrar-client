package no.unit.nva.events.examples;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;

import java.net.URI;
import org.junit.jupiter.api.Test;

public class DataciteDoiRequestTest {

    public static final String SOME_TYPE = "SomeType";
    public static final String SOME_XML = "SomeXml";
    public static final URI SOME_PUBLICATION_ID = URI.create("https://publication.com");
    public static final URI SOME_DOI = URI.create("https://someDoi.org");

    @Test
    public void builderContainsAllFields() {
        DataciteDoiRequest actual = sampleDataciteDoiRequest();

        assertThat(actual, is(not(nullValue())));
        assertThat(actual, doesNotHaveNullOrEmptyFields());
    }

    @Test
    public void equalsReturnsTrueForEquivalentObjects() {
        DataciteDoiRequest left = sampleDataciteDoiRequest();
        DataciteDoiRequest right = sampleDataciteDoiRequest();

        assertThat(left, doesNotHaveNullOrEmptyFields());
        assertThat(left, is(equalTo(right)));
        assertThat(left, is(not(sameInstance(right))));
    }

    @Test
    public void copyCopiesAllFields() {
        DataciteDoiRequest left = sampleDataciteDoiRequest();
        DataciteDoiRequest right = left.copy().build();

        assertThat(left, doesNotHaveNullOrEmptyFields());
        assertThat(right, doesNotHaveNullOrEmptyFields());
        assertThat(left, is(equalTo(right)));
    }

    @Test
    public void copyCopiesGeneratesNewObject() {
        DataciteDoiRequest left = sampleDataciteDoiRequest();
        DataciteDoiRequest right = left.copy().build();
        assertThat(left, is(not(sameInstance(right))));
    }

    private DataciteDoiRequest sampleDataciteDoiRequest() {
        return DataciteDoiRequest.newBuilder()
            .withType(SOME_TYPE)
            .withXml(SOME_XML)
            .withPublicationId(SOME_PUBLICATION_ID)
            .withExistingDoi(SOME_DOI)
            .build();
    }
}