package no.unit.nva.doi.datacite.clients;

final class DataCiteClientErrorMessages {


    public static final String HTTP_STATUS_LOG_TEMPLATE = " ({})";
    public static final String ERROR_CREATING_DOI = "Error creating new DOI with metadata";
    public static final String ERROR_UPDATING_METADATA_FOR_DOI = "Error updating metadata for DOI";
    public static final String ERROR_SETTING_DOI_URL = "Error setting DOI url";
    public static final String ERROR_DELETING_DOI_METADATA = "Error deleting DOI metadata";
    public static final String ERROR_DELETING_DOI = "Error deleting DOI";
    public static final String ERROR_COMMUNICATION_TEMPLATE = "Error during API communication: ({})";
    public static final String ERROR_GETTING_DOI = "Error getting DOI";
    public static final String COLON_SPACE = ": ";
    public static final String PREFIX_TEMPLATE_ENTRY = "{}";
    public static final String DOI_AND_HTTP_STATUS_TEMPLATE_ENTRIES = COLON_SPACE
                                                                      + PREFIX_TEMPLATE_ENTRY
                                                                      + HTTP_STATUS_LOG_TEMPLATE;
    public static final String ERROR_UPDATING_METADATA_FOR_DOI_TEMPLATE =
        ERROR_UPDATING_METADATA_FOR_DOI
        + DOI_AND_HTTP_STATUS_TEMPLATE_ENTRIES;
    public static final String ERROR_DELETING_DOI_TEMPLATE =
        ERROR_DELETING_DOI
        + DOI_AND_HTTP_STATUS_TEMPLATE_ENTRIES;
    public static final String ERROR_DELETING_DOI_METADATA_TEMPLATE =
        ERROR_DELETING_DOI_METADATA
        + DOI_AND_HTTP_STATUS_TEMPLATE_ENTRIES;
    public static final String ERROR_SETTING_DOI_URL_TEMPLATE =
        ERROR_SETTING_DOI_URL
        + DOI_AND_HTTP_STATUS_TEMPLATE_ENTRIES;
    public static final String ERROR_GETTING_DOI_TEMPLATE =
        ERROR_GETTING_DOI
        + DOI_AND_HTTP_STATUS_TEMPLATE_ENTRIES;

    private static final String HTTP_FAILED_RESPONSE_MESSAGE = "{}";
    public static final String ERROR_CREATING_DOI_TEMPLATE =
        ERROR_CREATING_DOI
        + PREFIX_TEMPLATE_ENTRY
        + HTTP_STATUS_LOG_TEMPLATE
        + HTTP_FAILED_RESPONSE_MESSAGE;

    private DataCiteClientErrorMessages() {

    }
}
