package org.commonprovenanceframework.cpm.template.serialization;

import org.commonprovenanceframework.cpm.template.schema.TraversalInformation;

import java.io.File;
import java.io.IOException;

public interface ITraversalInformationSerializer {
    /**
     * Serializes a {@link TraversalInformation} object to a JSON file.
     *
     * @param ti   the {@link TraversalInformation} to serialize
     * @param file the destination {@link File}
     * @throws IOException if writing to the file fails
     */
    void serializeTI(TraversalInformation ti, File file) throws IOException;

}