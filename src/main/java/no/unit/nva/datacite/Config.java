package no.unit.nva.datacite;

import org.apache.commons.lang3.StringUtils;

public class Config {

    public static final String MISSING_ENVIRONMENT_VARIABLES = "Missing environment variables";

    public static final String CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME = "ALLOWED_ORIGIN";
    public static final String DATACITE_MDS_CONFIGS_SECRET_ID_ENVIRONMENT_NAME = "DATACITE_MDS_CONFIGS";
    public static final String NVA_HOST_ENVIRONMENT_NAME = "NVA_HOST";

    private String corsHeader;
    private String nvaHost;
    private String dataCiteMdsConfigsSecretId;


    private Config() {
    }

    private static class LazyHolder {

        private static final Config INSTANCE = new Config();

        static {
            INSTANCE.setCorsHeader(System.getenv(CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME));
            INSTANCE.setDataCiteMdsConfigsSecretId(System.getenv(DATACITE_MDS_CONFIGS_SECRET_ID_ENVIRONMENT_NAME));
            INSTANCE.setNvaHost(System.getenv(NVA_HOST_ENVIRONMENT_NAME));
        }
    }

    public static Config getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean checkProperties() {
        if (StringUtils.isEmpty(dataCiteMdsConfigsSecretId) || StringUtils.isEmpty(nvaHost)) {
            throw new RuntimeException(MISSING_ENVIRONMENT_VARIABLES);
        }
        return true;
    }

    public String getNvaHost() {
        return nvaHost;
    }

    public void setNvaHost(String nvaHost) {
        this.nvaHost = nvaHost;
    }

    public String getDataCiteMdsConfigsSecretId() {
        return dataCiteMdsConfigsSecretId;
    }

    public void setDataCiteMdsConfigsSecretId(String dataCiteMdsConfigsSecretId) {
        this.dataCiteMdsConfigsSecretId = dataCiteMdsConfigsSecretId;
    }

    public String getCorsHeader() {
        return corsHeader;
    }

    public void setCorsHeader(String corsHeader) {
        this.corsHeader = corsHeader;
    }

}
