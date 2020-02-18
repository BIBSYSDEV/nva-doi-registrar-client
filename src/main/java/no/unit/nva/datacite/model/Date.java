package no.unit.nva.datacite.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "date")
public class Date {

    @JacksonXmlProperty(isAttribute = true)
    private String dateType;

    @JacksonXmlText()
    private String date;

    public Date() {
    }

    public Date(String date) {
        this.date = date;
    }

    public Date(String dateType, String date) {
        this.dateType = dateType;
        this.date = date;
    }

    public String getDateType() {
        return dateType;
    }

    public Date dateType(String dateType) {
        this.dateType = dateType;
        return this;
    }

    public String getDate() {
        return date;
    }

    public Date date(String date) {
        this.date = date;
        return this;
    }
}
