package cz.muni.fi.cpm.vanilla;

import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.merged.CpmMergedFactory;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.model.IEdge;
import cz.muni.fi.cpm.model.INode;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Relation;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CpmFactoryTest {
    private final ICpmFactory cF;

    private final ICpmProvFactory cPF;

    public CpmFactoryTest() {
        this.cF = new CpmMergedFactory();
        this.cPF = new CpmProvFactory();
    }


    @Test
    public void newNode_copyNode_returnsExpectedNode() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.newCpmBackwardConnector(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity entity2 = cPF.newCpmBackwardConnector(id1);

        QualifiedName id3 = cPF.newCpmQualifiedName("qN3");
        Entity entity3 = cPF.getProvFactory().newEntity(id3);

        Relation relation = cPF.getProvFactory().newWasDerivedFrom(id1, id2);

        INode node = cF.newNode(entity);
        INode node2 = cF.newNode(entity2);

        IEdge edge = cF.newEdge(relation);
        edge.setCause(node);
        edge.setEffect(node2);

        node.getCauseEdges().add(edge);

        Relation relation2 = cPF.getProvFactory().newWasDerivedFrom(id1, id3);
        INode node3 = cF.newNode(entity3);

        IEdge edge2 = cF.newEdge(relation2);
        edge2.setCause(node);
        edge2.setEffect(node3);

        node.getCauseEdges().add(edge2);

        INode output = cF.newNode(node);

        assertNotNull(output);
        assertEquals(0, output.getCauseEdges().size());
        assertEquals(0, output.getEffectEdges().size());
    }
}
