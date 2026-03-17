package cz.muni.fi.cpm.model;

import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public abstract class CpmDocumentAdditionalTest {
    protected final ICpmFactory cF;
    protected final org.openprovenance.prov.model.ProvFactory pF;
    protected final ICpmProvFactory cPF;

    public CpmDocumentAdditionalTest(ICpmFactory cF) {
        this.cF = cF;
        pF = cF.getProvFactory();
        cPF = new CpmProvFactory(pF);
    }


    @Test
    public void getTraversalInformationPartAndDSPart_validData_returnsTiAndDs() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity1 = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity entity2 = cPF.newCpmBackwardConnector(id2);

        QualifiedName id3 = cPF.newCpmQualifiedName("qN3");
        Agent agent = cPF.getProvFactory().newAgent(id3);

        QualifiedName id4 = cPF.newCpmQualifiedName("qN4");
        Entity entity4 = cPF.newCpmBackwardConnector(id4);

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id3);

        Relation relation2 = cPF.getProvFactory().newWasDerivedFrom(id2, id4);

        Relation relation3 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id2, id3);

        Relation relation4 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr2"), id2,
                cPF.newCpmQualifiedName("nonExistent"));

        Document document = pF.newDocument();
        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        bundle.getStatement().addAll(List.of(entity1, agent, entity2, entity4));
        bundle.getStatement().addAll(List.of(relation1, relation2, relation3, relation4));
        document.setNamespace(Namespace.gatherNamespaces(document));
        bundle.setNamespace(Namespace.gatherNamespaces(document));

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        List<INode> ti = doc.getTraversalInformationPart();
        assertEquals(2, ti.size());
        assertTrue(ti.stream().anyMatch(x -> entity2.equals(x.getAnyElement())));
        assertTrue(ti.stream().anyMatch(x -> entity4.equals(x.getAnyElement())));
        INode first = ti.stream().filter(x -> Objects.equals(x.getAnyElement(), entity4)).findAny().get();
        INode last = ti.stream().filter(x -> Objects.equals(x.getAnyElement(), entity2)).findAny().get();
        assertEquals(1, first.getCauseEdges().size());
        assertEquals(0, last.getCauseEdges().size());
        assertEquals(relation2, first.getCauseEdges().getFirst().getAnyRelation());
        assertSame(last, last.getEffectEdges().getLast().getEffect());
        assertSame(first, first.getCauseEdges().getFirst().getCause());

        List<INode> ds = doc.getDomainSpecificPart();
        assertEquals(2, ds.size());
        assertTrue(ds.stream().anyMatch(x -> entity1.equals(x.getAnyElement())));
        assertTrue(ds.stream().anyMatch(x -> agent.equals(x.getAnyElement())));
        first = ds.stream().filter(x -> Objects.equals(x.getAnyElement(), entity1)).findAny().get();
        last = ds.stream().filter(x -> Objects.equals(x.getAnyElement(), agent)).findAny().get();
        assertEquals(0, first.getCauseEdges().size());
        assertEquals(1, last.getCauseEdges().size());
        assertEquals(relation1, first.getEffectEdges().getFirst().getAnyRelation());
        assertSame(first, first.getEffectEdges().getFirst().getEffect());
        assertSame(last, last.getCauseEdges().getFirst().getCause());

        List<IEdge> crossPart = doc.getCrossPartEdges();
        assertEquals(1, crossPart.size());
        assertEquals(relation3, crossPart.getFirst().getAnyRelation());
    }


    @Test
    public void getRelatedNodes_validData_returnsExpectedNodes() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity1 = cPF.newCpmForwardConnector(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity entity2 = cPF.newCpmBackwardConnector(id2);

        QualifiedName id3 = cPF.newCpmQualifiedName("qN3");
        Entity entity3 = cPF.newCpmBackwardConnector(id3);

        QualifiedName id4 = cPF.newCpmQualifiedName("qN4");
        Entity entity4 = cPF.newCpmBackwardConnector(id4);

        Relation relation1 = cPF.getProvFactory().newWasDerivedFrom(id4, id3);
        Relation relation2 = cPF.getProvFactory().newWasDerivedFrom(id3, id2);
        Relation relation3 = cPF.getProvFactory().newWasDerivedFrom(id2, id1);

        Document document = pF.newDocument();
        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        bundle.getStatement().addAll(List.of(entity1, entity2, entity3, entity4));
        bundle.getStatement().addAll(List.of(relation1, relation2, relation3));
        document.setNamespace(Namespace.gatherNamespaces(document));
        bundle.setNamespace(Namespace.gatherNamespaces(document));

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        List<INode> precursors = doc.getPrecursors(id1);
        assertEquals(3, precursors.size());
        assertTrue(precursors.stream().anyMatch(x -> entity2.equals(x.getAnyElement())));
        assertTrue(precursors.stream().anyMatch(x -> entity3.equals(x.getAnyElement())));
        assertTrue(precursors.stream().anyMatch(x -> entity4.equals(x.getAnyElement())));

        assertEquals(0, doc.getPrecursors(id4).size());

        List<INode> successors = doc.getSuccessors(id4);
        assertEquals(3, successors.size());
        assertTrue(successors.stream().anyMatch(x -> entity1.equals(x.getAnyElement())));
        assertTrue(successors.stream().anyMatch(x -> entity2.equals(x.getAnyElement())));
        assertTrue(successors.stream().anyMatch(x -> entity3.equals(x.getAnyElement())));

        assertEquals(0, doc.getSuccessors(id1).size());
    }


    @Test
    public void getRelatedNodes_invalidAndNullData_returnsNull() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Agent agent = cPF.newCpmAgent(id1, CpmType.SENDER_AGENT, new ArrayList<>());

        Document document = pF.newDocument();
        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        bundle.getStatement().add(agent);
        document.setNamespace(Namespace.gatherNamespaces(document));
        bundle.setNamespace(Namespace.gatherNamespaces(document));

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        List<INode> precursors = doc.getPrecursors(id1);
        assertNull(precursors);
        assertNull(doc.getSuccessors(null));
    }

}