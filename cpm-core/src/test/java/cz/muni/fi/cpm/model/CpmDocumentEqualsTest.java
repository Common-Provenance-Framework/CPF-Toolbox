package cz.muni.fi.cpm.model;

import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public abstract class CpmDocumentEqualsTest {
    protected final ICpmFactory cF;
    protected final org.openprovenance.prov.model.ProvFactory pF;
    protected final ICpmProvFactory cPF;

    public CpmDocumentEqualsTest(ICpmFactory cF) {
        this.cF = cF;
        pF = cF.getProvFactory();
        cPF = new CpmProvFactory(pF);
    }


    @Test
    public void equals_graphAndStatementsAndDocument_returnsTrue() {
        QualifiedName id1Stat = cPF.newCpmQualifiedName("qN1");
        Entity entity1Stat = cPF.getProvFactory().newEntity(id1Stat);

        QualifiedName id2Stat = cPF.newCpmQualifiedName("qN2");
        Entity entity2Stat = cPF.newCpmBackwardConnector(id2Stat);

        QualifiedName id3Stat = cPF.newCpmQualifiedName("qN3");
        Agent agentStat = cPF.getProvFactory().newAgent(id3Stat);

        QualifiedName id4Stat = cPF.newCpmQualifiedName("qN4");
        Entity entity4Stat = cPF.newCpmBackwardConnector(id4Stat);

        Relation relation1Stat = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1Stat, id3Stat);

        Relation relation2Stat = cPF.getProvFactory().newWasDerivedFrom(id2Stat, id4Stat);

        Relation relation3Stat = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id2Stat, id3Stat);

        QualifiedName bundleIdStat = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument docStat = new CpmDocument(List.of(entity2Stat, entity4Stat, relation2Stat), List.of(entity1Stat, agentStat, relation1Stat), List.of(relation3Stat), bundleIdStat, pF, cPF, cF);


        QualifiedName id1Graph = cPF.newCpmQualifiedName("qN1");
        Entity entity1Graph = cPF.getProvFactory().newEntity(id1Graph);
        INode node1Graph = cF.newNode(entity1Graph);

        QualifiedName id2Graph = cPF.newCpmQualifiedName("qN2");
        Entity entity2Graph = cPF.newCpmBackwardConnector(id2Graph);
        INode node2Graph = cF.newNode(entity2Graph);

        QualifiedName id3Graph = cPF.newCpmQualifiedName("qN3");
        Agent agentGraph = cPF.getProvFactory().newAgent(id3Graph);
        INode node3Graph = cF.newNode(agentGraph);

        QualifiedName id4Graph = cPF.newCpmQualifiedName("qN4");
        Entity entity4Graph = cPF.newCpmBackwardConnector(id4Graph);
        INode node4Graph = cF.newNode(entity4Graph);

        Relation relation1Graph = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1Graph, id3Graph);
        IEdge edge1Graph = cF.newEdge(relation1Graph);
        edge1Graph.setEffect(node1Graph);
        edge1Graph.setCause(node3Graph);
        node1Graph.getEffectEdges().add(edge1Graph);
        node3Graph.getCauseEdges().add(edge1Graph);

        Relation relation2Graph = cPF.getProvFactory().newWasDerivedFrom(id2Graph, id4Graph);
        IEdge edge2 = cF.newEdge(relation2Graph);
        edge2.setEffect(node2Graph);
        edge2.setCause(node4Graph);
        node2Graph.getEffectEdges().add(edge2);
        node4Graph.getCauseEdges().add(edge2);

        Relation relation3Graph = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id2Graph, id3Graph);
        IEdge edge3 = cF.newEdge(relation3Graph);
        edge3.setEffect(node2Graph);
        edge3.setCause(node3Graph);
        node2Graph.getEffectEdges().add(edge3);
        node3Graph.getCauseEdges().add(edge3);

        QualifiedName bundleIdGraph = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument docGraph = new CpmDocument(List.of(entity2Graph, entity4Graph, relation2Graph), List.of(entity1Graph, agentGraph, relation1Graph), List.of(relation3Graph), bundleIdGraph, pF, cPF, cF);

        QualifiedName id1Doc = cPF.newCpmQualifiedName("qN1");
        Entity entity1Doc = cPF.getProvFactory().newEntity(id1Doc);

        QualifiedName id2Doc = cPF.newCpmQualifiedName("qN2");
        Entity entity2Doc = cPF.newCpmBackwardConnector(id2Doc);

        QualifiedName id3Doc = cPF.newCpmQualifiedName("qN3");
        Agent agentDoc = cPF.getProvFactory().newAgent(id3Doc);

        QualifiedName id4Doc = cPF.newCpmQualifiedName("qN4");
        Entity entity4Doc = cPF.newCpmBackwardConnector(id4Doc);

        Relation relation1Doc = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1Doc, id3Doc);

        Relation relation2Doc = cPF.getProvFactory().newWasDerivedFrom(id2Doc, id4Doc);

        Relation relation3Doc = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id2Doc, id3Doc);

        Document document = pF.newDocument();
        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        bundle.getStatement().addAll(List.of(entity1Doc, agentDoc, entity2Doc, entity4Doc));
        bundle.getStatement().addAll(List.of(relation1Doc, relation2Doc, relation3Doc));
        document.setNamespace(Namespace.gatherNamespaces(document));
        bundle.setNamespace(Namespace.gatherNamespaces(document));

        CpmDocument docDoc = new CpmDocument(document, pF, cPF, cF);

        assertEquals(docDoc, docGraph);
        assertEquals(docStat, docGraph);
        assertEquals(docStat, docDoc);
    }

    @Test
    public void equals_unmappedRelations_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");

        QualifiedName id3 = cPF.newCpmQualifiedName("qN3");

        QualifiedName id4 = cPF.newCpmQualifiedName("qN4");

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id3);

        Relation relation2 = cPF.getProvFactory().newWasDerivedFrom(id2, id4);
        Document document = pF.newDocument();
        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);
        bundle.getStatement().addAll(List.of(relation1, relation2));
        CpmDocument doc1 = new CpmDocument(document, pF, cPF, cF);

        Document document2 = pF.newDocument();
        Bundle bundle2 = pF.newNamedBundle(id, new ArrayList<>());
        document2.getStatementOrBundle().add(bundle2);
        CpmDocument doc2 = new CpmDocument(document2, pF, cPF, cF);
        bundle2.getStatement().add(relation1);

        assertNotEquals(doc1, doc2);
    }

}
