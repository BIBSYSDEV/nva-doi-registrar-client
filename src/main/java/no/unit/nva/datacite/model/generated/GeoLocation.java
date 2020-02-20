package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "geoLocation")
public class GeoLocation {

    private String geoLocationPlace;
    private GeoLocationPoint geoLocationPoint;
    private GeoLocationBox geoLocationBox;

    public GeoLocation() {
    }

    public GeoLocation(String geoLocationPlace) {
        this.geoLocationPlace = geoLocationPlace;
    }

    public String getGeoLocationPlace() {
        return geoLocationPlace;
    }

    public GeoLocation geoLocationPlace(String geoLocationPlace) {
        this.geoLocationPlace = geoLocationPlace;
        return this;
    }

    public GeoLocationPoint getGeoLocationPoint() {
        return geoLocationPoint;
    }

    public GeoLocation geoLocationPoint(GeoLocationPoint geoLocationPoint) {
        this.geoLocationPoint = geoLocationPoint;
        return this;
    }

    public GeoLocationBox getGeoLocationBox() {
        return geoLocationBox;
    }

    public GeoLocation geoLocationBox(GeoLocationBox geoLocationBox) {
        this.geoLocationBox = geoLocationBox;
        return this;
    }

}
