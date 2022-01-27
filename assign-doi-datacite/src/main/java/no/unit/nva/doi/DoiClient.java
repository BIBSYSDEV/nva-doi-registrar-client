package no.unit.nva.doi;

import java.net.URI;
import no.unit.nva.doi.datacite.clients.DataCiteClient;
import no.unit.nva.doi.datacite.clients.exception.ClientException;
import no.unit.nva.doi.datacite.restclient.models.DoiStateDto;
import no.unit.nva.doi.models.Doi;

/**
 * NVAs supported DOI operations.
 *
 * <p>DOIs can exist in three states: draft, de-listed, and findable. DOIs are in the draft state when metadata
 * have been registered, and will transition to the findable state when registering a landing page (URL).
 *
 * @see <a href="https://support.datacite.org/docs/landing-pages">Best practices for landing pages</a>
 * @see <a href="https://support.datacite.org/docs/tombstone-pages">Best practices for Tombstone Landing pages</a>
 * @see <a href="https://support.datacite.org/docs/versioning">Best practices about versioning</a>
 * @see DataCiteClient
 */
public interface DoiClient {

    /**
     * Create a DOI with metadata and an auto-generated suffix in the specified customer repository.
     *
     * <p>The specified customer repository are configured by the NVA application administrators. Each customer has
     * their own repository with associated prefix that will be used for NVA.
     *
     * @param customerId NVAs customerId
     * @return {@link Doi} containing prefix/suffix ({@link Doi#toIdentifier()}) from provider
     * @throws ClientException Error while communicating with Registry Agency
     */
    Doi createDoi(URI customerId) throws ClientException;

    /**
     * Update metadata for a DOI.
     *
     * @param customerId          NVAs customerId
     * @param metadataDataCiteXml datacite schema serialized xml as string
     * @param doi                 {@link Doi} containing prefix/suffix ({@link Doi#toIdentifier()})
     * @throws ClientException Error while communicating with Registry Agency
     * @see <a href="https://support.datacite.org/docs/metadata-quality">Metadata quality requirements</a>
     * @see <a href="https://support.datacite.org/docs/connecting-research-outputs">Connect Reseearch Outputs
     *     (SCHOLIX)</a>
     */
    void updateMetadata(URI customerId, Doi doi, String metadataDataCiteXml) throws ClientException;

    /**
     * Set landing page for a Doi. This will also turns the DOI into findable state!
     *
     * <p>Once a DOI is findable, it cannot be deleted!
     *
     * @param customerId NVAs customerId
     * @param url        Location of landing page.
     * @param doi        {@link Doi} containing prefix/suffix ({@link Doi#toIdentifier()})
     * @throws ClientException Error while communicating with Registry Agency
     * @see <a href="https://support.datacite.org/docs/datacite-doi-display-guidelines">DOI display guidelines.</a>
     */
    void setLandingPage(URI customerId, Doi doi, URI url) throws ClientException;

    /**
     * Delete metadata from DOI.
     *
     * <p>Findable DOIs can be transitioned to the registered state (the metadata are no longer included in the search
     * index) using this ${@link #deleteMetadata(URI, Doi)}.
     *
     * <p>Notice: Updating metadata for a DOI that has been in deleted state (registered) will turn it into findable
     * state.
     *
     * @param customerId NVAs customerId
     * @param doi        {@link Doi} containing prefix/suffix ({@link Doi#toIdentifier()})
     * @throws ClientException Error while communicating with Registry Agency
     */
    void deleteMetadata(URI customerId, Doi doi) throws ClientException;

    /**
     * Delete a DOI which is in draft.
     *
     * <p>The DOI cannot be deleted after its first promotion to findable state.
     *
     * @param customerId NVAs customerId
     * @param doi        {@link Doi} containing prefix/suffix ({@link Doi#toIdentifier()})
     * @throws ClientException Error while communicating with Registry Agency
     */
    void deleteDraftDoi(URI customerId, Doi doi) throws ClientException;

    DoiStateDto getDoi(URI customerId, Doi doi) throws ClientException;
}
