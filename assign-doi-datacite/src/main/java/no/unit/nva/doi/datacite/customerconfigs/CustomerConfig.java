package no.unit.nva.doi.datacite.customerconfigs;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Base64;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

public class CustomerConfig {

    private static final String ERROR_HAS_INVALID_CONFIGURATION = " has invalid configuration!";
    private static final String COLON = ":";

    private final URI customerId;
    private final String password;
    private final String username;
    private final String doiPrefix;

    public CustomerConfig(@JsonProperty("customerId") final URI customerId,
                          @JsonProperty("dataCiteMdsClientPassword") final String password,
                          @JsonProperty("dataCiteMdsClientUsername") final String username,
                          @JsonProperty("customerDoiPrefix") final String doiPrefix) {
        this.customerId = customerId;
        this.password = password;
        this.username = username;
        this.doiPrefix = doiPrefix;
    }

    public String extractBasicAuthenticationString()
        throws CustomerConfigException {
        if (!isFullyConfigured()) {
            throw new CustomerConfigException(customerId + ERROR_HAS_INVALID_CONFIGURATION);
        }
        return basicAuth(username, password);
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + COLON + password).getBytes());
    }

    private boolean isFullyConfigured() {
        return StringUtils.isNotBlank(password)
               && StringUtils.isNotBlank(username)
               && StringUtils.isNotBlank(doiPrefix);
    }

    public URI getCustomerId() {
        return customerId;
    }

    public String getDoiPrefix() {
        return doiPrefix;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomerConfig that = (CustomerConfig) o;
        return Objects.equals(customerId, that.customerId)
               && Objects.equals(password, that.password)
               && Objects.equals(username, that.username)
               && Objects.equals(doiPrefix, that.doiPrefix);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(customerId, password, username, doiPrefix);
    }
}
