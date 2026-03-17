package cz.muni.fi.cpm.template.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.SpecializationOf;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.openprovenance.prov.vanilla.QualifiedName;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.template.schema.BackwardConnector;
import cz.muni.fi.cpm.template.schema.ConnectorAttributed;
import cz.muni.fi.cpm.template.schema.HashAlgorithms;
import cz.muni.fi.cpm.template.schema.SpecForwardConnector;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;

public class ConnectorTest {
  private TemplateProvMapper mapper;

  @BeforeEach
  public void setUp() {
    mapper = new TemplateProvMapper(new CpmProvFactory());
  }

  @Test
  public void toStatements_basicIdSet_returnsOneStatement() {
    BackwardConnector connector = new BackwardConnector();
    QualifiedName id = new QualifiedName("uri", "connectorExample", "ex");
    connector.setId(id);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();

    assertNotNull(statements);
    assertEquals(1, statements.size());

    Statement statement = statements.getFirst();
    assertInstanceOf(Entity.class, statement);
    Entity entity = (Entity) statement;

    assertNotNull(entity.getType());
    assertEquals(1, entity.getType().size());
    Type type = entity.getType().getFirst();
    assertEquals(CpmType.BACKWARD_CONNECTOR.toString(), ((QualifiedName) type.getValue()).getLocalPart());
  }

  @Test
  public void toStatements_withExternalId_returnsCorrectExternalId() {
    BackwardConnector connector = new BackwardConnector();
    connector.setId(new QualifiedName("uri", "connectorExample", "ex"));
    String externalId = "externalIdExample";
    connector.setExternalId(externalId);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    Entity entity = (Entity) statements.getFirst();

    assertNotNull(entity.getOther());
    assertInstanceOf(LangString.class, entity.getOther().getLast().getValue());
    assertEquals(CpmAttribute.EXTERNAL_ID.toString(), entity.getOther().getLast().getElementName().getLocalPart());
    assertEquals(externalId, ((LangString) entity.getOther().getLast().getValue()).getValue());
  }

  @Test
  public void toStatements_withReferencedBundleId_returnsCorrectBundleId() {
    BackwardConnector connector = new BackwardConnector();
    connector.setId(new QualifiedName("uri", "connectorExample", "ex"));
    QualifiedName bundleId = new QualifiedName("uri", "bundleExample", "ex");
    connector.setReferencedBundleId(bundleId);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    Entity entity = (Entity) statements.getFirst();

    assertNotNull(entity.getOther());
    assertEquals(1, entity.getOther().size());
    assertEquals(CpmAttribute.REFERENCED_BUNDLE_ID.toString(),
        entity.getOther().getFirst().getElementName().getLocalPart());
    assertEquals(bundleId, entity.getOther().getFirst().getValue());
  }

  @Test
  public void toStatements_withHashAndAlg_returnsCorrectValues() {
    BackwardConnector connector = new BackwardConnector();
    connector.setId(new QualifiedName("uri", "connectorExample", "ex"));
    Object hashValue = "hashValue";
    connector.setReferencedBundleHashValue(hashValue);
    connector.setHashAlg(HashAlgorithms.SHA256);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    Entity entity = (Entity) statements.getFirst();

    assertNotNull(entity.getOther());
    assertEquals(2, entity.getOther().size());

    Optional<Other> hashValueAttr = entity.getOther().stream()
        .filter(o -> o.getElementName().getLocalPart().equals(CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE.toString()))
        .findFirst();

    assertTrue(hashValueAttr.isPresent());
    assertEquals(hashValue, hashValueAttr.get().getValue());

    Optional<Other> hashAlgAttr = entity.getOther().stream()
        .filter(o -> o.getElementName().getLocalPart().equals(CpmAttribute.HASH_ALG.toString()))
        .findFirst();

    assertTrue(hashAlgAttr.isPresent());
    assertInstanceOf(LangString.class, hashAlgAttr.get().getValue());
    assertEquals(HashAlgorithms.SHA256.toString(), ((LangString) hashAlgAttr.get().getValue()).getValue());
  }

  @Test
  public void toStatements_withDerivedFrom_returnsCorrectRelation() {
    BackwardConnector connector = new BackwardConnector();
    QualifiedName qN = new QualifiedName("uri", "connectorExample", "ex");
    connector.setId(qN);
    QualifiedName derivedFromId = new QualifiedName("uri", "derivedFromExample", "ex");
    connector.setDerivedFrom(List.of(derivedFromId));

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    assertEquals(2, statements.size());

    Optional<WasDerivedFrom> relations = statements.stream()
        .filter(WasDerivedFrom.class::isInstance)
        .map(WasDerivedFrom.class::cast)
        .findFirst();

    assertTrue(relations.isPresent());

    Statement derivedFromStatement = relations.get();
    assertInstanceOf(WasDerivedFrom.class, derivedFromStatement);
    assertEquals(derivedFromId, ((WasDerivedFrom) derivedFromStatement).getUsedEntity());
    assertEquals(qN, ((WasDerivedFrom) derivedFromStatement).getGeneratedEntity());
  }

