package org.commonprovenanceframework.cpm.merged;

import org.commonprovenanceframework.cpm.model.IEdge;
import org.commonprovenanceframework.cpm.model.INode;
import org.commonprovenanceframework.cpm.model.ProvUtilities2;
import org.openprovenance.prov.model.QualifiedRelation;
import org.openprovenance.prov.model.Relation;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.List;
import java.util.Objects;

public class MergedEdge implements IEdge {
    private Relation relation;
    private INode effect;
    private INode cause;

    public MergedEdge(Relation relation, INode effect, INode cause) {
        this.relation = relation;
        this.effect = effect;
        this.cause = cause;
    }

    public MergedEdge(Relation relation) {
        this.relation = relation;
    }

    @Override
    public Relation getAnyRelation() {
        return relation;
    }

    @Override
    public List<Relation> getRelations() {
        return List.of(relation);
    }

    @Override
    public Kind getKind() {
        return relation.getKind();
    }

    @Override
    public void handleDuplicate(Relation duplicateRelation) {
        if (this.relation instanceof QualifiedRelation qR1 && duplicateRelation instanceof QualifiedRelation qR2) {
            ProvUtilities2.mergeAttributes(qR1, qR2);
//        } else {
//            throw new UnsupportedOperationException(UNSUPPORTED_DUPLICATE_RELATION + ": " + duplicateRelation.getKind());
        }
    }

    @Override
    public boolean remove(Relation relation) {
        if (this.relation == relation) {
            this.relation = null;
            return true;
        }
        return false;
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
        MergedEdge edge = (MergedEdge) o;
        return Objects.equals(relation, edge.relation) &&
                Objects.equals(effect != null ? effect.getElements() : null,
                        edge.effect != null ? edge.effect.getElements() : null) &&
                Objects.equals(cause != null ? cause.getElements() : null,
                        edge.cause != null ? edge.cause.getElements() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relation,
                effect != null ? effect.getElements() : null,
                cause != null ? cause.getElements() : null
        );
    }

}
