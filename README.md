# Nva Datacite MDS

### Query parameters (Input)
```
InstitutionID - 'institutionId'
URL - 'url'
DataCite XML - 'dataciteXML'
```
InstitutionID: Identifier of institution

URL: Landing page

DataCite XML: Metadata of resource, expected format: https://schema.datacite.org/meta/kernel-4/   

### Secrets Manager (AWS)

Depends on secret "dataciteMdsConfigs"

Value should be stored in JSON format like this:

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
