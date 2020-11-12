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
    "institution": "INSTITUTION-ID",
    "institutionPrefix": "INSTITUTION-DOI-PREFIX",
    "dataCiteMdsClientUrl": "https://mds(.test).datacite.org",
    "dataCiteMdsClientUsername": "INSTITUTION-DOI-PROVIDER-USERNAME",
    "dataCiteMdsClientPassword": "INSTITUTION-DOI-PROVIDER-PASSWORD"
    },
    ...
]
```
