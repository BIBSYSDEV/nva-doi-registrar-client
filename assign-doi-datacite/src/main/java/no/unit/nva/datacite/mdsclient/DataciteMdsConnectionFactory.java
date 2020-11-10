package no.unit.nva.datacite.mdsclient;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.time.Duration;
import no.unit.nva.datacite.config.PasswordAuthenticationFactory;

public class DataciteMdsConnectionFactory {


    private final PasswordAuthenticationFactory authenticationFactory;
    private final String mdsEndpoint;

    public DataciteMdsConnectionFactory(PasswordAuthenticationFactory authenticationFactory, String mdsEndpoint) {
        this.authenticationFactory = authenticationFactory;
        this.mdsEndpoint = mdsEndpoint;
    }

    public DataCiteMdsConnection getAuthenticatedConnection(String customerId) {
        return new DataCiteMdsConnection(HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(2))
            .authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return authenticationFactory.getCredentials(customerId);
                }
            })
            .build(), mdsEndpoint);
    }
}
