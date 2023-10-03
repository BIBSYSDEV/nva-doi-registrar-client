package no.unit.nva.doi.datacite.customerconfigs;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.Path;
import no.unit.nva.doi.datacite.utils.FakeSecretsManagerCountingCalls;
import no.unit.nva.stubs.FakeSecretsManagerClient;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import nva.commons.secrets.SecretsReader;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomerConfigsExtractorImplTest {

    private static final String SECRET_NAME = "someSecretName";
    private static final String SECRET_KEY = "someSecretKey";
    private SecretsReader secretsReader;

    private CustomerConfigExtractorImpl customerConfigExtractor;

    @BeforeEach
    void setup() {
        var fakeSecretsManagerClient = new FakeSecretsManagerClient();
        fakeSecretsManagerClient.putSecret(SECRET_NAME, SECRET_KEY, getValidSecretString());
        this.secretsReader = new SecretsReader(fakeSecretsManagerClient);
        this.customerConfigExtractor = new CustomerConfigExtractorImpl(secretsReader,
                                                                       SECRET_NAME,
                                                                       SECRET_KEY);
    }

    @Test
    void shouldThrowExceptionWhenRetrievingCustomerConfigIfSecretReaderDoesNotContainCustomer() {
        var fakeSecretsManagerClient = new FakeSecretsManagerClient();
        this.secretsReader = new SecretsReader(fakeSecretsManagerClient);
        this.customerConfigExtractor = new CustomerConfigExtractorImpl(secretsReader, SECRET_NAME, SECRET_KEY);
        assertThrows(CustomerConfigException.class, () -> customerConfigExtractor.getCustomerConfig(randomUri()));
    }

    @Test
    void shouldThrowExceptionIfConfigFromSecretsManagerIsNotParsable() {
        var fakeSecretsManagerClient = new FakeSecretsManagerClient();
        fakeSecretsManagerClient.putSecret(SECRET_NAME, SECRET_KEY, randomString());
        this.secretsReader = new SecretsReader(fakeSecretsManagerClient);
        this.customerConfigExtractor = new CustomerConfigExtractorImpl(secretsReader, SECRET_NAME, SECRET_KEY);
        assertThrows(CustomerConfigException.class, () -> customerConfigExtractor.getCustomerConfig(randomUri()));
    }

    @Test
    void shouldThrowExceptionWhenAttemptingToRetrieveCustomerThatDoesNotExist() {
        var customerUriNotInConfig = randomUri();
        assertThrows(CustomerConfigException.class,
                     () -> customerConfigExtractor.getCustomerConfig(customerUriNotInConfig));
    }

    @Test
    void shouldReturnCustomerWhenRetrievingCustomerThatExistInConfig()
        throws CustomerConfigException {
        var expectedCustomer = new CustomerConfig(UriWrapper.fromUri("https://example.net/customer/id/1234").getUri(),
                                                  "randompasswd1",
                                                  "user1.repository",
                                                  "10.5072");
        var actualCustomer = customerConfigExtractor.getCustomerConfig(expectedCustomer.getCustomerId());
        assertThat(actualCustomer, is(equalTo(expectedCustomer)));
    }

    @Test
    void shouldOnlyFetchSecretOnceAfterBeingConstructed() throws CustomerConfigException {
        var fakeSecretsManagerClientCountingCalls = new FakeSecretsManagerCountingCalls();
        fakeSecretsManagerClientCountingCalls.putSecret(SECRET_NAME, SECRET_KEY, getValidSecretString());
        this.secretsReader = new SecretsReader(fakeSecretsManagerClientCountingCalls);
        this.customerConfigExtractor = new CustomerConfigExtractorImpl(secretsReader, SECRET_NAME, SECRET_KEY);
        var expectedCustomer = new CustomerConfig(UriWrapper.fromUri("https://example.net/customer/id/1234").getUri(),
                                                  "randompasswd1",
                                                  "user1.repository",
                                                  "10.5072");
        var actualCustomerFirst = customerConfigExtractor.getCustomerConfig(expectedCustomer.getCustomerId());
        var actualCustomerSecond = customerConfigExtractor.getCustomerConfig(expectedCustomer.getCustomerId());
        assertThat(actualCustomerFirst, is(equalTo(expectedCustomer)));
        assertThat(actualCustomerSecond, is(equalTo(expectedCustomer)));
        assertThat(fakeSecretsManagerClientCountingCalls.getNumberOfTimesFetchSecretsHasBeenCalled(), is(equalTo(1)));
    }

    private String getValidSecretString() {
        return IoUtils.stringFromResources(Path.of("example-mds-config.json"));
    }
}
