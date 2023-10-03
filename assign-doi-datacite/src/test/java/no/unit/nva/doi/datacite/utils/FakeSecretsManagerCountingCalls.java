package no.unit.nva.doi.datacite.utils;

import no.unit.nva.stubs.FakeSecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class FakeSecretsManagerCountingCalls extends FakeSecretsManagerClient implements SecretsManagerClient {

    private int numberOfTimesFetchSecretsHasBeenCalled;

    public FakeSecretsManagerCountingCalls() {
        super();
        numberOfTimesFetchSecretsHasBeenCalled = 0;
    }

    @Override
    public GetSecretValueResponse getSecretValue(GetSecretValueRequest getSecretValueRequest) {
        numberOfTimesFetchSecretsHasBeenCalled++;
        return super.getSecretValue(getSecretValueRequest);
    }

    public int getNumberOfTimesFetchSecretsHasBeenCalled() {
        return numberOfTimesFetchSecretsHasBeenCalled;
    }
}