  @Test
  public void toStatements_withAttributedTo_returnsCorrectAttribution() {
    BackwardConnector connector = new BackwardConnector();
    QualifiedName qN = new QualifiedName("uri", "connectorExample", "ex");
    connector.setId(qN);
    QualifiedName attributedToId = new QualifiedName("uri", "attributedToExample", "ex");
    ConnectorAttributed cA = new ConnectorAttributed();
    cA.setAgentId(attributedToId);
    connector.setAttributedTo(cA);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    assertEquals(2, statements.size());

    Optional<WasAttributedTo> attributedToStatement = statements.stream()
        .filter(WasAttributedTo.class::isInstance)
        .map(WasAttributedTo.class::cast)
        .findFirst();

    assertTrue(attributedToStatement.isPresent());
    assertEquals(qN, attributedToStatement.get().getEntity());
    assertEquals(attributedToId, attributedToStatement.get().getAgent());
  }

  @Test
  public void toStatements_forwardConnectorSpecialisation_returnsCorrectSpecialisation() {
    SpecForwardConnector connector = new SpecForwardConnector();
    QualifiedName qN = new QualifiedName("uri", "connectorExample", "ex");
    connector.setId(qN);
    QualifiedName specialisationId = new QualifiedName("uri", "specialisationExample", "ex");
    connector.setSpecializationOf(specialisationId);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    assertEquals(2, statements.size());

    Optional<SpecializationOf> specStatement = statements.stream()
        .filter(SpecializationOf.class::isInstance)
        .map(SpecializationOf.class::cast)
        .findFirst();

    assertTrue(specStatement.isPresent());
    assertEquals(specialisationId, specStatement.get().getGeneralEntity());
    assertEquals(qN, specStatement.get().getSpecificEntity());
  }

  @Test
  public void toStatements_withProvenanceUri_returnsCorrectUri() {
    SpecForwardConnector connector = new SpecForwardConnector();
    connector.setId(new QualifiedName("uri", "connectorExample", "ex"));
    String provenanceUri = "http://example.com/provenance";
    connector.setProvenanceServiceUri(provenanceUri);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    Entity entity = (Entity) statements.get(0);

    assertNotNull(entity.getOther());
    assertEquals(1, entity.getOther().size());

    Attribute uri = entity.getOther().getFirst();
    assertEquals(CpmAttribute.PROVENANCE_SERVICE_URI.toString(), uri.getElementName().getLocalPart());
    assertEquals(provenanceUri, uri.getValue());
  }

  @Test
  public void toStatements_withReferencedBundleSpecV_returnsCorrectUri() {
    SpecForwardConnector connector = new SpecForwardConnector();
    connector.setId(new QualifiedName("uri", "connectorExample", "ex"));
    String version = "x.y.z";
    connector.setReferencedBundleSpecV(version);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    Entity entity = (Entity) statements.get(0);

    assertNotNull(entity.getOther());
    assertEquals(1, entity.getOther().size());

    Attribute attr = entity.getOther().getFirst();
    assertEquals(CpmAttribute.REFERENCED_BUNDLE_SPECV.toString(), attr.getElementName().getLocalPart());
    assertInstanceOf(LangString.class, attr.getValue());
    assertEquals(version, LangString.class.cast(attr.getValue()).getValue());
  }

  @Test
  public void toStatements_withReferencedMetaBundleSpecV_returnsCorrectUri() {
    SpecForwardConnector connector = new SpecForwardConnector();
    connector.setId(new QualifiedName("uri", "connectorExample", "ex"));
    String version = "x.y.z";
    connector.setReferencedMetaBundleSpecV(version);

    List<Statement> statements = mapper.toStatementsStream(connector).toList();
    Entity entity = (Entity) statements.get(0);

    assertNotNull(entity.getOther());
    assertEquals(1, entity.getOther().size());

    Attribute attr = entity.getOther().getFirst();
    assertEquals(CpmAttribute.REFERENCED_META_BUNDLE_SPECV.toString(), attr.getElementName().getLocalPart());
    assertInstanceOf(LangString.class, attr.getValue());
    assertEquals(version, LangString.class.cast(attr.getValue()).getValue());
  }

  @Test
  public void toStatements_nullConnector_returnsNull() {
    assertTrue(mapper.toStatementsStream((BackwardConnector) null).toList().isEmpty());
    assertTrue(mapper.toStatementsStream((SpecForwardConnector) null).toList().isEmpty());
  }
}
