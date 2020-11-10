package no.unit.nva.datacite.clients;

import java.net.URI;
import no.unit.nva.datacite.clients.exception.ClientException;
import no.unit.nva.datacite.clients.models.Doi;

/**
 * NVAs supported DOI operations.
 */
public interface DoiClient {

    /**
     * Create a DOI where registry agency (ie datacite) auto generates the suffix under the associated customer
     * repository for NVA.
     *
     * @param customerId          NVAs customerId
     * @param metadataDataciteXml datacite schema serialized xml as string
     * @return DOI from provider in the following syntax: prefix/suffix
     * @throws ClientException Error while communicating with Registry Agency
     */
    Doi createDoi(String customerId, String metadataDataciteXml) throws ClientException;

    /**
     * Update metadata for a given DOI.
     *
     * @param customerId          NVAs customerId
     * @param doi                 DOI in the format of prefix/suffix
     * @param metadataDataciteXml datacite schema serialized xml as string
     * @throws ClientException Error while communicating with Registry Agency
     */
    void updateMetadata(String customerId, Doi doi, String metadataDataciteXml) throws ClientException;

    /**
     * Request to mark DOI as findable.
     *
     * <p>Once a DOI is findable, it cannot be deleted!
     *
     * @param customerId NVAs customerId
     * @param doi        DOI in the format of prefix/suffix
     * @param url        Location of landing page.
     * @throws ClientException Error while communicating with Registry Agency
     */
    void setLandingPage(String customerId, Doi doi, URI url) throws ClientException;

    /**
     * Delete metadata from DOI.
     *
     * <p>DOIs can exist in three states: draft, registered, and findable. DOIs are in the draft state when metadata
     * have been registered, and will transition to the findable state when registering a URL.
     *
     * <p>Findable DOIs can be transitioned to the registered state (the metadata are no longer included in the search
     * index) using this deleteMetadata command.
     *
     * @param customerId NVAs customerId
     * @param doi        DOI in the format of prefix/suffix
     * @throws ClientException Error while communicating with Registry Agency
     */
    void deleteMetadata(String customerId, Doi doi) throws ClientException;

    /**
     * Delete a DOI which is in draft.
     *
     * <p>The DOI cannot be deleted after its first promotion to findable state.
     * @param customerId NVAs customerId
     * @param doi DOI in the format of prefix/suffix
     * @throws ClientException Error while communicating with Registry Agency
     */
    void deleteDraftDoi(String customerId, Doi doi) throws ClientException;
}
