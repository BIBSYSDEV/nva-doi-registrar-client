# nva-doi-registrar-client

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/08aae785336d4c9f9c66ee3e5975889e)](https://www.codacy.com/gh/BIBSYSDEV/nva-doi-registrar-client/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BIBSYSDEV/nva-doi-registrar-client&amp;utm_campaign=Badge_Grade)

Library and lambda functions which integrates towards Datacite (registry agency, RA).

## API integrations

Datacite MDS

## Documentation about DOI

See NVAs page in Confluence about [DOI](https://unit.atlassian.net/wiki/spaces/NVAP/pages/979763260/Digital+Object+Identifier+DOI)

### Input

```
{
    "url": "[url-value]",
    "institutionId": "[institutionId-value]",
    "dataciteXml": "[dataciteXml-value]"
}
``` 
url: Landing page URL

institutionId: Identifier of institution

dataciteXml: Metadata of resource, expected format: https://schema.datacite.org/meta/kernel-4/  

### Secrets Manager (AWS)

Secret with id "dataciteMdsConfigs" must present. 

Value of secret should be stored in JSON format like this:

```
[
  {
    "customerId": "https://example.net/nva/customer/id/1234",
    "customerDoiPrefix": "10.5072",
    "dataCiteMdsClientUrl": "https://mds.test.datacite.org",
    "dataCiteMdsClientUsername": "org.repositoryuser",
    "dataCiteMdsClientPassword": "CUSTOMER-NVA-PASSWORD"
  },
  ...
]
```

#### Description of configuration

-  customerId: IRI for NVA customer.
-  customerDoiPrefix:  A reserved NVA repository containing associated DOI prefix for NVA usage.
-  dataCiteMdsClientUrl: API endpoint to DataCite MDS API.
-  dataCiteMdsClientPassword: Organization's repository user password.

## assignd-doi-datecite-example CLI client

You can easily run the CLI via gradle by issuing the commands with:

`./gradlew :assign-doi-datacite-example:run --args="<command line argument goes here>"`

Examples below, copy them into `<command line argument goes here>`

> Create DOI with metadata

`create --config $(pwd)/datacite-mds-test-config.json --customer https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934 -m $(pwd)/assign-doi-datacite-example/example-minimal-document.xml 10.16903`

> Delete DOI in Draft state

`delete doi --config $(pwd)/datacite-mds-test-config.json --customer https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934 10.16903/test-rono-dev-example3`

> Delete DOI metadata  (This makes it de-listed)

`delete metadata --config $(pwd)/datacite-mds-test-config.json --customer https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934 10.16903/test-rono-dev-example1`

> Update DOI metadata

`update --config $(pwd)/datacite-mds-test-config.json --customer https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934 -m $(pwd)/assign-doi-datacite-example/example-minimal-document.xml 10.16903/test-rono-dev-example1`

> Update DOI landing page 

`landingpage --config $(pwd)/datacite-mds-test-config.json --customer https://api.dev.nva.aws.unit.no/customer/f54c8aa9-073a-46a1-8f7c-dde66c853934 -l https://github.com/BIBSYSDEV/nva-doi-registrar-client 10.16903/test-rono-dev-example1`

Notice changes won't show before it has gone 1 day! DOI landing page are cached for 24 hours. 
You need to query the JSON API in the handle server to verify if it has been updated.

> Other information

The CLI client is hardcoded and configured to run towards test environment API servers, so even if you change the `--config` to contain `dataCiteMdsClientUrl` for production, the authenticator won't authenticate for security reasons.
 We want to be really careful about minting DOIs in production! 
