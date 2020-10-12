# Nva Datacite MDS

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

## Other links

- https://blog.libscie.org/doi-primer - Nice blog that explains the DOI system, inlcuding what the numbers in a DOI actually mean, and the DOI history.