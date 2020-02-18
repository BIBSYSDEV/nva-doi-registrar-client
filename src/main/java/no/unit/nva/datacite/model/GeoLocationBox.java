package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "geolocationBox")
public class GeoLocationBox {

    private String westBoundLongitude;
    private String eastBoundLongitude;
    private String southBoundLatitude;
    private String northBoundLatitude;

    public GeoLocationBox() {
    }

    public GeoLocationBox(String westBoundLongitude, String eastBoundLongitude, String southBoundLatitude, String northBoundLatitude) {
        this.westBoundLongitude = westBoundLongitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.southBoundLatitude = southBoundLatitude;
        this.northBoundLatitude = northBoundLatitude;
    }

    public String getWestBoundLongitude() {
        return westBoundLongitude;
    }

    public GeoLocationBox westBoundLongitude(String westBoundLongitude) {
        this.westBoundLongitude = westBoundLongitude;
        return this;
    }

    public String getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    public GeoLocationBox eastBoundLongitude(String eastBoundLongitude) {
        this.eastBoundLongitude = eastBoundLongitude;
        return this;
    }

    public String getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    public GeoLocationBox southBoundLatitude(String southBoundLatitude) {
        this.southBoundLatitude = southBoundLatitude;
        return this;
    }

    public String getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    public GeoLocationBox northBoundLatitude(String northBoundLatitude) {
        this.northBoundLatitude = northBoundLatitude;
        return this;
    }
}
