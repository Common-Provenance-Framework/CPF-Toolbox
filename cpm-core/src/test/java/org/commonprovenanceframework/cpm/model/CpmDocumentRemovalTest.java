package org.commonprovenanceframework.cpm.model;

import org.commonprovenanceframework.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class CpmDocumentRemovalTest {
    protected final ICpmFactory cF;
    protected final org.openprovenance.prov.model.ProvFactory pF;
    protected final ICpmProvFactory cPF;

    public CpmDocumentRemovalTest(ICpmFactory cF) {
        this.cF = cF;
        pF = cF.getProvFactory();
        cPF = new CpmProvFactory(pF);
    }


    @Test
    public void removeNode_nodeWithRelations_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent = cPF.getProvFactory().newAgent(id2);

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, relation1), List.of(), bundleId, pF, cPF, cF);

        assertTrue(doc.removeNode(id1));
        assertTrue(doc.getNodes(id1).isEmpty());
        assertFalse(doc.areAllRelationsMapped());
        assertNull(doc.getEdge(id1, id2).getEffect());
        assertEquals(1, doc.getDomainSpecificPart().size());

        assertTrue(doc.removeNode(id2));
        assertTrue(doc.getNodes(id2).isEmpty());
        assertFalse(doc.areAllRelationsMapped());
        assertNull(doc.getEdge(id1, id2).getCause());
        assertEquals(0, doc.getDomainSpecificPart().size());

        doc.doAction(entity);
        doc.doAction(agent);
        assertTrue(doc.areAllRelationsMapped());
        assertNotNull(doc.getEdge(id1, id2).getEffect());
        assertEquals(entity, doc.getEdge(id1, id2).getEffect().getAnyElement());
        assertNotNull(doc.getEdge(id1, id2).getCause());
        assertEquals(agent, doc.getEdge(id1, id2).getCause().getAnyElement());
    }


    @Test
    public void removeNode_nodesWithSameId_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent2 = cPF.getProvFactory().newAgent(id2);

        Agent agent = cPF.getProvFactory().newAgent(id1);

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, agent2, relation1), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.removeNode(id1));
        assertTrue(doc.removeNode(id1, Kind.PROV_ENTITY));
        assertEquals(1, doc.getNodes(id1).size());
        assertFalse(doc.areAllRelationsMapped());
        assertNull(doc.getEdge(id1, id2).getEffect());
        assertEquals(2, doc.getDomainSpecificPart().size());
    }

    @Test
    public void removeNodes_nodesWithSameId_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent2 = cPF.getProvFactory().newAgent(id2);

        Agent agent = cPF.getProvFactory().newAgent(id1);

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, agent2, relation1), List.of(), bundleId, pF, cPF, cF);

        assertTrue(doc.removeNodes(id1));
        assertTrue(doc.getNodes(id1).isEmpty());
        assertFalse(doc.areAllRelationsMapped());
        assertNull(doc.getEdge(id1, id2).getEffect());
        assertEquals(1, doc.getDomainSpecificPart().size());
    }


    @Test
    public void removeElement_nodeWithRelations_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent = cPF.getProvFactory().newAgent(id2);

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, relation1), List.of(), bundleId, pF, cPF, cF);

        assertTrue(doc.removeElement(doc.getNode(id1).getAnyElement()));
        assertTrue(doc.getNodes(id1).isEmpty());
        assertFalse(doc.areAllRelationsMapped());
        assertNull(doc.getEdge(id1, id2).getEffect());
        assertEquals(1, doc.getDomainSpecificPart().size());
    }

    @Test
    public void removeNode_duplicateId_returnsFalse() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity1 = cPF.getProvFactory().newEntity(id1);

        Agent agent = cPF.getProvFactory().newAgent(id1);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity1, agent), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.removeNode(id1));
    }

    @Test
    public void removeNode_nonExistentId_returnsFalse() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity1 = cPF.getProvFactory().newEntity(id1);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity1), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.removeNode(cPF.newCpmQualifiedName("nonExistentId")));
    }


    @Test
    public void removeEdges_mappedRelation_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent = cPF.getProvFactory().newAgent(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasAttributedTo relation1 = cPF.getProvFactory().newWasAttributedTo(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, relation1), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.removeEdge(rel, Kind.PROV_ASSOCIATION));
        assertTrue(doc.removeEdge(rel, Kind.PROV_ATTRIBUTION));
        assertNull(doc.getEdge(rel));
        assertNull(doc.getEdge(id1, id2));
        assertTrue(doc.getNode(id1).getCauseEdges().isEmpty());
        assertTrue(doc.getNode(id1).getEffectEdges().isEmpty());
        assertTrue(doc.areAllRelationsMapped());

        doc.doAction(relation1);
        assertTrue(doc.areAllRelationsMapped());
        assertNotNull(doc.getEdge(id1, id2));
        assertNotNull(doc.getEdge(rel));
    }

    @Test
    public void removeEdges_unmappedRelationCause_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasAttributedTo relation1 = cPF.getProvFactory().newWasAttributedTo(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, relation1), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.areAllRelationsMapped());
        assertFalse(doc.removeEdge(id1, id2, Kind.PROV_SPECIALIZATION));
        assertTrue(doc.removeEdge(id1, id2));
        assertNull(doc.getEdge(rel));
        assertNull(doc.getEdge(id1, id2));
        assertTrue(doc.getNode(id1).getCauseEdges().isEmpty());
        assertTrue(doc.getNode(id1).getEffectEdges().isEmpty());
        assertTrue(doc.areAllRelationsMapped());

        Agent agent = cPF.getProvFactory().newAgent(id2);
        doc.doAction(agent);
        assertTrue(doc.areAllRelationsMapped());
        assertTrue(doc.getNode(id2).getCauseEdges().isEmpty());
    }


    @Test
    public void removeEdges_unmappedRelationEffect_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent = cPF.getProvFactory().newAgent(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasAttributedTo relation1 = cPF.getProvFactory().newWasAttributedTo(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(agent, relation1), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.areAllRelationsMapped());
        assertFalse(doc.removeEdges(rel, Kind.PROV_ASSOCIATION));
        assertTrue(doc.removeEdges(rel, Kind.PROV_ATTRIBUTION));
        assertNull(doc.getEdge(rel));
        assertNull(doc.getEdge(id1, id2));
        assertTrue(doc.areAllRelationsMapped());

        Entity entity = cPF.getProvFactory().newEntity(id1);
        doc.doAction(entity);
        assertTrue(doc.areAllRelationsMapped());
        assertTrue(doc.getNode(id2).getEffectEdges().isEmpty());
    }


    @Test
    public void removeEdges_nonExistentId_returnsFalse() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasAttributedTo relation1 = cPF.getProvFactory().newWasAttributedTo(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(relation1), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.removeEdges(cPF.newCpmQualifiedName("nonExistentId")));
        assertFalse(doc.removeEdges(cPF.newCpmQualifiedName("nonExistentId1"), cPF.newCpmQualifiedName("nonExistentId2")));
    }

    @Test
    public void removeInfluence_removeThenAddCauseAndEffect_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(inf), List.of(), bundleId, pF, cPF, cF);

        assertTrue(doc.removeEdges(rel));

        doc.doAction(cause);
        assertTrue(doc.getNode(id1).getCauseEdges().isEmpty());
        assertTrue(doc.getEdges(id1, id2, Kind.PROV_INFLUENCE).isEmpty());

        doc.doAction(effect);
        assertTrue(doc.getNode(id2).getEffectEdges().isEmpty());
        assertNull(doc.getEdge(id1, id2));
    }

    @Test
    public void removeRelation_relationWithNodes_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent = cPF.getProvFactory().newAgent(id2);

        QualifiedName relId = cPF.newCpmQualifiedName("attr");
        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(relId, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, relation1), List.of(), bundleId, pF, cPF, cF);

        assertTrue(doc.removeRelation(doc.getEdge(relId).getAnyRelation()));
        assertTrue(doc.getEdges(relId).isEmpty());
        assertTrue(doc.areAllRelationsMapped());
        assertTrue(doc.getNode(id1).getCauseEdges().isEmpty());
        assertTrue(doc.getNode(id2).getEffectEdges().isEmpty());
        assertEquals(2, doc.getDomainSpecificPart().size());
    }


    @Test
    public void removeRelation_hadMember_returnsTrue() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName collectionId = pF.newQualifiedName("uri", "collection", "ex");
        Entity collection = pF.newEntity(collectionId);
        bundle.getStatement().add(collection);

        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity", "ex");
        Entity entity1 = pF.newEntity(entityId1);
        bundle.getStatement().add(entity1);

        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");
        Entity entity2 = pF.newEntity(entityId2);
        bundle.getStatement().add(entity2);

        HadMember hadMember = pF.newHadMember(collectionId, entityId1, entityId2);
        bundle.getStatement().add(hadMember);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertTrue(doc.removeRelation(doc.getEdge(collectionId, entityId1).getAnyRelation()));
        assertNull(doc.getEdge(collectionId, entityId1));
        assertNull(doc.getEdge(collectionId, entityId2));
    }

}
