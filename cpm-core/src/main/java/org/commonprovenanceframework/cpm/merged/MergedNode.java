package org.commonprovenanceframework.cpm.merged;

import org.commonprovenanceframework.cpm.model.IEdge;
import org.commonprovenanceframework.cpm.model.INode;
import org.commonprovenanceframework.cpm.model.ProvUtilities2;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MergedNode implements INode {
    private final List<IEdge> effectEdges;
    private final List<IEdge> causeEdges;
    private Element element;

    public MergedNode(Element element, List<IEdge> effectEdges, List<IEdge> causeEdges) {
        this.element = element;
        this.effectEdges = effectEdges;
        this.causeEdges = causeEdges;
    }

    public MergedNode(Element element) {
        this.element = element;
        this.effectEdges = new ArrayList<>();
        this.causeEdges = new ArrayList<>();
    }

    @Override
    public Element getAnyElement() {
        return element;
    }

    @Override
    public QualifiedName getId() {
        return element.getId();
    }

    @Override
    public Kind getKind() {
        return element.getKind();
    }

    @Override
    public List<Element> getElements() {
        return List.of(element);
    }

    @Override
    public void handleDuplicate(Element duplicateElement) {
        ProvUtilities2.mergeAttributes(this.element, duplicateElement);
    }

    @Override
    public boolean remove(Element element) {
        if (element == this.element) {
            this.element = null;
            return true;
        }
        return false;
    }

    @Override
    public List<IEdge> getEffectEdges() {
        return effectEdges;
    }

    @Override
    public List<IEdge> getCauseEdges() {
        return causeEdges;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MergedNode node = (MergedNode) o;
        return Objects.equals(element, node.element) && Objects.equals(effectEdges, node.effectEdges) && Objects.equals(causeEdges, node.causeEdges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, effectEdges, causeEdges);
    }
}
