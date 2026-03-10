package cz.muni.fi.cpm.model;

import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.exception.NoSpecificKind;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.StatementOrBundle.Kind;
import org.openprovenance.prov.model.extension.QualifiedHadMember;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class CpmDocumentConstructorTest {
    protected final ICpmFactory cF;
    protected final DatatypeFactory datatypeFactory;
    protected final org.openprovenance.prov.model.ProvFactory pF;
    protected final ProvUtilities u;
    protected final ICpmProvFactory cPF;

    public CpmDocumentConstructorTest(ICpmFactory cF) throws Exception {
        this.cF = cF;
        pF = cF.getProvFactory();
        u = new ProvUtilities();
        datatypeFactory = DatatypeFactory.newInstance();
        cPF = new CpmProvFactory(pF);
    }

    @Test
    public void constructor_nullDocument_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(null, pF, cPF, cF));
    }

    @Test
    public void constructor_withoutBundle_throwsIllegalArgumentException() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(document, pF, cPF, cF));
    }

    @Test
    public void constructor_graphPartsNullDocument_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), pF, cPF, cF));
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(pF.newQualifiedName("uri", "bundle", "ex"), null, new ArrayList<>(), new ArrayList<>(), pF, cPF, cF));
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(pF.newQualifiedName("uri", "bundle", "ex"), new ArrayList<>(), null, new ArrayList<>(), pF, cPF, cF));
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(pF.newQualifiedName("uri", "bundle", "ex"), new ArrayList<>(), new ArrayList<>(), null, pF, cPF, cF));
    }


    @Test
    public void constructor_statementPartsNullDocument_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, pF, cPF, cF));
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(null, new ArrayList<>(), new ArrayList<>(), pF.newQualifiedName("uri", "bundle", "ex"), pF, cPF, cF));
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(new ArrayList<>(), null, new ArrayList<>(), pF.newQualifiedName("uri", "bundle", "ex"), pF, cPF, cF));
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(new ArrayList<>(), new ArrayList<>(), null, pF.newQualifiedName("uri", "bundle", "ex"), pF, cPF, cF));
    }


    @Test
    public void constructor_statementsNullDocument_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(new ArrayList<>(), null, pF, cPF, cF));
        assertThrows(IllegalArgumentException.class, () -> new CpmDocument(null, pF.newQualifiedName("uri", "bundle", "ex"), pF, cPF, cF));
    }

    @Test
    public void constructor_graph_returnsExpectedDocument() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity1 = cPF.getProvFactory().newEntity(id1);
        INode node1 = cF.newNode(entity1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity entity2 = cPF.newCpmEntity(id2, CpmType.BACKWARD_CONNECTOR, new ArrayList<>());
        INode node2 = cF.newNode(entity2);

        QualifiedName id3 = cPF.newCpmQualifiedName("qN3");
        Agent agent = cPF.getProvFactory().newAgent(id3);
        INode node3 = cF.newNode(agent);

        QualifiedName id4 = cPF.newCpmQualifiedName("qN4");
        Entity entity4 = cPF.newCpmEntity(id4, CpmType.BACKWARD_CONNECTOR, new ArrayList<>());
        INode node4 = cF.newNode(entity4);

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id3);
        IEdge edge1 = cF.newEdge(relation1);
        edge1.setEffect(node1);
        edge1.setCause(node3);
        node1.getEffectEdges().add(edge1);
        node3.getCauseEdges().add(edge1);

        Relation relation2 = cPF.getProvFactory().newWasDerivedFrom(id2, id4);
        IEdge edge2 = cF.newEdge(relation2);
        edge2.setEffect(node2);
        edge2.setCause(node4);
        node2.getEffectEdges().add(edge2);
        node4.getCauseEdges().add(edge2);

        Relation relation3 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id2, id3);
        IEdge edge3 = cF.newEdge(relation3);
        edge3.setEffect(node2);
        edge3.setCause(node3);
        node2.getEffectEdges().add(edge3);
        node3.getCauseEdges().add(edge3);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(entity2, entity4, relation2), List.of(entity1, agent, relation1), List.of(relation3), bundleId, pF, cPF, cF);

        assertEquals(node1, doc.getNode(id1));
        assertEquals(node2, doc.getNode(id2));
        assertEquals(node3, doc.getNode(id3));
        assertEquals(node4, doc.getNode(id4));
        assertEquals(edge1, doc.getEdge(id1, id3));
        assertEquals(edge2, doc.getEdge(id2, id4));
        assertEquals(edge3, doc.getEdge(id2, id3));
        assertNotNull(doc.getBundleId());
    }

    @Test
    public void constructor_statements_returnsExpectedDocument() {
        QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
        Entity entity1 = cPF.getProvFactory().newEntity(id1);

        QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
        Entity entity2 = cPF.newCpmEntity(id2, CpmType.BACKWARD_CONNECTOR, new ArrayList<>());

        QualifiedName id3 = cPF.newCpmQualifiedName("qN3");
        Agent agent = cPF.getProvFactory().newAgent(id3);

        QualifiedName id4 = cPF.newCpmQualifiedName("qN4");
        Entity entity4 = cPF.newCpmEntity(id4, CpmType.BACKWARD_CONNECTOR, new ArrayList<>());

        Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id3);

        Relation relation2 = cPF.getProvFactory().newWasDerivedFrom(id2, id4);

        Relation relation3 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id2, id3);

        QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

        CpmDocument doc = new CpmDocument(List.of(entity2, entity4, relation2), List.of(entity1, agent, relation1), List.of(relation3), bundleId, pF, cPF, cF);

        assertNotNull(doc.getNode(id1));
        assertNotNull(doc.getNode(id2));
        assertNotNull(doc.getNode(id3));
        assertNotNull(doc.getNode(id4));
        assertNotNull(doc.getEdge(id1, id3));
        assertNotNull(doc.getEdge(id2, id4));
        assertNotNull(doc.getEdge(id2, id3));
        assertNotNull(doc.getBundleId());
    }


    @Test
    public void constructor_namespace_returnsExpectedNamespaces() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertEquals(document.getNamespace().getNamespaces(), doc.getNamespaces().getNamespaces());
    }

    @Test
    public void constructor_mainActivity_returnsExpectedMainActivity() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName activityId = pF.newQualifiedName("uri", "activity", "ex");
        XMLGregorianCalendar startTime = datatypeFactory.newXMLGregorianCalendar("2024-11-13T10:00:00");
        XMLGregorianCalendar endTime = datatypeFactory.newXMLGregorianCalendar("2024-11-13T12:00:00");
        CpmType type = CpmType.MAIN_ACTIVITY;
        Collection<Attribute> attributes = new ArrayList<>();
        Activity activity = cPF.newCpmActivity(activityId, startTime, endTime, type, attributes);
        bundle.getStatement().add(activity);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(activityId));
        assertFalse(doc.getTraversalInformationPart().isEmpty());
        assertNotNull(doc.getMainActivity());
        assertEquals(activity, doc.getNode(activityId).getAnyElement());
    }


    @Test
    public void constructor_regularActivity_returnsNull() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName activityId = pF.newQualifiedName("uri", "activity", "ex");
        Activity activity = pF.newActivity(activityId);
        bundle.getStatement().add(activity);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(activityId));
        assertTrue(doc.getTraversalInformationPart().isEmpty());
        assertNull(doc.getMainActivity());
    }


    @Test
    public void constructor_forwardConnector_returnsExpectedForwardConnectors() {
      Document document = pF.newDocument();
      document.setNamespace(cPF.newCpmNamespace());

      QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
      Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
      document.getStatementOrBundle().add(bundle);

      QualifiedName entityId = pF.newQualifiedName("uri", "entity", "ex");
      CpmType type = CpmType.FORWARD_CONNECTOR;
      Collection<Attribute> attributes = new ArrayList<>();
      Entity entity = cPF.newCpmEntity(entityId, type, attributes);
      bundle.getStatement().add(entity);

      CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

      assertNotNull(doc.getNode(entityId));
      assertFalse(doc.getTraversalInformationPart().isEmpty());
      assertEquals(1, doc.getForwardConnectors().size());
      assertEquals(entityId, doc.getForwardConnectors().getFirst().getAnyElement().getId());
      assertEquals(entity, doc.getNode(entityId).getAnyElement());
    }

    @Test
    public void constructor_specForwardConnector_returnsExpectedForwardConnectors() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName entityId = pF.newQualifiedName("uri", "entity", "ex");
        CpmType type = CpmType.SPEC_FORWARD_CONNECTOR;
        Collection<Attribute> attributes = new ArrayList<>();
        Entity entity = cPF.newCpmEntity(entityId, type, attributes);
        bundle.getStatement().add(entity);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(entityId));
        assertFalse(doc.getTraversalInformationPart().isEmpty());
        assertEquals(1, doc.getSpecForwardConnectors().size());
        assertEquals(entityId, doc.getSpecForwardConnectors().getFirst().getAnyElement().getId());
        assertEquals(entity, doc.getNode(entityId).getAnyElement());
    }


    @Test
    public void constructor_regularEntity_returnsExpectedRegularEntity() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName entityId = pF.newQualifiedName("uri", "entity", "ex");
        Entity entity = pF.newEntity(entityId);
        bundle.getStatement().add(entity);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(entityId));
        assertTrue(doc.getTraversalInformationPart().isEmpty());
        assertTrue(doc.getForwardConnectors().isEmpty());
        assertEquals(entity, doc.getNode(entityId).getAnyElement());
        assertNotNull(doc.getBundleId());
    }


    @Test
    public void constructor_identicalIdDifferentKind_returnsExpectedTypes() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName identicalId = pF.newQualifiedName("uri", "id", "ex");

        Entity entity = pF.newEntity(identicalId);
        bundle.getStatement().add(entity);

        Activity activity = pF.newActivity(identicalId);
        bundle.getStatement().add(activity);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertThrows(IllegalStateException.class, () -> doc.getNode(identicalId));
        assertEquals(2, doc.getNodes(identicalId).size());
        assertEquals(entity, doc.getNode(identicalId, Kind.PROV_ENTITY).getAnyElement());
        assertEquals(activity, doc.getNode(identicalId, Kind.PROV_ACTIVITY).getAnyElement());
    }


    @Test
    public void constructor_senderAgent_returnsExpectedSenderAgent() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName agentId = pF.newQualifiedName("uri", "agent", "ex");
        CpmType type = CpmType.SENDER_AGENT;
        Collection<Attribute> attributes = new ArrayList<>();
        Agent agent = cPF.newCpmAgent(agentId, type, attributes);
        bundle.getStatement().add(agent);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(agentId));
        assertFalse(doc.getTraversalInformationPart().isEmpty());
        assertEquals(agent, doc.getNode(agentId).getAnyElement());
    }


    @Test
    public void constructor_regularAgent_returnsExpectedRegularAgent() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName agentId = pF.newQualifiedName("uri", "agent", "ex");
        Agent agent = pF.newAgent(agentId);
        bundle.getStatement().add(agent);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(agentId));
        assertTrue(doc.getTraversalInformationPart().isEmpty());
        assertEquals(agent, doc.getNode(agentId).getAnyElement());
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
    @MethodSource("cz.muni.fi.cpm.RelationProvider#provideRelations")
    public void constructor_validRelationsMapping_returnsMappedRelations(Relation relation) throws NoSpecificKind {
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

        bundle.getStatement().add(relation);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(effectId));
        assertNotNull(doc.getNode(doc.getNode(effectId).getAnyElement()));
        assertNotNull(doc.getNode(causeId));
        assertNotNull(doc.getNode(causeId).getAnyElement());
        assertNotNull(doc.getEdge(effectId, causeId));
        assertFalse(doc.getEdges(doc.getEdge(effectId, causeId).getAnyRelation()).isEmpty());
        assertEquals(relation, doc.getNode(effectId).getEffectEdges().getFirst().getAnyRelation());
        assertEquals(relation, doc.getNode(causeId).getCauseEdges().getFirst().getAnyRelation());
        assertTrue(doc.areAllRelationsMapped());

        if (relation instanceof Identifiable relWithId) {
            assertNotNull(doc.getEdge(relWithId.getId()));
            assertFalse(doc.getEdges(relWithId.getId(), relation.getKind()).isEmpty());
            assertEquals(relation, doc.getEdge(relWithId.getId()).getAnyRelation());
            assertEquals(effect, doc.getEdge(relWithId.getId()).getEffect().getAnyElement());
            assertEquals(cause, doc.getEdge(relWithId.getId()).getCause().getAnyElement());
        } else {
            assertEquals(relation, doc.getEdge(effectId, causeId).getAnyRelation());
            assertEquals(effect, doc.getEdge(effectId, causeId).getEffect().getAnyElement());
            assertEquals(cause, doc.getEdge(effectId, causeId).getCause().getAnyElement());
        }
    }


    @Test
    public void constructor_unfinishedRelationsMappingCause_handlesUnmappedRelations() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity1", "ex");
        Entity entity1 = pF.newEntity(entityId1);
        bundle.getStatement().add(entity1);

        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");


        QualifiedName relationId = pF.newQualifiedName("uri", "relation", "ex");
        WasDerivedFrom wasDerivedFrom = pF.newWasDerivedFrom(relationId, entityId1, entityId2);
        bundle.getStatement().add(wasDerivedFrom);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(entityId1));
        assertNull(doc.getNode(entityId2));
        assertNotNull(doc.getEdge(relationId));
        assertNotNull(doc.getEdge(entityId1, entityId2));
        assertNull(doc.getEdge(entityId1, entityId2).getCause());
        assertNotNull(doc.getEdge(entityId1, entityId2).getEffect());
        assertEquals(wasDerivedFrom, doc.getEdge(relationId).getAnyRelation());
        assertFalse(doc.areAllRelationsMapped());
        assertEquals(entity1, doc.getEdge(relationId).getEffect().getAnyElement());
        assertNull(doc.getEdge(relationId).getCause());

        Entity entity2 = pF.newEntity(entityId2);
        bundle.getStatement().add(entity2);
        doc.doAction(entity2);

        assertTrue(doc.areAllRelationsMapped());
        assertEquals(entity2, doc.getEdge(relationId).getCause().getAnyElement());
    }


    @Test
    public void constructor_unfinishedRelationsMappingEffect_handlesUnmappedRelations() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity1", "ex");

        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");
        Entity entity2 = pF.newEntity(entityId2);
        bundle.getStatement().add(entity2);

        QualifiedName relationId = pF.newQualifiedName("uri", "relation", "ex");
        WasDerivedFrom wasDerivedFrom = pF.newWasDerivedFrom(relationId, entityId1, entityId2);
        bundle.getStatement().add(wasDerivedFrom);

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNull(doc.getNode(entityId1));
        assertNotNull(doc.getNode(entityId2));
        assertNotNull(doc.getEdge(relationId));
        assertNotNull(doc.getEdge(entityId1, entityId2));
        assertNotNull(doc.getEdge(entityId1, entityId2).getCause());
        assertNull(doc.getEdge(entityId1, entityId2).getEffect());
        assertEquals(wasDerivedFrom, doc.getEdge(relationId).getAnyRelation());
        assertFalse(doc.areAllRelationsMapped());
        assertEquals(entity2, doc.getEdge(relationId).getCause().getAnyElement());
        assertNull(doc.getEdge(relationId).getEffect());

        Entity entity1 = pF.newEntity(entityId1);
        bundle.getStatement().add(entity1);
        doc.doAction(entity1);

        assertTrue(doc.areAllRelationsMapped());
        assertEquals(entity1, doc.getEdge(relationId).getEffect().getAnyElement());
    }


    @Test
    public void constructor_toDocument_returnsEquivalentDocument() {
        Document document = pF.newDocument();

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity", "ex");
        Entity entity1 = pF.newEntity(entityId1);
        bundle.getStatement().add(entity1);

        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");
        Entity entity2 = pF.newEntity(entityId2);
        bundle.getStatement().add(entity2);

        QualifiedName relationId = pF.newQualifiedName("uri", "relation", "ex");
        WasDerivedFrom wasDerivedFrom = pF.newWasDerivedFrom(relationId, entityId1, entityId2);
        bundle.getStatement().add(wasDerivedFrom);
        document.setNamespace(Namespace.gatherNamespaces(document));
        bundle.setNamespace(Namespace.gatherNamespaces(document));

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);
        Document resultDoc = doc.toDocument();

        Map<String, String> resultNs = resultDoc.getNamespace().getNamespaces();
        assertTrue(resultNs.entrySet().containsAll(document.getNamespace().getNamespaces().entrySet()));

        assertEquals(1, resultDoc.getStatementOrBundle().size());
        assertEquals(bundle.getKind(), resultDoc.getStatementOrBundle().getFirst().getKind());
        Bundle resultBundle = (Bundle) resultDoc.getStatementOrBundle().getFirst();
        assertEquals(bundle.getId(), resultBundle.getId());
        assertEquals(bundle.getStatement().size(), resultBundle.getStatement().size());
        assertEquals(new HashSet<>(bundle.getStatement()), new HashSet<>(resultBundle.getStatement()));
        assertNotNull(resultBundle.getNamespace());
        assertEquals(resultDoc.getNamespace(), resultBundle.getNamespace().getParent());
    }


    @Test
    public void constructor_hadMemberLast_returnsMappedHadMemberRelations() {
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

        assertNotNull(doc.getNode(entityId1));
        assertNotNull(doc.getNode(entityId2));
        assertNotNull(doc.getEdge(collectionId, entityId2));
        assertEquals(hadMember, doc.getEdge(collectionId, entityId1).getAnyRelation());
        assertEquals(hadMember, doc.getEdge(collectionId, entityId2).getAnyRelation());
        assertTrue(doc.areAllRelationsMapped());
    }


    @Test
    public void constructor_hadMemberFirst_returnsMappedHadMemberRelations() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName collectionId = pF.newQualifiedName("uri", "collection", "ex");
        Entity collection = pF.newEntity(collectionId);

        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity", "ex");
        Entity entity1 = pF.newEntity(entityId1);

        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");
        Entity entity2 = pF.newEntity(entityId2);

        HadMember hadMember = pF.newHadMember(collectionId, entityId1, entityId2);
        bundle.getStatement().add(hadMember);

        bundle.getStatement().addAll(List.of(collection, entity1, entity2));

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertNotNull(doc.getNode(entityId1));
        assertNotNull(doc.getNode(entityId2));
        assertNotNull(doc.getEdge(collectionId, entityId2));
        assertEquals(hadMember, doc.getEdge(collectionId, entityId1).getAnyRelation());
        assertEquals(hadMember, doc.getEdge(collectionId, entityId2).getAnyRelation());
        assertDoesNotThrow(() -> doc.getEdge(collectionId, collectionId));
        assertTrue(doc.areAllRelationsMapped());
    }


    @Test
    public void constructor_unMappedHadMember_returnsCorrectEdge() {
        Document document = pF.newDocument();
        document.setNamespace(cPF.newCpmNamespace());

        QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
        Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
        document.getStatementOrBundle().add(bundle);

        QualifiedName collectionId = pF.newQualifiedName("uri", "collection", "ex");
        Entity collection = pF.newEntity(collectionId);

        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity", "ex");
        Entity entity1 = pF.newEntity(entityId1);

        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");

        HadMember hadMember = pF.newHadMember(collectionId, entityId1, entityId2);
        bundle.getStatement().add(hadMember);

        bundle.getStatement().addAll(List.of(collection, entity1));

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

        assertFalse(doc.areAllRelationsMapped());
        assertEquals(1, doc.getEdges(collectionId, entityId2).size());
        assertNotSame(doc.getEdge(collectionId, entityId1), doc.getEdge(collectionId, entityId2));
    }


    @Test
    public void constructor_HadMemberToDocument_returnsExpectedDocument() {
        Document document = pF.newDocument();

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

        QualifiedName relationId = pF.newQualifiedName("uri", "relation", "ex");
        QualifiedHadMember hadMember = pF.newQualifiedHadMember(relationId, collectionId, List.of(entityId1, entityId2), Collections.emptyList());
        bundle.getStatement().add(hadMember);
        document.setNamespace(Namespace.gatherNamespaces(document));
        bundle.setNamespace(Namespace.gatherNamespaces(document));

        CpmDocument doc = new CpmDocument(document, pF, cPF, cF);
        Document resultDoc = doc.toDocument();

        Map<String, String> resultNs = resultDoc.getNamespace().getNamespaces();
        assertTrue(resultNs.entrySet().containsAll(document.getNamespace().getNamespaces().entrySet()));

        assertEquals(2, doc.getEdges(relationId).size());
        assertEquals(1, resultDoc.getStatementOrBundle().size());
        assertEquals(bundle.getKind(), resultDoc.getStatementOrBundle().getFirst().getKind());
        Bundle resultBundle = (Bundle) resultDoc.getStatementOrBundle().getFirst();
        assertEquals(bundle.getId(), resultBundle.getId());
        assertEquals(bundle.getStatement().size(), resultBundle.getStatement().size());
        assertEquals(new HashSet<>(bundle.getStatement()), new HashSet<>(resultBundle.getStatement()));
    }

}
