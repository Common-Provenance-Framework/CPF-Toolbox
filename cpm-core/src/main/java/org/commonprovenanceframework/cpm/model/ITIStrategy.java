package org.commonprovenanceframework.cpm.model;

public interface ITIStrategy {

    /**
     * Determines whether the given node belongs to the traversal information part of the document.
     *
     * @param node the node to check
     * @return {@code true} if the node is part of the traversal information, {@code false} otherwise
     */
    boolean belongsToTraversalInformation(INode node);
}
