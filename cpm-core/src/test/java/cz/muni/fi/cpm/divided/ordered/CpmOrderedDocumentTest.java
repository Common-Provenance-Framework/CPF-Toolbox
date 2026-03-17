package cz.muni.fi.cpm.divided.ordered;

import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.divided.CpmDividedDocumentTest;
import cz.muni.fi.cpm.model.CpmDocument;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CpmOrderedDocumentTest extends CpmDividedDocumentTest {

    @Nested
    public class CpmOrderedDocumentAdditionalTest extends CpmDividedDocumentAdditionalTest {
        public CpmOrderedDocumentAdditionalTest() {
            super(new CpmOrderedFactory());
        }
    }

    @Nested
    public class CpmOrderedDocumentConstructorTest extends CpmDividedDocumentConstructorTest {
        public CpmOrderedDocumentConstructorTest() throws Exception {
            super(new CpmOrderedFactory());
        }


        @RepeatedTest(10)
        public void constructor_randomizedOrderOf_keepsOrder() {

            QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
            Entity entity1 = cPF.getProvFactory().newEntity(id1);

            QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
            Entity entity2 = cPF.newCpmBackwardConnector(id2);

            QualifiedName id3 = cPF.newCpmQualifiedName("qN3");
            Agent agent = cPF.getProvFactory().newAgent(id3);

            Entity duplicate = pF.newEntity(id2);

            Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id3);

            Relation relation2 = cPF.getProvFactory().newWasDerivedFrom(id2, id1);

            Activity activity1 = pF.newActivity(id1);

            Document document = pF.newDocument();
            QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
            Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
            document.getStatementOrBundle().add(bundle);

            List<Statement> st = Arrays.asList(entity1, entity2, agent, relation1, relation2, activity1, duplicate);
            Collections.shuffle(st);

            bundle.getStatement().addAll(st);

            Document docFromCpm = new CpmDocument(document, pF, cPF, cF).toDocument();
            assertEquals(bundle.getStatement(), ((Bundle) docFromCpm.getStatementOrBundle().getFirst()).getStatement());
        }

        @RepeatedTest(3)
        public void constructor_identicalObjects_keepsBothObjects() {
            QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
            Entity entity1 = cPF.getProvFactory().newEntity(id1);
            QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
            Entity entity2 = pF.newEntity(id2);

            Entity duplicate = pF.newEntity(id2);
            Relation relation = cPF.getProvFactory().newWasDerivedFrom(id2, id1);
            Relation duplicateRel = pF.newStatement(relation);

            Document document = pF.newDocument();
            QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
            Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
            document.getStatementOrBundle().add(bundle);

            List<Statement> st = Arrays.asList(entity2, duplicate, entity1, relation, duplicateRel);
            Collections.shuffle(st);

            bundle.getStatement().addAll(st);

            Document docFromCpm = new CpmDocument(document, pF, cPF, cF).toDocument();
            assertEquals(bundle.getStatement(), ((Bundle) docFromCpm.getStatementOrBundle().getFirst()).getStatement());
        }

    }


    @Nested
    public class CpmOrderedDocumentEqualsTest extends CpmDividedDocumentEqualsTest {
        public CpmOrderedDocumentEqualsTest() {
            super(new CpmOrderedFactory());
        }
    }


    @Nested
    public class CpmOrderedDocumentInfluenceTest extends CpmDividedDocumentInfluenceTest {
        public CpmOrderedDocumentInfluenceTest() {
            super(new CpmOrderedFactory());
        }


    }

    @Nested
    class CpmOrderedDocumentModificationTest extends CpmDividedDocumentModificationTest {
        public CpmOrderedDocumentModificationTest() {
            super(new CpmOrderedFactory());
        }

        @Test
        public void setElementIdentifier_twoElementsWithSameId_modifiesOnlyOne() {
            QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
            Entity entity = cPF.getProvFactory().newEntity(id1);

            Entity entity2 = cPF.getProvFactory().newEntity(id1);

            QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
            Agent agent = cPF.getProvFactory().newAgent(id2);

            QualifiedName newId1 = cPF.newCpmQualifiedName("newQN1");

            Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);
            Relation relation2 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), newId1, id2);

            QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

            CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, entity2, relation1, relation2), List.of(), bundleId, pF, cPF, cF);

            assertEquals(2, doc.getNode(id1).getElements().size());
            assertTrue(doc.setNewElementIdentifier(doc.getNode(id1).getAnyElement(), newId1));

            assertNotNull(doc.getNode(id1));
            assertEquals(1, doc.getNode(id1).getElements().size());
            assertNotNull(doc.getNode(newId1));
            assertTrue(doc.areAllRelationsMapped());
            assertNotNull(doc.getEdge(id1, id2).getEffect());

            List<Statement> stsAfterChange = ((Bundle) doc.toDocument().getStatementOrBundle().getFirst()).getStatement();
            assertEquals(newId1, ((Identifiable) stsAfterChange.get(0)).getId());
            assertEquals(id1, ((Identifiable) stsAfterChange.get(2)).getId());
        }


        @Test
        public void setRelationCauseAndEffect_twoRelationsWithSameId_modifiesOnlyOne() {
            QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
            Entity entity = cPF.getProvFactory().newEntity(id1);


            QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
            Agent agent = cPF.getProvFactory().newAgent(id2);

            QualifiedName newId1 = cPF.newCpmQualifiedName("newQN1");
            Entity entity2 = cPF.getProvFactory().newEntity(newId1);

            Relation relation1 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);
            Relation relation2 = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);

            QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

            CpmDocument doc = new CpmDocument(List.of(), List.of(entity, agent, entity2, relation1, relation2), List.of(), bundleId, pF, cPF, cF);

            assertEquals(2, doc.getEdge(id1, id2).getRelations().size());
            assertTrue(doc.setNewCauseAndEffect(doc.getEdge(id1, id2).getAnyRelation(), newId1, id2));

            assertNotNull(doc.getEdge(id1, id2));
            assertEquals(1, doc.getEdge(id1, id2).getRelations().size());
            assertNotNull(doc.getEdge(newId1, id2));
            assertTrue(doc.areAllRelationsMapped());
            assertFalse(doc.getNode(id1).getEffectEdges().isEmpty());
            assertFalse(doc.getNode(newId1).getEffectEdges().isEmpty());

            List<Statement> stsAfterChange = ((Bundle) doc.toDocument().getStatementOrBundle().getFirst()).getStatement();
            assertEquals(newId1, u.getEffect((Relation) stsAfterChange.get(3)));
            assertEquals(id1, u.getEffect((Relation) stsAfterChange.get(4)));
        }


        @Test
        public void setCollectionMembers_hadMemberOrder_returnsTrue() {
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

            QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");
            Entity entity2 = pF.newEntity(entityId2);

            HadMember hadMember = pF.newHadMember(collectionId, entityId1, entityId2);
            bundle.getStatement().add(hadMember);
            bundle.getStatement().add(entity1);
            bundle.getStatement().add(entity2);

            CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

            assertTrue(doc.setCollectionMembers(collectionId, List.of(entityId1, entityId2),
                    newId, List.of(entityId1)));

            List<Statement> stsAfterChange = ((Bundle) doc.toDocument().getStatementOrBundle().getFirst()).getStatement();
            assertEquals(newId, ((HadMember) stsAfterChange.get(1)).getCollection());
        }

    }

    @Nested
    class CpmOrderedDocumentRemovalTest extends CpmDividedDocumentRemovalTest {
        public CpmOrderedDocumentRemovalTest() {
            super(new CpmOrderedFactory());
        }

    }
}
