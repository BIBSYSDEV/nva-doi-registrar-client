package no.unit.nva.datacite;


import org.junit.jupiter.api.Test;

public class DataCiteMdsClientConfigTest {


    public static final String INSTITUTION = "institution";
    public static final String INSTITUTION_PREFIX = "institutionPrefix";
    public static final String DATA_CITE_MDS_CLIENT_URL = "dataCiteMdsClientUrl";
    public static final String DATACITE_MDS_CLIENT_USERNAME = "dataCiteMdsClientUsername";
    public static final String DATACITE_MDS_CLIENT_PASSWORD = "dataCiteMdsClientPassword";

    @Test
    public void exists() {
        new DataCiteMdsClientConfig(INSTITUTION, INSTITUTION_PREFIX, DATA_CITE_MDS_CLIENT_URL,
                DATACITE_MDS_CLIENT_USERNAME, DATACITE_MDS_CLIENT_PASSWORD);
    }

    @Test
    public void testSetters() {
        DataCiteMdsClientConfig dataCiteMdsClientConfig = new DataCiteMdsClientConfig(INSTITUTION, INSTITUTION_PREFIX,
                DATA_CITE_MDS_CLIENT_URL, DATACITE_MDS_CLIENT_USERNAME, DATACITE_MDS_CLIENT_PASSWORD);
        dataCiteMdsClientConfig.setInstitution(INSTITUTION);
        dataCiteMdsClientConfig.setInstitutionPrefix(INSTITUTION_PREFIX);
        dataCiteMdsClientConfig.setDataCiteMdsClientUrl(DATA_CITE_MDS_CLIENT_URL);
        dataCiteMdsClientConfig.setDataCiteMdsClientUsername(DATACITE_MDS_CLIENT_USERNAME);
        dataCiteMdsClientConfig.setDataCiteMdsClientPassword(DATACITE_MDS_CLIENT_PASSWORD);
    }

}