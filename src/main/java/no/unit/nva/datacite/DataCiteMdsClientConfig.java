package no.unit.nva.datacite;

public class DataCiteMdsClientConfig {

    public String institution;
    public String institutionPrefix;
    public String dataCiteMdsClient_url;
    public String dataCiteMdsClient_username;
    public String dataCiteMdsClient_password;

    public DataCiteMdsClientConfig(String institution, String institutionPrefix, String dataCiteMdsClient_url, String dataCiteMdsClient_username, String dataCiteMdsClient_password) {
        this.institution = institution;
        this.institutionPrefix = institutionPrefix;
        this.dataCiteMdsClient_url = dataCiteMdsClient_url;
        this.dataCiteMdsClient_username = dataCiteMdsClient_username;
        this.dataCiteMdsClient_password = dataCiteMdsClient_password;
    }
}
