package no.unit.nva.doi.lambda;

public final class Env {

  public static final String EVENT_BUS_NAME = "EVENT_BUS_NAME";
  public static final String DLQ_URL = "DLQ_URL";
  public static final String MAX_ATTEMPT = "MAX_ATTEMPT";

  public static String getEventBusName() {
    return getEnvValue(EVENT_BUS_NAME);
  }

  public static String getDlqUrl() {
    return getEnvValue(DLQ_URL);
  }

  public static int getMaxAttempt() {
    return Integer.parseInt(getEnvValue(MAX_ATTEMPT));
  }

  private static String getEnvValue(final String name) {
    return System.getenv(name);
  }
}
