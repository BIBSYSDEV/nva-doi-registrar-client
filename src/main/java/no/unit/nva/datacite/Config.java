package no.unit.nva.datacite;


import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.TreeMap;

public class Config {

    public static final String MISSING_ENVIRONMENT_VARIABLES = "Missing environment variables";
    public static final String DATACITE_MDS_CONFIGS = "DATACITE_MDS_CONFIGS";

    public static final String CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME = "ALLOWED_ORIGIN";

    private String corsHeader;
    private Map<String, DataCiteMdsClientConfig> dataCiteMdsClientConfigsMap = new TreeMap<>();

    private Config() {
    }

    private static class LazyHolder {

        private static final Config INSTANCE = new Config();

        static {
            INSTANCE.setCorsHeader(System.getenv(CORS_ALLOW_ORIGIN_HEADER_ENVIRONMENT_NAME));
            INSTANCE.setDataCiteMdsConfigs(System.getenv(DATACITE_MDS_CONFIGS));
        }
    }

    public boolean checkProperties() {
        if (dataCiteMdsClientConfigsMap.isEmpty()) {
            throw new RuntimeException(MISSING_ENVIRONMENT_VARIABLES);
        }
        return true;
    }

    public void setDataCiteMdsConfigs(String dataCiteMdsConfigs) {
        DataCiteMdsClientConfig[] dataCiteMdsClientConfigs = new Gson().fromJson(dataCiteMdsConfigs, DataCiteMdsClientConfig[].class);
        if (dataCiteMdsClientConfigs != null) {
            for (DataCiteMdsClientConfig dataCiteMdsClientConfig : dataCiteMdsClientConfigs) {
                dataCiteMdsClientConfigsMap.put(dataCiteMdsClientConfig.institution, dataCiteMdsClientConfig);
            }
        }
    }

    public DataCiteMdsClientConfig getDataciteMdsConfigForInstitution(String institution) {
        return dataCiteMdsClientConfigsMap.get(institution);
    }

    public static Config getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getCorsHeader() {
        return corsHeader;
    }

    public void setCorsHeader(String corsHeader) {
        this.corsHeader = corsHeader;
    }

}
