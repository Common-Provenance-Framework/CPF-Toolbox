package org.commonprovenanceframework.cpm.model;

import org.commonprovenanceframework.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class CpmDocumentInfluenceTest {
    protected final ICpmFactory cF;
    protected final org.openprovenance.prov.model.ProvFactory pF;
    protected final ICpmProvFactory cPF;

    public CpmDocumentInfluenceTest(ICpmFactory cF) {
        this.cF = cF;
        pF = cF.getProvFactory();
        cPF = new CpmProvFactory(pF);
    }

    @Test
    public void addInfluence_withoutCauseAndEffect_returnsUnmappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(inf), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdge(id1, id2));
        assertFalse(doc.areAllRelationsMapped());
    }

    @Test
    public void addInfluence_withOneEffectSecond_returnsUnmappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(inf, entity), List.of(), bundleId, pF, cPF, cF);

        assertFalse(doc.getEdges(id1, id2, Kind.PROV_INFLUENCE).isEmpty());
        assertEquals(doc.getNode(id1), doc.getEdge(id1, id2).getEffect());
        assertFalse(doc.areAllRelationsMapped());

        QualifiedName newId1 = cPF.newCpmQualifiedName("newQN1");
        assertTrue(doc.setNewNodeIdentifier(id1, Kind.PROV_ENTITY, newId1));
        assertNotNull(doc.getEdge(id1, id2));
    }


    @Test
    public void addInfluence_withOneCauseSecond_returnsUnmappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity entity = pF.newEntity(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(inf, entity), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdge(id1, id2));
        assertEquals(doc.getNode(id2), doc.getEdge(id1, id2).getCause());
        assertFalse(doc.areAllRelationsMapped());
    }


    @Test
    public void addInfluence_effectFirstInfluenceSecondCauseThird_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(effect, inf, cause), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdge(id1, id2));
        assertEquals(doc.getNode(id1), doc.getEdge(id1, id2).getEffect());
        assertEquals(doc.getNode(id2), doc.getEdge(id1, id2).getCause());
        assertTrue(doc.getNode(id1).getEffectEdges().contains(doc.getEdge(id1, id2)));
        assertTrue(doc.getNode(id2).getCauseEdges().contains(doc.getEdge(id1, id2)));
        assertTrue(doc.areAllRelationsMapped());
    }


    @Test
    public void addInfluence_influenceFirstEffectSecondCauseThird_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(inf, effect, cause), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdge(id1, id2));
        assertEquals(doc.getNode(id1), doc.getEdge(id1, id2).getEffect());
        assertEquals(doc.getNode(id2), doc.getEdge(id1, id2).getCause());
        assertTrue(doc.getNode(id1).getEffectEdges().contains(doc.getEdge(id1, id2)));
        assertTrue(doc.getNode(id2).getCauseEdges().contains(doc.getEdge(id1, id2)));
        assertTrue(doc.areAllRelationsMapped());
    }

    @Test
    public void addInfluence_effectFirstCauseSecondInfluenceThird_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(effect, cause, inf), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdge(id1, id2));
        assertEquals(doc.getNode(id1), doc.getEdge(id1, id2).getEffect());
        assertEquals(doc.getNode(id2), doc.getEdge(id1, id2).getCause());
        assertTrue(doc.getNode(id1).getEffectEdges().contains(doc.getEdge(id1, id2)));
        assertTrue(doc.getNode(id2).getCauseEdges().contains(doc.getEdge(id1, id2)));
        assertTrue(doc.areAllRelationsMapped());
    }

    @Test
    public void addInfluence_oneEffectTwoCauses_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        Agent agent = pF.newAgent(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(effect, cause, inf, agent), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdges(rel));
        assertEquals(2, doc.getEdges(id1, id2).size());
        assertEquals(2, doc.getNodes(id2).size());
        assertEquals(2, doc.getNode(id1).getEffectEdges().size());
        assertEquals(1, doc.getNode(id2, Kind.PROV_ENTITY).getCauseEdges().size());
        assertEquals(1, doc.getNode(id2, Kind.PROV_AGENT).getCauseEdges().size());
        assertTrue(doc.areAllRelationsMapped());
    }


    @Test
    public void toDocument_influenceOneEffectTwoCauses_containsOneInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        Agent agent = pF.newAgent(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        Document document = pF.newDocument();
        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, List.of(effect, cause, inf, agent));
        document.getStatementOrBundle().add(bundle);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        Document resultDoc = doc.toDocument();

        assertEquals(1, resultDoc.getStatementOrBundle().size());
        assertEquals(bundle.getKind(), resultDoc.getStatementOrBundle().getFirst().getKind());
        Bundle resultBundle = (Bundle) resultDoc.getStatementOrBundle().getFirst();
        assertEquals(bundle.getId(), resultBundle.getId());
        assertEquals(bundle.getStatement().size(), resultBundle.getStatement().size());
        assertEquals(new HashSet<>(bundle.getStatement()), new HashSet<>(resultBundle.getStatement()));
    }


    @Test
    public void addInfluence_oneCauseTwoEffects_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        Agent agent = pF.newAgent(id1);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(effect, cause, inf, agent), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdges(rel));
        assertEquals(2, doc.getEdges(id1, id2).size());
        assertEquals(2, doc.getNodes(id1).size());
        assertEquals(2, doc.getNode(id2).getCauseEdges().size());
        assertEquals(1, doc.getNode(id1, Kind.PROV_ENTITY).getEffectEdges().size());
        assertEquals(1, doc.getNode(id1, Kind.PROV_AGENT).getEffectEdges().size());
        assertTrue(doc.areAllRelationsMapped());

        QualifiedName newId2 = cPF.newCpmQualifiedName("newQN2");
        assertTrue(doc.setNewNodeIdentifier(id2, Kind.PROV_ENTITY, newId2));
        assertEquals(2, doc.getEdges(id1, id2).size());
    }

    @Test
    public void addInfluence_twoEffectZeroCauseInfLast_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");

        Agent agent1 = pF.newAgent(id1);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(cause, agent1, inf), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdges(rel));
        assertEquals(2, doc.getEdges(id1, id2).size());
        assertEquals(2, doc.getNodes(id1).size());
        assertEquals(0, doc.getNodes(id2).size());
        assertEquals(1, doc.getNode(id1, Kind.PROV_ENTITY).getEffectEdges().size());
        assertEquals(1, doc.getNode(id1, Kind.PROV_AGENT).getEffectEdges().size());
        assertFalse(doc.areAllRelationsMapped());
    }


    @Test
    public void addInfluence_twoCauseZeroEffectInfLast_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        Agent agent2 = pF.newAgent(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(effect, agent2, inf), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdges(rel));
        assertEquals(2, doc.getEdges(id1, id2).size());
        assertEquals(0, doc.getNodes(id1).size());
        assertEquals(2, doc.getNodes(id2).size());
        assertEquals(1, doc.getNode(id2, Kind.PROV_ENTITY).getCauseEdges().size());
        assertEquals(1, doc.getNode(id2, Kind.PROV_AGENT).getCauseEdges().size());
        assertFalse(doc.areAllRelationsMapped());
    }


    @Test
    public void addInfluence_twoCauseTwoEffects_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        Agent agent1 = pF.newAgent(id1);
        Agent agent2 = pF.newAgent(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(), List.of(effect, agent1, inf, cause, agent2), List.of(), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdges(rel));
        assertEquals(4, doc.getEdges(id1, id2).size());
        assertEquals(2, doc.getNodes(id1).size());
        assertEquals(2, doc.getNodes(id2).size());
        assertEquals(2, doc.getNode(id2, Kind.PROV_ENTITY).getCauseEdges().size());
        assertEquals(2, doc.getNode(id1, Kind.PROV_ENTITY).getEffectEdges().size());
        assertEquals(2, doc.getNode(id2, Kind.PROV_AGENT).getCauseEdges().size());
        assertEquals(2, doc.getNode(id1, Kind.PROV_AGENT).getEffectEdges().size());
        assertTrue(doc.areAllRelationsMapped());

        QualifiedName newId1 = cPF.newCpmQualifiedName("newQN1");
        assertTrue(doc.setNewNodeIdentifier(id1, Kind.PROV_ENTITY, newId1));
        assertEquals(2, doc.getEdges(id1, id2).size());
    }


    @Test
    public void addInfluence_threeCauseThreeEffects_returnsMappedInfluence() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity cause = pF.newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity effect = pF.newEntity(id2);

        Agent agent1 = pF.newAgent(id1);
        Agent agent2 = pF.newAgent(id2);
        Activity activity1 = pF.newActivity(id1);
        Activity activity2 = pF.newActivity(id2);

        QualifiedName rel = cPF.newCpmQualifiedName("rel");
        WasInfluencedBy inf = pF.newWasInfluencedBy(rel, id1, id2);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(inf, cause, agent1, effect, agent2, activity1), bundleId, pF, cPF, cF);

        assertNotNull(doc.getEdges(rel));
        assertEquals(6, doc.getEdges(id1, id2).size());

        doc.doAction(activity2);

        assertEquals(9, doc.getEdges(id1, id2).size());
        assertEquals(9, doc.getEdges(id1, id2).stream().distinct().toList().size());
        assertEquals(3, doc.getNodes(id1).size());
        assertEquals(3, doc.getNodes(id2).size());
        assertEquals(3, doc.getNode(id1, Kind.PROV_ENTITY).getEffectEdges().size());
        assertEquals(3, doc.getNode(id2, Kind.PROV_ENTITY).getCauseEdges().size());
        assertEquals(3, doc.getNode(id1, Kind.PROV_AGENT).getEffectEdges().size());
        assertEquals(3, doc.getNode(id2, Kind.PROV_AGENT).getCauseEdges().size());
        assertEquals(3, doc.getNode(id1, Kind.PROV_ACTIVITY).getEffectEdges().size());
        assertEquals(3, doc.getNode(id2, Kind.PROV_ACTIVITY).getCauseEdges().size());
        assertTrue(doc.areAllRelationsMapped());

        QualifiedName newId1 = cPF.newCpmQualifiedName("newQN1");

        assertTrue(doc.setNewNodeIdentifier(id1, Kind.PROV_ENTITY, newId1));
        assertEquals(6, doc.getEdges(id1, id2).size());
        assertTrue(doc.areAllRelationsMapped());
    }
}
