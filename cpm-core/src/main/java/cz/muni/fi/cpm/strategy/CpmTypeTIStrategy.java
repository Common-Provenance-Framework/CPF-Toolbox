package cz.muni.fi.cpm.strategy;

import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.INode;
import cz.muni.fi.cpm.model.ITIStrategy;

/**
 * Strategy to determine whether a node belongs to traversal information part of
 * a document based on the CPM types
 * present in the underlying element
 */
public class CpmTypeTIStrategy implements ITIStrategy {
  @Override
  public boolean belongsToTraversalInformation(INode node) {
    return CpmUtilities.hasValidCpmType(node);
  }
}