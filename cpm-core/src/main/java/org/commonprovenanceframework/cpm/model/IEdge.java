package org.commonprovenanceframework.cpm.model;

import org.openprovenance.prov.model.Relation;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.List;

public interface IEdge extends Component {

    Relation getAnyRelation();

    List<Relation> getRelations();

    Kind getKind();

    /**
     * Method to handle duplicate relation of the same kind
     */
    void handleDuplicate(Relation duplicateRelation);

    boolean remove(Relation relation);

    INode getEffect();

    void setEffect(INode effect);

    INode getCause();

    void setCause(INode cause);
}
