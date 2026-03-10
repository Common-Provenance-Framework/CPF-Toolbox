package cz.muni.fi.cpm.merged;

import cz.muni.fi.cpm.model.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CpmMergedDocumentTest {

    @Nested
    public class CpmMergedDocumentAdditionalTest extends CpmDocumentAdditionalTest {
        public CpmMergedDocumentAdditionalTest() {
            super(new CpmMergedFactory());
        }
    }

    @Nested
    public class CpmMergedDocumentConstructorTest extends CpmDocumentConstructorTest {
        public CpmMergedDocumentConstructorTest() throws Exception {
            super(new CpmMergedFactory());
        }

        @Test
        public void constructor_identicalIdSameKind_returnsExpectedTypes() {
            Document document = pF.newDocument();
            document.setNamespace(cPF.newCpmNamespace());

            QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
            Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
            document.getStatementOrBundle().add(bundle);

            QualifiedName entityId = pF.newQualifiedName("uri", "entity", "ex");
            Entity entity1 = pF.newEntity(entityId);
            bundle.getStatement().add(entity1);

            Entity entity2 = pF.newEntity(entityId);
            QualifiedName typeId = pF.newQualifiedName("uri", "type", "ex");
            Type type = pF.newType("", typeId);
            entity2.getType().add(type);
            bundle.getStatement().add(entity2);

            CpmDocument doc = new CpmDocument(document, pF, cPF, cF);

            assertNotNull(doc.getNode(entityId));
            assertTrue(doc.getTraversalInformationPart().isEmpty());
            assertTrue(doc.getForwardConnectors().isEmpty());
            assertTrue(doc.getSpecForwardConnectors().isEmpty());
            assertFalse(doc.getNode(entityId).getAnyElement().getType().isEmpty());
            assertEquals(type, doc.getNode(entityId).getAnyElement().getType().getFirst());
        }
    }


    @Nested
    public class CpmMergedDocumentEqualsTest extends CpmDocumentEqualsTest {
        public CpmMergedDocumentEqualsTest() {
            super(new CpmMergedFactory());
        }
    }


    @Nested
    public class CpmMergedDocumentInfluenceTest extends CpmDocumentInfluenceTest {
        public CpmMergedDocumentInfluenceTest() {
            super(new CpmMergedFactory());
        }
    }

    @Nested
    class CpmMergedDocumentModificationTest extends CpmDocumentModificationTest {
        public CpmMergedDocumentModificationTest() {
            super(new CpmMergedFactory());
        }

    }

    @Nested
    class CpmMergedDocumentRemovalTest extends CpmDocumentRemovalTest {
        public CpmMergedDocumentRemovalTest() {
            super(new CpmMergedFactory());
        }

    }

}
