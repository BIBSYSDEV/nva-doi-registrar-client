package no.unit.nva.datacite.clients.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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

    private String createRandomSuffix() {
        return UUID.randomUUID().toString();
    }
}