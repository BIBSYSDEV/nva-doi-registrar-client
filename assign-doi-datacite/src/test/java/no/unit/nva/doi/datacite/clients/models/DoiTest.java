package no.unit.nva.doi.datacite.clients.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;


class DoiTest {

    private static final String DEMO_PREFIX = "10.5072";
    private static final String EXAMPLE_PREFIX = DEMO_PREFIX;
    public static final String DOI_PROXY = "https://doi.org/";
    public static final String FORWARD_SLASH = "/";
    public static final String DUMMY_SUFFIXID = "dummy-suffixid";

    @Test
    void toIdentifier() {
        String randomSuffix = createRandomSuffix();
        Doi doi = createDoi(randomSuffix);
        assertThat(doi.toIdentifier(), is(equalTo(EXAMPLE_PREFIX + "/" + randomSuffix)));
    }



    private Doi createDoi(String randomSuffix) {
        return ImmutableDoi.builder()
            .prefix(EXAMPLE_PREFIX)
            .suffix(randomSuffix)
            .build();
    }

    @Test
    void toId() {
        String randomSuffix = createRandomSuffix();
        var doi = createDoi(randomSuffix);
        URI expectedUri = URI.create(DOI_PROXY + EXAMPLE_PREFIX + FORWARD_SLASH + randomSuffix);
        assertThat(doi.toId(), is(equalTo(expectedUri)));
    }

    @Test
    void builderWithIdentifierSetsPrefixAndSuffixCorrect() {
        String exampleIdentifier = "10.5072/dummy-suffixid";
        Doi doi = ImmutableDoi.builder().identifier(exampleIdentifier).build();
        assertThat(doi.prefix(), is(equalTo(DEMO_PREFIX)));
        assertThat(doi.suffix(), is(equalTo(DUMMY_SUFFIXID)));
    }

    @Test
    void builderOnlyPrefixThrowsException() {
        var actual = assertThrows(IllegalStateException.class,
            () -> ImmutableDoi.builder().prefix(DEMO_PREFIX).build());
        assertThat(actual.getMessage(), containsString("required attributes are not set"));
    }

    @Test
    void builderOnlySuffixThrowsException() {
        var actual = assertThrows(IllegalStateException.class,
            () -> ImmutableDoi.builder().suffix(createRandomSuffix()).build());
        assertThat(actual.getMessage(), containsString("required attributes are not set"));
    }

    @Test
    void builderWithNullIdentifierThrowsNPE() {
        assertThrows(NullPointerException.class, () -> ImmutableDoi.builder().identifier(null).build());
    }

    @Test
    void builderWithInvalidDoiThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ImmutableDoi.builder().identifier(DEMO_PREFIX).build());
    }

    private String createRandomSuffix() {
        return UUID.randomUUID().toString();
    }
}