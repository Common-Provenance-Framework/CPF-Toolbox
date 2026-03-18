package cz.muni.fi.cpm.strategy;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.merged.CpmMergedFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.model.INode;
import cz.muni.fi.cpm.model.ITIStrategy;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.vanilla.ProvFactory;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class CpmTypeTIStrategyTest {
  ITIStrategy strategy;

  private ProvFactory pF;
  private CpmMergedFactory cF;
  private ICpmProvFactory cPF;

  @BeforeEach
  public void setUp() {
    pF = new ProvFactory();
    cF = new CpmMergedFactory();
    cPF = new CpmProvFactory();
    strategy = new CpmTypeTIStrategy();
  }

  @Test
  public void hasNonTIAttributes_nullElement_returnsFalse() {
    assertFalse(strategy.belongsToTraversalInformation(null));
  }

  @Test
  public void belongsToTraversalInformation_withoutAnyAttributes_returnsFalse() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newEntity(id);
    INode node = cF.newNode(element);

    assertFalse(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withoutType_withOther_returnsFalse() {
    QualifiedName attrValue = pF.newQualifiedName(
        "uri",
        "attributeValue",
        "ex");

    QualifiedName attrName = pF.newQualifiedName(
        "uri",
        "attributeName",
        "ex");

    QualifiedName attrValueType = pF.getName().PROV_QUALIFIED_NAME;

    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
    Attribute attribute = pF.newOther(attrName, attrValue, attrValueType);

    Element element = pF.newEntity(id, Collections.singletonList(attribute));
    INode node = cF.newNode(element);

    assertFalse(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withType_withOther_returnsFalse() {
    QualifiedName typeValue = pF.newQualifiedName(
        "uri",
        "type",
        "ex");

    QualifiedName attrValue = pF.newQualifiedName(
        "uri",
        "attributeValue",
        "ex");

    QualifiedName attrName = pF.newQualifiedName(
        "uri",
        "attributeName",
        "ex");

    QualifiedName attrValueType = pF.getName().PROV_QUALIFIED_NAME;

    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
    Attribute attribute = pF.newOther(attrName, attrValue, attrValueType);

    Element element = pF.newEntity(id, Collections.singletonList(attribute));
    element.getType().add(pF.newType(typeValue, attrValueType));
    INode node = cF.newNode(element);

    assertFalse(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmType_backwardConnector_returnsTrue() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newEntity(id, Collections.emptyList());
    element.getType().add(cPF.newCpmType(CpmType.BACKWARD_CONNECTOR));
    INode node = cF.newNode(element);

    assertTrue(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmType_forwardConnector_returnsTrue() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newEntity(id, Collections.emptyList());
    element.getType().add(cPF.newCpmType(CpmType.FORWARD_CONNECTOR));
    INode node = cF.newNode(element);

    assertTrue(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmType_specForwardConnector_returnsTrue() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newEntity(id, Collections.emptyList());
    element.getType().add(cPF.newCpmType(CpmType.SPEC_FORWARD_CONNECTOR));

    INode node = cF.newNode(element);

    assertTrue(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmType_identifier_returnsTrue() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newEntity(id, Collections.emptyList());
    element.getType().add(cPF.newCpmType(CpmType.IDENTIFIER));

    INode node = cF.newNode(element);

    assertTrue(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmType_mainActivity_returnsTrue() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newActivity(id);
    element.getType().add(cPF.newCpmType(CpmType.MAIN_ACTIVITY));

    INode node = cF.newNode(element);

    assertTrue(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmType_receiverAgent_returnsTrue() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newAgent(id);
    element.getType().add(cPF.newCpmType(CpmType.RECEIVER_AGENT));

    INode node = cF.newNode(element);

    assertTrue(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmType_senderAgent_returnsTrue() {
    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");

    Element element = pF.newAgent(id);
    element.getType().add(cPF.newCpmType(CpmType.SENDER_AGENT));

    INode node = cF.newNode(element);

    assertTrue(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withTwoAgentCpmTypes_returnsFalse() {

    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
    Element element = pF.newEntity(id);
    element.getType().add(cPF.newCpmType(CpmType.SENDER_AGENT));
    element.getType().add(cPF.newCpmType(CpmType.RECEIVER_AGENT));

    INode node = cF.newNode(element);

    assertFalse(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withTwoValidCpmTypes_returnsFalse() {

    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
    Element element = pF.newEntity(id);
    element.getType().add(cPF.newCpmType(CpmType.FORWARD_CONNECTOR));
    element.getType().add(cPF.newCpmType(CpmType.SPEC_FORWARD_CONNECTOR));

    INode node = cF.newNode(element);

    assertFalse(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withCpmTypeAndInvalidSecType_returnsFalse() {
    QualifiedName validQualifiedName = pF.newQualifiedName(
        CpmNamespaceConstants.CPM_NS,
        "validQualifiedName",
        CpmNamespaceConstants.CPM_PREFIX);

    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
    Element element = pF.newEntity(id);
    element.getType().add(cPF.newCpmType(CpmType.BACKWARD_CONNECTOR));
    element.getType().add(pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME));

    INode node = cF.newNode(element);

    assertFalse(strategy.belongsToTraversalInformation(node));
  }

  @Test
  public void belongsToTraversalInformation_withInvalidType_returnsFalse() {
    QualifiedName validQualifiedName = pF.newQualifiedName(
        CpmNamespaceConstants.CPM_NS,
        "validQualifiedName",
        CpmNamespaceConstants.CPM_PREFIX);

    QualifiedName id = pF.newQualifiedName("uri", "entity", "ex");
    Element element = pF.newEntity(id);
    element.getType().add(pF.newType(validQualifiedName, pF.getName().PROV_QUALIFIED_NAME));

    INode node = cF.newNode(element);

    assertFalse(strategy.belongsToTraversalInformation(node));
  }
}