package cz.muni.fi.cpm.template.mapper;

import cz.muni.fi.cpm.template.schema.TraversalInformation;
import org.openprovenance.prov.model.Document;

public interface ITemplateProvMapper {
    /**
     * Maps a {@link TraversalInformation} object to a PROV {@link Document}.
     * <p>
     * This method gathers all relevant PROV statements from the traversal information.
     * The resulting statements are wrapped in a single named bundle, which is then
     * placed inside a PROV document with an extended namespace.
     *
     * @param ti the {@link TraversalInformation} to map
     * @return a PROV {@link Document} containing one named bundle with all mapped statements
     */
    Document toProvDocument(TraversalInformation ti);
}
