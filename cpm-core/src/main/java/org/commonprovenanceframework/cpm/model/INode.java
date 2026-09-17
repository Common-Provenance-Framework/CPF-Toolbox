package org.commonprovenanceframework.cpm.model;

import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.ArrayList;
import java.util.List;

public interface INode extends Component {
    Element getAnyElement();

    QualifiedName getId();

    Kind getKind();

    /**
     * Retrieves all elements within node
     *
     * @return a list of all elements within node
     */
    List<Element> getElements();


    /**
     * Method to handle duplicate element of the same kind with the same id
     */
    void handleDuplicate(Element duplicateElement);

    /**
     * Method to remove element from the node
     */
    boolean remove(Element element);

    /**
     * Retrieves all edges where the current node acts as the effect.
     *
     * @return a list of edges where this node is the effect node
     */
    List<IEdge> getEffectEdges();

    /**
     * Retrieves all edges where the current node acts as the cause.
     *
     * @return a list of edges where this node is the cause node
     */
    List<IEdge> getCauseEdges();

    /**
     * Retrieves all edges connected to the current node,
     * including edges where the node is an effect or a cause.
     *
     * @return a list of all edges connected to this node
     */
    default List<IEdge> getAllEdges() {
        List<IEdge> allEdges = new ArrayList<>();
        allEdges.addAll(getEffectEdges());
        allEdges.addAll(getCauseEdges());
        return allEdges;
    }
}
