package no.unit.nva.doi.updatedoi.model.event;

public enum PublicationDoiRequestDetailType {
    DRAFT("doiRequest.draft"),
    FINDABLE("doiRequest.findable"),
    REGISTER("doiRequest.find");
//@JsonEnumDefaultValue UNKNOWN
    private final String detailType;

    PublicationDoiRequestDetailType(String detailType) {
        this.detailType = detailType;
    }

    @Override
    public String toString() {
        return detailType;
    }
}
