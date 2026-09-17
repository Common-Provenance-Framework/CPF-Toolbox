package org.commonprovenanceframework.cpm.divided;

import org.commonprovenanceframework.cpm.model.IEdge;
import org.commonprovenanceframework.cpm.model.INode;
import org.openprovenance.prov.model.Relation;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DividedEdge implements IEdge {
    private final List<Relation> relations;
    private INode effect;
    private INode cause;

    public DividedEdge(Relation relation, INode effect, INode cause) {
        this.relations = new ArrayList<>(List.of(relation));
        this.effect = effect;
        this.cause = cause;
    }

    public DividedEdge(Relation relation) {
        this.relations = new ArrayList<>(List.of(relation));
    }

    public DividedEdge(List<Relation> relations) {
        this.relations = new ArrayList<>(relations);
    }

    @Override
    public Relation getAnyRelation() {
        return relations.getFirst();
    }

    @Override
    public List<Relation> getRelations() {
        return Collections.unmodifiableList(relations);
    }

    @Override
    public Kind getKind() {
        return relations.getFirst().getKind();
    }

    @Override
    public void handleDuplicate(Relation duplicateRelation) {
        relations.add(duplicateRelation);
    }

    @Override
    public boolean remove(Relation relation) {
        return relations.removeIf(r -> r == relation);
    }

    @Override
    public INode getEffect() {
        return effect;
    }

    @Override
    public void setEffect(INode effect) {
        this.effect = effect;
    }

    @Override
    public INode getCause() {
        return cause;
    }

    @Override
    public void setCause(INode cause) {
        this.cause = cause;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DividedEdge edge = (DividedEdge) o;
        return Objects.equals(relations, edge.relations) &&
                Objects.equals(effect != null ? effect.getElements() : null,
                        edge.effect != null ? edge.effect.getElements() : null) &&
                Objects.equals(cause != null ? cause.getElements() : null,
                        edge.cause != null ? edge.cause.getElements() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relations,
                effect != null ? effect.getElements() : null,
                cause != null ? cause.getElements() : null
        );
    }

}
