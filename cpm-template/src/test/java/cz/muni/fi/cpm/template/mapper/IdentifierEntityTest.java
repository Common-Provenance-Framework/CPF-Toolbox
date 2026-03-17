package cz.muni.fi.cpm.template.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.vanilla.QualifiedName;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.template.schema.IdentifierEntity;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;

public class IdentifierEntityTest {
  private TemplateProvMapper mapper;

  @BeforeEach
  public void setUp() {
    mapper = new TemplateProvMapper(new CpmProvFactory());
  }

  @Test
  public void toStatements_basicIdSet_returnsOneStatement() {
    IdentifierEntity ie = new IdentifierEntity();
    QualifiedName qN = new QualifiedName("uri", "example", "ex");
    ie.setId(qN);

    List<Statement> statements = mapper.toStatementsStream(ie).toList();

    assertNotNull(statements);
    assertEquals(1, statements.size());

    Statement statement = statements.getFirst();
    assertNotNull(statement);
    assertInstanceOf(Entity.class, statement);

    Entity entity = (Entity) statement;
    assertEquals(qN, entity.getId());

    assertNotNull(entity.getType());
    assertEquals(1, entity.getType().size());
    Type type = entity.getType().getFirst();
    assertEquals(CpmType.IDENTIFIER.toString(), ((QualifiedName) type.getValue()).getLocalPart());
  }

  @Test
  public void toStatements_withExternalId_returnsCorrectExternalId() {
    IdentifierEntity ie = new IdentifierEntity();
    ie.setId(new QualifiedName("uri", "example", "ex"));
    String qN = "externalId";
    ie.setExternalId(qN);

    List<Statement> statements = mapper.toStatementsStream(ie).toList();

    Entity entity = (Entity) statements.getFirst();

    assertNotNull(entity.getOther());
    assertEquals(1, entity.getOther().size());
    assertEquals(CpmAttribute.EXTERNAL_ID.toString(), entity.getOther().getFirst().getElementName().getLocalPart());
    assertEquals(qN, ((LangString) entity.getOther().getFirst().getValue()).getValue());
  }

  @Test
  public void toStatements_withExternalIdType_returnsCorrectExternalIdType() {
    IdentifierEntity ie = new IdentifierEntity();
    ie.setId(new QualifiedName("uri", "example", "ex"));
    String externalIdType = "externalIdType";
    ie.setExternalIdType(externalIdType);

    List<Statement> statements = mapper.toStatementsStream(ie).toList();

    Entity entity = (Entity) statements.getFirst();

    assertNotNull(entity.getOther());
    assertEquals(1, entity.getOther().size());
    assertInstanceOf(LangString.class, entity.getOther().getFirst().getValue());
    assertEquals(CpmAttribute.EXTERNAL_ID_TYPE.toString(),
        entity.getOther().getFirst().getElementName().getLocalPart());
    assertEquals(externalIdType, ((LangString) entity.getOther().getFirst().getValue()).getValue());
  }

  @Test
  public void toStatements_withComment_returnsCorrectComment() {
    IdentifierEntity ie = new IdentifierEntity();
    ie.setId(new QualifiedName("uri", "example", "ex"));
    String comment = "Comment";
    ie.setComment(comment);

    List<Statement> statements = mapper.toStatementsStream(ie).toList();

    Entity entity = (Entity) statements.getFirst();

    assertNotNull(entity.getOther());
    assertEquals(1, entity.getOther().size());
    assertInstanceOf(LangString.class, entity.getOther().getFirst().getValue());
    assertEquals(CpmAttribute.COMMENT.toString(), entity.getOther().getFirst().getElementName().getLocalPart());
    assertEquals(comment, ((LangString) entity.getOther().getFirst().getValue()).getValue());
  }

  @Test
  public void toStatements_nullEntity_returnsNull() {
    assertTrue(mapper.toStatementsStream((IdentifierEntity) null).toList().isEmpty());
  }
}