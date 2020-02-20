package no.unit.nva.datacite.model.generated;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(namespace = "resourceType")
public class ResourceType {

    @JacksonXmlText()
    private String resourceType;

    @JacksonXmlProperty(isAttribute = true)
    private String resourceTypeGeneral;

    public ResourceType() {
    }

    public ResourceType(String resourceType, String resourceTypeGeneral) {
        this.resourceType = resourceType;
        this.resourceTypeGeneral = resourceTypeGeneral;
    }

    public String getResourceType() {
        return resourceType;
    }

    public ResourceType resourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getResourceTypeGeneral() {
        return resourceTypeGeneral;
    }

    public ResourceType resourceTypeGeneral(String resourceTypeGeneral) {
        this.resourceTypeGeneral = resourceTypeGeneral;
        return this;
    }

}
