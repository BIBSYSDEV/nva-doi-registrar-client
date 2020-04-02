package no.unit.nva.datacite;

import org.apache.commons.lang3.StringUtils;

public class Config {

    public static final String ERROR_MISSING_ENVIRONMENT_VARIABLES = "Missing environment variables";

    public static final String ENVIRONMENT_NAME_CORS_ALLOW_ORIGIN_HEADER = "ALLOWED_ORIGIN";
    public static final String ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS = "DATACITE_MDS_CONFIGS";

    private String corsHeader;
    private String dataCiteMdsConfigs;


    private Config() {
    }

    private static class LazyHolder {

        private static final Config INSTANCE = new Config();

        static {
            INSTANCE.setCorsHeader(System.getenv(ENVIRONMENT_NAME_CORS_ALLOW_ORIGIN_HEADER));
            INSTANCE.setDataCiteMdsConfigs(System.getenv(ENVIRONMENT_NAME_DATACITE_MDS_CONFIGS));
        }
    }

    public static Config getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Checking if mandatory properties are present.
     *
     * @return <code>TRUE</code> if properties are present.
     */
    public boolean checkProperties() {
        if (StringUtils.isEmpty(getDataCiteMdsConfigs())) {
            throw new RuntimeException(ERROR_MISSING_ENVIRONMENT_VARIABLES);
        }
        return true;
    }


    public String getDataCiteMdsConfigs() {
        return dataCiteMdsConfigs;
    }

    public void setDataCiteMdsConfigs(String dataCiteMdsConfigsSecretId) {
        this.dataCiteMdsConfigs = dataCiteMdsConfigsSecretId;
    }

    public String getCorsHeader() {
        return corsHeader;
    }

    public void setCorsHeader(String corsHeader) {
        this.corsHeader = corsHeader;
    }

}
