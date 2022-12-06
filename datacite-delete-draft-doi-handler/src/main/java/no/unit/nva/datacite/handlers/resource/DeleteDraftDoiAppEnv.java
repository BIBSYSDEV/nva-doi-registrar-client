package no.unit.nva.datacite.handlers.resource;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public final class DeleteDraftDoiAppEnv {

    public static final String DATACITE_MDS_HOST = "DATACITE_MDS_HOST";
    public static final String DATACITE_REST_HOST = "DATACITE_REST_HOST";
    public static final String CUSTOMER_SECRETS_SECRET_NAME = "CUSTOMER_SECRETS_SECRET_NAME";
    public static final String CUSTOMER_SECRETS_SECRET_KEY = "CUSTOMER_SECRETS_SECRET_KEY";
    private static final Environment ENVIRONMENT = new Environment();

    @JacocoGenerated
    private DeleteDraftDoiAppEnv() {
    }

    @JacocoGenerated
    public static String getDataCiteMdsApiHost() {
        return getEnvValue(DATACITE_MDS_HOST);
    }

    @JacocoGenerated
    public static String getDataCiteRestApiHost() {
        return getEnvValue(DATACITE_REST_HOST);
    }

    @JacocoGenerated
    public static String getCustomerSecretsSecretName() {
        return getEnvValue(CUSTOMER_SECRETS_SECRET_NAME);
    }

    @JacocoGenerated
    public static String getCustomerSecretsSecretKey() {
        return getEnvValue(CUSTOMER_SECRETS_SECRET_KEY);
    }

    @JacocoGenerated
    private static String getEnvValue(final String name) {
        return ENVIRONMENT.readEnv(name);
    }
}
