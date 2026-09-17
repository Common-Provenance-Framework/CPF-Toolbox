package org.commonprovenanceframework.cpm.divided.unordered;

import org.commonprovenanceframework.cpm.divided.CpmDividedDocumentTest;
import org.junit.jupiter.api.Nested;

public class CpmUnorderedDocumentTest extends CpmDividedDocumentTest {

    @Nested
    public class CpmUnorderedDocumentAdditionalTest extends CpmDividedDocumentAdditionalTest {
        public CpmUnorderedDocumentAdditionalTest() {
            super(new CpmUnorderedFactory());
        }
    }

    @Nested
    public class CpmUnorderedDocumentConstructorTest extends CpmDividedDocumentConstructorTest {
        public CpmUnorderedDocumentConstructorTest() throws Exception {
            super(new CpmUnorderedFactory());
        }

    }


    @Nested
    public class CpmUnorderedDocumentEqualsTest extends CpmDividedDocumentEqualsTest {
        public CpmUnorderedDocumentEqualsTest() {
            super(new CpmUnorderedFactory());
        }
    }


    @Nested
    public class CpmUnorderedDocumentInfluenceTest extends CpmDividedDocumentInfluenceTest {
        public CpmUnorderedDocumentInfluenceTest() {
            super(new CpmUnorderedFactory());
        }
    }

    @Nested
    class CpmUnorderedDocumentModificationTest extends CpmDividedDocumentModificationTest {
        public CpmUnorderedDocumentModificationTest() {
            super(new CpmUnorderedFactory());
        }

    }

    @Nested
    class CpmUnorderedDocumentRemovalTest extends CpmDividedDocumentRemovalTest {
        public CpmUnorderedDocumentRemovalTest() {
            super(new CpmUnorderedFactory());
        }

    }

}
