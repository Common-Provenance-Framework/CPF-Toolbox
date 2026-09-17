package org.commonprovenanceframework.cpm.strategy;

import org.commonprovenanceframework.cpm.model.CpmUtilities;
import org.commonprovenanceframework.cpm.model.INode;
import org.commonprovenanceframework.cpm.model.ITIStrategy;

/**
 * Strategy to determine whether a node belongs to traversal information part of
 * a document based on the CPM types present in the underlying element
 *
 * element belongs to traversal if have only one type and is valid cpm type
 *
 * It is not traversal element, if there are other types as well.
 */
public class CpmTypeTIStrategy implements ITIStrategy {
  @Override
  public boolean belongsToTraversalInformation(INode node) {
    return CpmUtilities.hasValidCpmType(node);
  }
}