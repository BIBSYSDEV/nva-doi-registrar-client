package no.unit.nva.datacite.handlers;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public final class DraftDoiAppEnv {

    public static final String CUSTOMER_SECRETS_SECRET_NAME = "CUSTOMER_SECRETS_SECRET_NAME";
    public static final String CUSTOMER_SECRETS_SECRET_KEY = "CUSTOMER_SECRETS_SECRET_KEY";
    private static final Environment ENVIRONMENT = new Environment();

    @JacocoGenerated
    private DraftDoiAppEnv() {
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
