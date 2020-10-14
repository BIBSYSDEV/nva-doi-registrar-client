package no.unit.nva.events.handlers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.Objects;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public class SampleHandlerInput {

    private String name;
    private String message;
    private Integer identifier;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SampleHandlerInput that = (SampleHandlerInput) o;
        return Objects.equals(getName(), that.getName())
            && Objects.equals(getMessage(), that.getMessage())
            && Objects.equals(getIdentifier(), that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMessage(), getIdentifier());
    }
}
