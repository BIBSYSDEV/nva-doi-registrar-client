package no.unit.nva.datacite.clients;

import java.net.URI;

/**
 * NVAs supported DOI operations.
 */
public interface DoiClient {

    /**
     * Create a DOI where registry agency (ie datacite) auto generates the suffix under the associated customer repository for NVA.
     * @param customerId NVAs customerId
     * @param metadataDataciteXml datacite schema serialized xml as string
     * @return DOI from provider in the following syntax: prefix/suffix
     */
    String createDoi(String customerId, String metadataDataciteXml);

    /**
     * Update metadata for a given DOI.
     * @param customerId NVAs customerId
     * @param doi DOI in the format of prefix/suffix
     * @param metadataDataciteXml datacite schema serialized xml as string
     */
    void updateMetadata(String customerId, String doi, String metadataDataciteXml);

    /**
     * Request to mark DOI as findable.
     * <p>Once a DOI is findable, it cannot be deleted!
     * @param customerId NVAs customerId
     * @param doi DOI in the format of prefix/suffix
     * @param url Location of landing page.
     */
    void setLandingPage(String customerId, String doi, URI url);
}
