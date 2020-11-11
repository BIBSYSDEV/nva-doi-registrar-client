# Nva Datacite MDS

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7998e73fbdfb4b1eb18c761abc0e7499)](https://app.codacy.com/gh/BIBSYSDEV/nva-doi-registrar-client?utm_source=github.com&utm_medium=referral&utm_content=BIBSYSDEV/nva-doi-registrar-client&utm_campaign=Badge_Grade_Settings)

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
