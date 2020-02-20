package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "geoLocationPoint")
public class GeoLocationPoint {

    private String pointLongitude;
    private String pointLatitude;

    public GeoLocationPoint() {
    }

    public GeoLocationPoint(String pointLongitude, String pointLatitude) {
        this.pointLongitude = pointLongitude;
        this.pointLatitude = pointLatitude;
    }

    public String getPointLongitude() {
        return pointLongitude;
    }

    public GeoLocationPoint pointLongitude(String pointLongitude) {
        this.pointLongitude = pointLongitude;
        return this;
    }

    public String getPointLatitude() {
        return pointLatitude;
    }

    public GeoLocationPoint pointLatitude(String pointLatitude) {
        this.pointLatitude = pointLatitude;
        return this;
    }

}
