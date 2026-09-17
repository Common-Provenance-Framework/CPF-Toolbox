package org.commonprovenanceframework.cpm.template.deserialization;

import org.commonprovenanceframework.cpm.template.schema.TraversalInformation;
import org.openprovenance.prov.model.Document;

import java.io.IOException;
import java.io.InputStream;

public interface ITraversalInformationDeserializer {
    /**
     * Deserializes a JSON stream into a {@link TraversalInformation} object.
     *
     * @param in the input stream containing JSON data
     * @return the deserialized {@link TraversalInformation} object
     * @throws IOException if an I/O or parsing error occurs
     */
    TraversalInformation deserializeTI(InputStream in) throws IOException;

    /**
     * Deserializes a JSON stream into a PROV {@link Document}, by first converting
     * the JSON to {@link TraversalInformation} and then mapping it to a PROV document.
     *
     * @param in the input stream containing JSON data
     * @return the resulting PROV {@link Document}
     * @throws IOException if an I/O or parsing error occurs
     */
    Document deserializeDocument(InputStream in) throws IOException;
}
