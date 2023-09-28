package no.unit.nva.datacite.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;

import java.net.URI;

public class ReserveDoiRequest implements JsonSerializable {

    private final URI customer;

    @JsonCreator
    public ReserveDoiRequest(@JsonProperty("customer") URI customer) {
        this.customer = customer;
    }

    public URI getCustomer() {
        return customer;
    }
}
