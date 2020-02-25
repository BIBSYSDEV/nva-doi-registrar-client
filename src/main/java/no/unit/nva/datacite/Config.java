package no.unit.nva.datacite;

import org.apache.commons.lang3.StringUtils;

public class Config {

    public static final String MISSING_ENVIRONMENT_VARIABLES = "Missing environment variables";

    public static final String CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME = "ALLOWED_ORIGIN";
    public static final String DATACITE_MDS_CONFIGS_ENVIRONMENT_NAME = "DATACITE_MDS_CONFIGS";
    public static final String NVA_FRONTEND_HOST_ENVIRONMENT_NAME = "NVA_FRONTEND_HOST";

    private String corsHeader;
    private String nvaFrontendHost;
    private String dataCiteMdsConfigs;


    private Config() {
    }

    private static class LazyHolder {

        private static final Config INSTANCE = new Config();

        static {
            INSTANCE.setCorsHeader(System.getenv(CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME));
            INSTANCE.setDataCiteMdsConfigs(System.getenv(DATACITE_MDS_CONFIGS_ENVIRONMENT_NAME));
            INSTANCE.setNvaFrontendHost(System.getenv(NVA_FRONTEND_HOST_ENVIRONMENT_NAME));
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
        if (StringUtils.isEmpty(getDataCiteMdsConfigs()) || StringUtils.isEmpty(getNvaFrontendHost())) {
            throw new RuntimeException(MISSING_ENVIRONMENT_VARIABLES);
        }
        return true;
    }

    public String getNvaFrontendHost() {
        return nvaFrontendHost;
    }

    public void setNvaFrontendHost(String nvaFrontendHost) {
        this.nvaFrontendHost = nvaFrontendHost;
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
