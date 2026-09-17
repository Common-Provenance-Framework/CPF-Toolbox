package org.commonprovenanceframework.cpm.model;

import org.commonprovenanceframework.cpm.constants.CpmType;
import org.commonprovenanceframework.cpm.exception.NoSpecificKind;
import org.commonprovenanceframework.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class CpmDocumentModificationTest {
    protected final ICpmFactory cF;
    protected final org.openprovenance.prov.model.ProvFactory pF;
    protected final ICpmProvFactory cPF;
    protected final ProvUtilities u;

    public CpmDocumentModificationTest(ICpmFactory cF) {
        this.cF = cF;
        pF = cF.getProvFactory();
        u = new ProvUtilities();
        cPF = new CpmProvFactory(pF);
    }

    @Test
    public void setBundleId_validId_renamesBundle() {
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
        QualifiedName newId = pF.newQualifiedName("uri", "newName", "ex");
        doc.setBundleId(newId);
        Document output = doc.toDocument();
        assertEquals(newId, ((Bundle) output.getStatementOrBundle().getFirst()).getId());
    }


    @Test
    public void setBundleId_nullId_returnsNull() {
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
        doc.setBundleId(null);
        Document output = doc.toDocument();
        assertNull(((Bundle) output.getStatementOrBundle().getFirst()).getId());
    }


    @Test
    public void setNodeIdentifier_nodeWithRelations_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent = cPF.getProvFactory().newAgent(id2);

        QualifiedName newId1 = cPF.newCpmQualifiedName("newQN1");

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);
        Relation relation2 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), newId1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, relation1, relation2), List.of(), bundleId, pF, cPF, cF);

        assertTrue(doc.setNewNodeIdentifier(id1, Kind.PROV_ENTITY, newId1));

        assertTrue(doc.getNodes(id1).isEmpty());
        assertFalse(doc.areAllRelationsMapped());
        assertNull(doc.getEdge(id1, id2).getEffect());

        assertNotNull(doc.getNode(newId1));
        assertNotNull(doc.getEdge(newId1, id2).getEffect());
        assertEquals(2, doc.getDomainSpecificPart().size());

        doc.doAction(entity);
        assertTrue(doc.areAllRelationsMapped());
        assertNotNull(doc.getEdge(id1, id2).getEffect());
        assertEquals(entity, doc.getEdge(id1, id2).getEffect().getAnyElement());
        assertNotNull(doc.getEdge(id1, id2).getCause());
        assertEquals(agent, doc.getEdge(id1, id2).getCause().getAnyElement());
        assertEquals(3, doc.getDomainSpecificPart().size());
    }


    @Test
    public void setElementIdentifier_nodeWithRelations_returnsTrue() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Agent agent = cPF.getProvFactory().newAgent(id2);

        QualifiedName newId1 = cPF.newCpmQualifiedName("newQN1");

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);
        Relation relation2 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), newId1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, relation1, relation2), List.of(), bundleId, pF, cPF, cF);

        assertTrue(doc.setNewElementIdentifier(doc.getNode(id1).getAnyElement(), newId1));

        assertTrue(doc.getNodes(id1).isEmpty());
        assertFalse(doc.areAllRelationsMapped());
        assertNull(doc.getEdge(id1, id2).getEffect());

        assertNotNull(doc.getNode(newId1));
        assertNotNull(doc.getEdge(newId1, id2).getEffect());
        assertEquals(2, doc.getDomainSpecificPart().size());
    }


    private Element getElement(Kind kind, QualifiedName id) {
        if (Kind.PROV_ENTITY == kind) {
            return pF.newEntity(id);
        } else if (Kind.PROV_AGENT == kind) {
            return pF.newAgent(id);
        } else if (Kind.PROV_ACTIVITY == kind) {
            return pF.newActivity(id);
        }
        throw new UnsupportedOperationException();
    }

    @ParameterizedTest
    @MethodSource("org.commonprovenanceframework.cpm.RelationProvider#provideRelations")
    public void setElementIdentifier_edgeWithNodes_returnsTrue(Relation relation) throws NoSpecificKind {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName effectId = u.getEffect(relation);
        QualifiedName causeId = u.getCause(relation);
        Element effect = getElement(ProvUtilities2.getEffectKind(relation.getKind()), effectId);
        Element cause = getElement(ProvUtilities2.getCauseKind(relation.getKind()), causeId);
        bundle.getStatement().add(effect);
        bundle.getStatement().add(cause);

        QualifiedName effectId2 = pF.newQualifiedName(effectId.getNamespaceURI(), effectId.getLocalPart() + "2", effectId.getPrefix());
        QualifiedName causeId2 = pF.newQualifiedName(causeId.getNamespaceURI(), causeId.getLocalPart() + "2", causeId.getPrefix());
        Element effect2 = getElement(ProvUtilities2.getEffectKind(relation.getKind()), effectId2);
        Element cause2 = getElement(ProvUtilities2.getCauseKind(relation.getKind()), causeId2);
        bundle.getStatement().add(effect2);
        bundle.getStatement().add(cause2);

        bundle.getStatement().add(relation);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertTrue(doc.setNewCauseAndEffect(effectId, causeId, relation.getKind(), effectId2, causeId2));

        assertTrue(doc.getEdges(effectId, causeId).isEmpty());
        assertTrue(doc.areAllRelationsMapped());
        assertTrue(doc.getNode(effectId).getEffectEdges().isEmpty());
        assertTrue(doc.getNode(effectId).getCauseEdges().isEmpty());
        assertTrue(doc.getNode(causeId).getEffectEdges().isEmpty());
        assertTrue(doc.getNode(causeId).getCauseEdges().isEmpty());

        assertNotNull(doc.getEdge(effectId2, causeId2));
        assertFalse(doc.getNode(effectId2).getEffectEdges().isEmpty());
        assertTrue(doc.getNode(effectId2).getCauseEdges().isEmpty());
        assertTrue(doc.getNode(causeId2).getEffectEdges().isEmpty());
        assertFalse(doc.getNode(causeId2).getCauseEdges().isEmpty());
        assertEquals(1, doc.getEdges().size());
    }

    @Test
    public void setCollectionMembers_hadMember_returnsTrue() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName collectionId = pF.newQualifiedName("uri", "collection", "ex");
        Entity collection = pF.newEntity(collectionId);
        bundle.getStatement().add(collection);

        QualifiedName newId = pF.newQualifiedName("uri", "newId", "ex");

        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity", "ex");
        Entity entity1 = pF.newEntity(entityId1);
        bundle.getStatement().add(entity1);

        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");
        Entity entity2 = pF.newEntity(entityId2);
        bundle.getStatement().add(entity2);

        HadMember hadMember = pF.newHadMember(collectionId, entityId1, entityId2);
        bundle.getStatement().add(hadMember);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertTrue(doc.setCollectionMembers(collectionId, List.of(entityId1, entityId2),
                newId, List.of(entityId1)));

        assertNull(doc.getEdge(collectionId, entityId1));
        assertNull(doc.getEdge(collectionId, entityId2));
        assertNotNull(doc.getEdge(newId, entityId1));
        assertNull(doc.getEdge(newId, entityId2));
        assertFalse(doc.getNode(entityId1).getCauseEdges().isEmpty());
        assertTrue(doc.getNode(entityId2).getCauseEdges().isEmpty());
    }

}
