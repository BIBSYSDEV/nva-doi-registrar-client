# Nva Datacite MDS

### Secrets Manager

Depends on secret "dataciteMdsConfigs"

Value should be stored in JSON like this:

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