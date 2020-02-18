package no.unit.nva.datacite;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;


import java.util.Map;
import java.util.Objects;

/**
 * Handler for requests to Lambda function.
 */
public class DataCiteMdsHandler implements RequestHandler<Map<String, Object>, GatewayResponse> {

    public static final String QUERY_PARAMETERS_KEY = "queryParameters";
    public static final String PATH_PARAMETERS_KEY = "pathParameters";
    public static final String PATH_PARAMETER_IDENTIFIER = "identifier";
    public static final String MISSING_PATH_PARAMETER_IDENTIFIER =
            "Missing path param '" + PATH_PARAMETER_IDENTIFIER + "'";
    public static final int ERROR_CALLING_REMOTE_SERVER = Response.Status.BAD_GATEWAY.getStatusCode();
    public static final String REMOTE_SERVER_ERRORMESSAGE = "remote server errormessage: ";

    protected final transient DataCiteMdsConnection dataciteMdsConnection;

    public DataCiteMdsHandler() {
        this.dataciteMdsConnection = new DataCiteMdsConnection();
    }

    public DataCiteMdsHandler(DataCiteMdsConnection dataciteMdsConnection) {
        this.dataciteMdsConnection = dataciteMdsConnection;
    }

    /**
     * Main lambda function.
     *
     * @return a GatewayResponse
     */
    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(final Map<String, Object> input, Context context) {
        Config.getInstance().checkProperties();
        GatewayResponse gatewayResponse = new GatewayResponse();
        try {
            this.checkParameters(input);
        } catch (RuntimeException e) {
            System.out.println(e);
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }

        return gatewayResponse;
    }


    @SuppressWarnings("unchecked")
    private void checkParameters(Map<String, Object> input) {
        Map<String, String> pathParameters = (Map<String, String>) input.get(PATH_PARAMETERS_KEY);
        if (Objects.isNull(pathParameters)) {
            throw new RuntimeException(MISSING_PATH_PARAMETER_IDENTIFIER);
        }
        if (StringUtils.isEmpty(pathParameters.get(PATH_PARAMETER_IDENTIFIER))) {
            throw new RuntimeException(MISSING_PATH_PARAMETER_IDENTIFIER);
        }
    }

}
