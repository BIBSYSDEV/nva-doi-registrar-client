package no.unit.nva.datacite;


import no.unit.nva.datacite.models.DataCiteMdsClientSecretConfig;
import org.junit.jupiter.api.Test;

public class DataCiteMdsClientSecretConfigTest {


    public static final String INSTITUTION = "institution";
    public static final String INSTITUTION_PREFIX = "institutionPrefix";
    public static final String DATA_CITE_MDS_CLIENT_URL = "dataCiteMdsClientUrl";
    public static final String DATACITE_MDS_CLIENT_USERNAME = "dataCiteMdsClientUsername";
    public static final String DATACITE_MDS_CLIENT_PASSWORD = "dataCiteMdsClientPassword";

    @Test
    public void exists() {
        new DataCiteMdsClientSecretConfig(INSTITUTION, INSTITUTION_PREFIX, DATA_CITE_MDS_CLIENT_URL,
                DATACITE_MDS_CLIENT_USERNAME, DATACITE_MDS_CLIENT_PASSWORD);
    }

    @Test
    public void testSetters() {
        DataCiteMdsClientSecretConfig dataCiteMdsClientSecretConfig = new DataCiteMdsClientSecretConfig(INSTITUTION, INSTITUTION_PREFIX,
                DATA_CITE_MDS_CLIENT_URL, DATACITE_MDS_CLIENT_USERNAME, DATACITE_MDS_CLIENT_PASSWORD);
        dataCiteMdsClientSecretConfig.setInstitution(INSTITUTION);
        dataCiteMdsClientSecretConfig.setInstitutionPrefix(INSTITUTION_PREFIX);
        dataCiteMdsClientSecretConfig.setDataCiteMdsClientUrl(DATA_CITE_MDS_CLIENT_URL);
        dataCiteMdsClientSecretConfig.setDataCiteMdsClientUsername(DATACITE_MDS_CLIENT_USERNAME);
        dataCiteMdsClientSecretConfig.setDataCiteMdsClientPassword(DATACITE_MDS_CLIENT_PASSWORD);
    }

}