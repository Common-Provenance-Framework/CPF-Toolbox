package org.commonprovenanceframework.cpm.strategy;

import org.commonprovenanceframework.cpm.constants.CpmType;
import org.commonprovenanceframework.cpm.constants.DctAttributeConstants;
import org.commonprovenanceframework.cpm.constants.DctNamespaceConstants;
import org.commonprovenanceframework.cpm.model.CpmUtilities;
import org.commonprovenanceframework.cpm.model.IEdge;
import org.commonprovenanceframework.cpm.model.INode;
import org.commonprovenanceframework.cpm.model.ITIStrategy;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.List;

import static org.commonprovenanceframework.cpm.model.CpmUtilities.hasCpmType;
import static org.commonprovenanceframework.cpm.model.CpmUtilities.hasValidCpmType;

/**
 * Strategy to determine whether a node belongs to traversal information part of
 * a document based on the attributes
 * present in the underlying element
 */
public class AttributeTIStrategy implements ITIStrategy {
  private static boolean hasNonTIAttributes(INode node) {
    if (node == null)
      return true;

    return !node.getElements().stream().allMatch(element -> element != null
        && element.getLocation().isEmpty()
        && element.getLabel().isEmpty()
        && (element.getType().isEmpty() // !!! this is redundant, because if the type is empty, the allMatch will return true !!!
            || element.getType().stream().allMatch(t -> t.getValue() instanceof QualifiedName qN
                && CpmUtilities.belongsToCpmNs(qN)))
        && element.getOther().stream().allMatch(x -> CpmUtilities.belongsToCpmNs(x.getElementName())
            || (x.getElementName() instanceof QualifiedName qN2
                && DctNamespaceConstants.DCT_PREFIX.equals(qN2.getPrefix())
                && DctNamespaceConstants.DCT_NS.equals(qN2.getNamespaceURI())
                && DctAttributeConstants.HAS_PART.equals(qN2.getLocalPart()))));
  }

  @Override
  public boolean belongsToTraversalInformation(INode node) {
    if (node == null)
      return false;
    if (hasNonTIAttributes(node))
      return false;

    boolean hasNodeValidCpmType = hasValidCpmType(node);

    List<INode> generalNodes = node.getEffectEdges().stream()
        .filter(x -> Kind.PROV_SPECIALIZATION.equals(x.getKind()))
        .map(IEdge::getCause).toList();

    if (generalNodes.isEmpty() && hasNodeValidCpmType)
      return true;
    if (generalNodes.size() != 1)
      return false;

    INode generalNode = generalNodes.getFirst();
    if (hasNonTIAttributes(generalNode))
      return false;
    return (hasCpmType(generalNode, CpmType.FORWARD_CONNECTOR)
        || hasCpmType(generalNode, CpmType.SPEC_FORWARD_CONNECTOR))
        && (!hasNodeValidCpmType // ??? if the node itself does not have a valid CPM type, it can be considered as TI if its generalization is a forward connector ???
            || hasCpmType(node, CpmType.FORWARD_CONNECTOR) // ??? Maybe this is not TRUE -> if the node itself has a valid CPM type, it can be considered as TI if it is a forward connector ???
            || hasCpmType(node, CpmType.SPEC_FORWARD_CONNECTOR)); // if node itself is a valid CPM type (specForwardConnector), it can be considered as TI if specialization of a forwardConnector
  }
}