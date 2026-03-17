package cz.muni.fi.cpm.model;


import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.merged.CpmMergedFactory;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.vanilla.ProvFactory;
import org.openprovenance.prov.vanilla.QualifiedName;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


public class CpmUtilitiesTest {
    private ProvFactory pF;
    private CpmMergedFactory cF;
    private ICpmProvFactory cPF;

    private static Stream<Object[]> provideConnectorTypes() {
        return Stream.of(
                new Object[]{CpmType.FORWARD_CONNECTOR.toString()},
                new Object[]{CpmType.SPEC_FORWARD_CONNECTOR.toString()},
                new Object[]{CpmType.BACKWARD_CONNECTOR.toString()}
        );
    }

    @BeforeEach
    public void setUp() {
        pF = new ProvFactory();
        cF = new CpmMergedFactory();
        cPF = new CpmProvFactory();
    }


    @Test
    public void hasCpmType_nullElementAndNullType_returnsFalse() {
        assertFalse(CpmUtilities.hasCpmType((INode) null, null));
    }

    @Test
    public void hasCpmType_withValidCpmElementAndNullType_returnsFalse() {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.FORWARD_CONNECTOR.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasCpmType(node, null));
    }

    @Test
    public void hasCpmType_withNullElementAndValidType_returnsFalse() {
        assertFalse(CpmUtilities.hasCpmType((INode) null, CpmType.BACKWARD_CONNECTOR));
    }

    @Test
    public void hasCpmType_withValidCpmElementAndType_returnsTrue() {
      QualifiedName validQualifiedName = new QualifiedName(
          CpmNamespaceConstants.CPM_NS,
          CpmType.FORWARD_CONNECTOR.toString(),
          CpmNamespaceConstants.CPM_PREFIX);

      org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
      Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

      Element element = pF.newEntity(id, Collections.singletonList(attribute));
      INode node = cF.newNode(element);

      assertTrue(CpmUtilities.hasCpmType(node, CpmType.FORWARD_CONNECTOR));
    }

    @Test
    public void hasCpmType_withValidCpmElementAndSpecForwConType_returnsTrue() {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.SPEC_FORWARD_CONNECTOR.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertTrue(CpmUtilities.hasCpmType(node, CpmType.SPEC_FORWARD_CONNECTOR));
    }

    @Test
    public void hasCpmType_receiverAgentInvalidKind_returnsFalse() {
        QualifiedName receiverAgent = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.RECEIVER_AGENT.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "agent", "ex");
        Attribute recAtr = pF.newType(receiverAgent, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, List.of(recAtr));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasCpmType(node, CpmType.RECEIVER_AGENT));
    }

    @Test
    public void hasCpmType_withInvalidUri_returnsFalse() {
        QualifiedName validQualifiedName = new QualifiedName(
                "invalidUri",
                CpmType.FORWARD_CONNECTOR.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasCpmType(node, CpmType.FORWARD_CONNECTOR));
    }

    @Test
    public void hasCpmType_withInvalidPrefix_returnsFalse() {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.FORWARD_CONNECTOR.toString(),
                "invalidPrefix");

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasCpmType(node, CpmType.FORWARD_CONNECTOR));
    }

    @Test
    public void hasCpmType_withInvalidType_returnsFalse() {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                "invalidType",
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasCpmType(node, CpmType.FORWARD_CONNECTOR));
    }

    @Test
    public void hasCpmType_withInvalidUriAndPrefix_returnsFalse() {
        QualifiedName validQualifiedName = new QualifiedName(
                "invalidUri",
                "validQualifiedName",
                "invalidPrefix");

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasCpmType(node, CpmType.FORWARD_CONNECTOR));
    }

    @Test
    public void hasCpmType_withoutType_returnsFalse() {
        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

        Element element = pF.newEntity(id, (List<Attribute>) null);
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasCpmType(node, CpmType.FORWARD_CONNECTOR));
    }

    @ParameterizedTest
    @MethodSource("provideConnectorTypes")
    public void isConnector_withValidCpmElementAndType_returnsTrue(String cpmType) {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                cpmType,
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertTrue(CpmUtilities.isConnector(node));
    }

    @Test
    public void isConnector_invalidType_returnsFalse() {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.MAIN_ACTIVITY.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.isConnector(node));
    }


    @Test
    public void hasAnyCpmType_hasType_returnsTrue() {
        for (CpmType type : CpmType.values()) {
            org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

            Attribute cpmType = cPF.newCpmType(type);
            Element element = switch (CpmType.CPM_TYPE_TO_KIND.get(type)) {
                case PROV_AGENT -> pF.newAgent(id, Collections.singletonList(cpmType));
                case PROV_ACTIVITY -> pF.newActivity(id, null, null, Collections.singletonList(cpmType));
                case PROV_ENTITY -> pF.newEntity(id, Collections.singletonList(cpmType));
                default -> throw new IllegalStateException("Unexpected value: " + CpmType.CPM_TYPE_TO_KIND.get(type));
            };
            INode node = cF.newNode(element);

            assertTrue(CpmUtilities.hasValidCpmType(node));
        }
    }

    @Test
    public void hasValidCpmType_bothForwardAndBackwardConnector_returnsFalse() {
        QualifiedName backward = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.BACKWARD_CONNECTOR.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        QualifiedName forward = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.FORWARD_CONNECTOR.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute bacAtr = pF.newType(backward, pF.getName().PROV_QUALIFIED_NAME);
        Attribute forAtr = pF.newType(forward, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, List.of(bacAtr, forAtr));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasValidCpmType(node));
    }

    @Test
    public void hasValidCpmType_bothReceiverAndSenderAgent_returnsTrue() {
        QualifiedName receiverAgent = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.RECEIVER_AGENT.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        QualifiedName senderAgent = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmType.SENDER_AGENT.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "agent", "ex");
        Attribute recAtr = pF.newType(receiverAgent, pF.getName().PROV_QUALIFIED_NAME);
        Attribute senAtr = pF.newType(senderAgent, pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newAgent(id, List.of(recAtr, senAtr));
        INode node = cF.newNode(element);

        assertTrue(CpmUtilities.hasValidCpmType(node));
    }

    @Test
    public void hasValidCpmType_noType_returnsFalse() {
        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

        Element element = pF.newEntity(id);
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasValidCpmType(node));
    }


    @Test
    public void hasValidCpmType_invalidType_returnsFalse() {
        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

        Attribute invalidType = pF.newType(
                cPF.newCpmQualifiedName("invalid"),
                pF.getName().PROV_QUALIFIED_NAME);

        Element element = pF.newEntity(id, Collections.singletonList(invalidType));
        INode node = cF.newNode(element);

        assertFalse(CpmUtilities.hasValidCpmType(node));
    }

    @Test
    public void containsCpmAttribute_withNull_returnsFalse() {
        assertFalse(CpmUtilities.containsCpmAttribute(null, CpmAttribute.REFERENCED_BUNDLE_ID));
        assertFalse(CpmUtilities.containsCpmAttribute(pF.newEntity(pF.newQualifiedName("uri", "entity", "ex"), List.of()), null));
    }

    @Test
    public void containsCpmAttribute_validAttribute_returnsTrue() {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmAttribute.REFERENCED_BUNDLE_ID.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newOther(validQualifiedName, "ref", pF.getName().XSD_STRING);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));

        assertTrue(CpmUtilities.containsCpmAttribute(element, CpmAttribute.REFERENCED_BUNDLE_ID));
    }

    @Test
    public void containsCpmAttribute_wrongNs_returnsFalse() {
        QualifiedName validQualifiedName = new QualifiedName(
                "wrong",
                CpmAttribute.REFERENCED_BUNDLE_ID.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newOther(validQualifiedName, "ref", pF.getName().XSD_STRING);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));

        assertFalse(CpmUtilities.containsCpmAttribute(element, CpmAttribute.REFERENCED_BUNDLE_ID));
    }

    @Test
    public void getCpmAttributeValue_withNull_returnsNull() {
        assertNull(CpmUtilities.getCpmAttributeValue(null, CpmAttribute.REFERENCED_BUNDLE_ID));
        assertNull(CpmUtilities.getCpmAttributeValue(pF.newEntity(pF.newQualifiedName("uri", "entity", "ex"), List.of()), null));
    }

    @Test
    public void getAttributeValue_validCpmAttribute_returnsValue() {
        QualifiedName validQualifiedName = new QualifiedName(
                CpmNamespaceConstants.CPM_NS,
                CpmAttribute.REFERENCED_BUNDLE_ID.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newOther(validQualifiedName, "ref123", pF.getName().XSD_STRING);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));

        LangString lS = pF.newInternationalizedString("ref123");
        assertEquals(lS, CpmUtilities.getCpmAttributeValue(element, CpmAttribute.REFERENCED_BUNDLE_ID));
    }

    @Test
    public void getCpmAttributeValue_wrongNs_returnsNull() {
        QualifiedName wrongQualifiedName = new QualifiedName(
                "wrong",
                CpmAttribute.REFERENCED_BUNDLE_ID.toString(),
                CpmNamespaceConstants.CPM_PREFIX);

        org.openprovenance.prov.model.QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
        Attribute attribute = pF.newOther(wrongQualifiedName, "ref123", pF.getName().XSD_STRING);

        Element element = pF.newEntity(id, Collections.singletonList(attribute));

        assertNull(CpmUtilities.getCpmAttributeValue(element, CpmAttribute.REFERENCED_BUNDLE_ID));
    }

}
