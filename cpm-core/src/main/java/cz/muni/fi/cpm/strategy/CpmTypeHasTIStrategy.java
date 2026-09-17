package cz.muni.fi.cpm.strategy;

import java.util.List;

import org.openprovenance.prov.model.HasType;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Type;

import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.INode;
import cz.muni.fi.cpm.model.ITIStrategy;

/**
 * Strategy to determine whether a node belongs to traversal information part of
 * a document based on the CPM types present in the underlying element
 *
 * element belongs to traversal if contains exactly one valid cpm type
 * or is merged agent
 *
 * It is ok, if there are other types as well. They are ignored.
 */
public class CpmTypeHasTIStrategy implements ITIStrategy {
  @Override
  public boolean belongsToTraversalInformation(INode node) {
    if (node == null)
      return false;

    List<QualifiedName> cpmTypes = node.getElements().stream()
        .filter(HasType.class::isInstance)
        .map(HasType.class::cast)
        .flatMap(ht -> ht.getType().stream())
        .distinct()
        .map(Type::getValue)
        .filter(QualifiedName.class::isInstance)
        .map(QualifiedName.class::cast)
        .filter(qN -> CpmUtilities.belongsToCpmNs(qN)
            && CpmType.STRING_VALUES.contains(qN.getLocalPart()))
        .filter(qN -> !qN.getLocalPart().equals(CpmType.IDENTIFIER.toString()))
        .toList();

    return cpmTypes.isEmpty()
        ? false
        : cpmTypes.size() == 1
            ? true
            : cpmTypes.stream()
                .reduce(true,
                    (acc, i) -> acc && CpmType.AGENTS.contains(i.getLocalPart()),
                    Boolean::logicalAnd);

  }
}