package no.unit.nva.doi.datacite.clients;

import no.unit.nva.doi.models.Doi;

public class DataciteClientTestBase {

    protected static final String DEMO_PREFIX = "10.5072";

    protected static final String INSTITUTION_PREFIX = DEMO_PREFIX;
    protected static final String EXAMPLE_DOI_SUFFIX = "1942810412-sadsfgffds";

    protected Doi createDoi(String prefix, String suffix) {
        return Doi.builder().withPrefix(prefix).withSuffix(suffix).build();
    }

    protected Doi createDoiWithDemoPrefixAndExampleSuffix() {
        return createDoi(DEMO_PREFIX, EXAMPLE_DOI_SUFFIX);
    }

    protected enum DoiStateStatus {
        DRAFT, FINDABLE, REGISTERED /* Marked as deleted */
    }
}
