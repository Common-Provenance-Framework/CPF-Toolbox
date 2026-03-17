package cz.muni.fi.cpm.template.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.vanilla.QualifiedName;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.template.schema.MergedAgent;
import cz.muni.fi.cpm.template.schema.ReceiverAgent;
import cz.muni.fi.cpm.template.schema.SenderAgent;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;

public class CpmAgentTest {
  private TemplateProvMapper mapper;

  @BeforeEach
  public void setUp() {
    mapper = new TemplateProvMapper(new CpmProvFactory());
  }

  @Test
  public void toStatements_ReceiverAgent_basicIdSet_returnsOneStatement() {
    ReceiverAgent agent = new ReceiverAgent();
    QualifiedName id = new QualifiedName("uri", "agentExample", "ex");
    agent.setId(id);

    List<Statement> statements = mapper.toStatementsStream(agent).toList();

    assertNotNull(statements);
    assertEquals(1, statements.size());

    Statement statement = statements.getFirst();
    assertInstanceOf(Agent.class, statement);

    Agent resultAgent = (Agent) statement;
    assertEquals(id, resultAgent.getId());

    assertNotNull(resultAgent.getType());
    assertEquals(1, resultAgent.getType().size());
    Type type = resultAgent.getType().getFirst();
    assertEquals(CpmType.RECEIVER_AGENT.toString(), ((QualifiedName) type.getValue()).getLocalPart());
  }

  @Test
  public void toStatements_SenderAgent_withContactIdPid_returnsCorrectContactId() {
    SenderAgent agent = new SenderAgent();
    agent.setId(new QualifiedName("uri", "agentExample", "ex"));
    String contactIdPid = "contact123";
    agent.setContactIdPid(contactIdPid);

    List<Statement> statements = mapper.toStatementsStream(agent).toList();
    Agent resultAgent = (Agent) statements.getFirst();

    List<Other> otherAttributes = resultAgent.getOther();
    assertNotNull(otherAttributes);
    assertEquals(1, otherAttributes.size());
    Type type = resultAgent.getType().getFirst();
    assertEquals(CpmType.SENDER_AGENT.toString(), ((QualifiedName) type.getValue()).getLocalPart());

    Attribute contactIdAttr = otherAttributes.getFirst();
    assertInstanceOf(LangString.class, contactIdAttr.getValue());
    assertEquals(CpmAttribute.CONTACT_ID_PID.toString(), contactIdAttr.getElementName().getLocalPart());
    assertEquals(contactIdPid, ((LangString) contactIdAttr.getValue()).getValue());
  }

  @Test
  public void toStatements_mergedAgent_withContactIdPid_returnsCorrectContactId() {
    MergedAgent agent = new MergedAgent();
    agent.setId(new QualifiedName("uri", "agentExample", "ex"));
    String contactIdPid = "contact123";
    agent.setContactIdPid(contactIdPid);

    List<Statement> statements = mapper.toStatementsStream(agent).toList();
    Agent resultAgent = (Agent) statements.getFirst();

    List<Other> otherAttributes = resultAgent.getOther();
    assertNotNull(otherAttributes);
    assertEquals(1, otherAttributes.size());
    Attribute contactIdAttr = otherAttributes.getFirst();
    assertInstanceOf(LangString.class, contactIdAttr.getValue());
    assertEquals(CpmAttribute.CONTACT_ID_PID.toString(), contactIdAttr.getElementName().getLocalPart());
    assertEquals(contactIdPid, ((LangString) contactIdAttr.getValue()).getValue());

    List<String> types = resultAgent.getType().stream()
        .map(Type::getValue)
        .filter(QualifiedName.class::isInstance)
        .map(QualifiedName.class::cast)
        .map(QualifiedName::getLocalPart)
        .toList();

    assertEquals(2, types.size());
    assertTrue(types.contains(CpmType.SENDER_AGENT.toString()));
    assertTrue(types.contains(CpmType.RECEIVER_AGENT.toString()));
    // assertTrue(types.stream().map(Type::getValue).filter(QualifiedName.class::isInstance).map(QualifiedName.class::cast)
    // .map(QualifiedName::getLocalPart).anyMatch(CpmType.SENDER_AGENT.toString()::equals));
    // assertTrue(types.stream().map(Type::getValue).filter(QualifiedName.class::isInstance).map(QualifiedName.class::cast)
    // .map(QualifiedName::getLocalPart).anyMatch(CpmType.RECEIVER_AGENT.toString()::equals));

  }

  @Test
  public void toStatements_nullAgent_returnsNull() {
    assertTrue(mapper.toStatementsStream((ReceiverAgent) null).toList().isEmpty());
    assertTrue(mapper.toStatementsStream((SenderAgent) null).toList().isEmpty());
  }
}
