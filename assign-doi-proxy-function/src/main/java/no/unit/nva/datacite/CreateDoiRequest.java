package no.unit.nva.datacite;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateDoiRequest {

    private final String url;
    private final String institutionId;
    private final String dataciteXml;


    /**
     * Creates a request to create a DataCite DOI.
     *
     * @param url           landing page url
     * @param institutionId institution id found in configuration
     * @param dataciteXml   datacite metadata xml
     */
    @JsonCreator
    public CreateDoiRequest(@JsonProperty("url") String url,
                            @JsonProperty("institutionId") String institutionId,
                            @JsonProperty("dataciteXml") String dataciteXml) {
        this.url = url;
        this.institutionId = institutionId;
        this.dataciteXml = dataciteXml;
    }

    public String getUrl() {
        return url;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public String getDataciteXml() {
        return dataciteXml;
    }

}