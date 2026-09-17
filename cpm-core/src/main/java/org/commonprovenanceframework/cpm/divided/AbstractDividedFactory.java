package org.commonprovenanceframework.cpm.divided;

import org.commonprovenanceframework.cpm.model.ICpmFactory;
import org.commonprovenanceframework.cpm.model.IEdge;
import org.commonprovenanceframework.cpm.model.INode;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.Relation;

public abstract class AbstractDividedFactory implements ICpmFactory {

    protected final ProvFactory pF;

    protected AbstractDividedFactory(ProvFactory pF) {
        this.pF = pF;
    }

    @Override
    public ProvFactory getProvFactory() {
        return pF;
    }

    @Override
    public IEdge newEdge(Relation relation) {
        Relation clonedRelation = pF.newStatement(relation);
        return processEdge(new DividedEdge(clonedRelation));
    }

    @Override
    public IEdge newEdgeWithoutCloning(Relation relation) {
        return processEdge(new DividedEdge(relation));
    }

    @Override
    public IEdge newEdge(IEdge edge) {
        return processEdge(new DividedEdge(edge.getRelations().stream().map(pF::newStatement).toList()));
    }

    @Override
    public IEdge newEdgeWithoutCloning(IEdge edge) {
        return processEdge(new DividedEdge(edge.getRelations()));
    }

    @Override
    public INode newNode(Element element) {
        Element clonedElement = pF.newStatement(element);
        return processNode(new DividedNode(clonedElement));
    }

    public INode newNode(INode node) {
        return processNode(new DividedNode(node.getElements().stream().map(pF::newStatement).toList()));
    }

    @Override
    public INode newNodeWithoutCloning(Element element) {
        return processNode(new DividedNode(element));
    }

    protected abstract IEdge processEdge(IEdge edge);

    protected abstract INode processNode(INode node);
}
