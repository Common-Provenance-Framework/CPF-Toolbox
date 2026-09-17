package org.commonprovenanceframework.cpm.divided;

import org.commonprovenanceframework.cpm.model.IEdge;
import org.commonprovenanceframework.cpm.model.INode;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DividedNode implements INode {
    final List<Element> elements;
    final List<IEdge> effectEdges;
    final List<IEdge> causeEdges;

    public DividedNode(Element element, List<IEdge> effectEdges, List<IEdge> causeEdges) {
        this.elements = new ArrayList<>(List.of(element));
        this.effectEdges = effectEdges;
        this.causeEdges = causeEdges;
    }

    public DividedNode(Element element) {
        this.elements = new ArrayList<>(List.of(element));
        this.effectEdges = new ArrayList<>();
        this.causeEdges = new ArrayList<>();
    }

    public DividedNode(List<Element> elements) {
        this.elements = new ArrayList<>(elements);
        this.effectEdges = new ArrayList<>();
        this.causeEdges = new ArrayList<>();
    }

    @Override
    public Element getAnyElement() {
        return elements.getFirst();
    }

    @Override
    public QualifiedName getId() {
        return elements.getFirst().getId();
    }

    @Override
    public Kind getKind() {
        return elements.getFirst().getKind();
    }

    @Override
    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public void handleDuplicate(Element duplicateElement) {
        elements.add(duplicateElement);
    }

    @Override
    public boolean remove(Element element) {
        return elements.removeIf(e -> element == e);
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
        DividedNode node = (DividedNode) o;
        return Objects.equals(elements, node.elements) && Objects.equals(effectEdges, node.effectEdges) && Objects.equals(causeEdges, node.causeEdges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, effectEdges, causeEdges);
    }
}
