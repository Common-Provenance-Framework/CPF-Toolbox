package cz.muni.fi.cpm.vanilla;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.constants.DctAttributeConstants;
import cz.muni.fi.cpm.constants.DctNamespaceConstants;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import org.openprovenance.prov.model.*;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wrapper for the {@link org.openprovenance.prov.vanilla.ProvFactory} from
 * ProvToolBox for creation of Cpm Statements
 */
public class CpmProvFactory implements ICpmProvFactory {

  private final ProvFactory pF;

  public CpmProvFactory() {
    this.pF = new org.openprovenance.prov.vanilla.ProvFactory();
  }

  public CpmProvFactory(ProvFactory pF) {
    this.pF = pF;
  }

  @Override
  public ProvFactory getProvFactory() {
    return pF;
  }

  @Override
  public Type newCpmType(CpmType type) {
    return pF.newType(
        newCpmQualifiedName(type.toString()),
        pF.getName().PROV_QUALIFIED_NAME);
  }

  @Override
  public QualifiedName newCpmQualifiedName(String local) {
    return pF.newQualifiedName(CpmNamespaceConstants.CPM_NS, local, CpmNamespaceConstants.CPM_PREFIX);
  }

  @Override
  public Other newCpmAttribute(CpmAttribute attr, QualifiedName value) {
    return newCpmAttribute(
        attr,
        value,
        pF.getName().PROV_QUALIFIED_NAME);
  }

  @Override
  public Other newCpmAttribute(CpmAttribute attr, String value) {
    return newCpmAttribute(
        attr,
        value,
        pF.getName().XSD_STRING);
  }

  @Override
  public Other newCpmAttribute(CpmAttribute attr, Object value) {
    return newCpmAttribute(
        attr,
        value,
        pF.getName().XSD_BYTE);
  }

  @Override
  public Other newCpmAttribute(CpmAttribute attr, Object value, QualifiedName type) {
    return pF.newOther(
        newCpmQualifiedName(attr.toString()),
        value,
        type);
  }

  @Override
  public Other newDctAttribute(QualifiedName value) {
    return pF.newOther(
        pF.newQualifiedName(
            DctNamespaceConstants.DCT_NS,
            DctAttributeConstants.HAS_PART,
            DctNamespaceConstants.DCT_PREFIX),
        value,
        pF.getName().PROV_QUALIFIED_NAME);
  }

  @Override
  public Other newCpmAttributeExternalId(String value) {
    return newCpmAttribute(CpmAttribute.EXTERNAL_ID, value);
  }

  @Override
  public Other newCpmAttributeExternalIdType(String value) {
    return newCpmAttribute(CpmAttribute.EXTERNAL_ID_TYPE, value);
  }

  @Override
  public Other newCpmAttributeComment(String value) {
    return newCpmAttribute(CpmAttribute.COMMENT, value);
  }

  @Override
  public Other newCpmAttributeContactIdPid(String value) {
    return newCpmAttribute(CpmAttribute.CONTACT_ID_PID, value);
  }

  @Override
  public Other newCpmAttributeHashAlg(String value) {
    return newCpmAttribute(CpmAttribute.HASH_ALG, value);
  }

  @Override
  public Other newCpmAttributeProvenanceServiceUri(String value) {
    return newCpmAttribute(
        CpmAttribute.PROVENANCE_SERVICE_URI,
        value,
        pF.getName().XSD_ANY_URI);
  }

  @Override
  public Other newCpmAttributeReferencedBundleHashValue(Object value) {
    return newCpmAttribute(CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE, value);
  }

  @Override
  public Other newCpmAttributeReferencedBundleId(QualifiedName value) {
    return newCpmAttribute(CpmAttribute.REFERENCED_BUNDLE_ID, value);
  }

  @Override
  public Other newCpmAttributeReferencedMetaBundleId(QualifiedName value) {
    return newCpmAttribute(CpmAttribute.REFERENCED_META_BUNDLE_ID, value);
  }

  @Override
  public Other newCpmAttributeReferencedBundleSpecV(String value) {
    return newCpmAttribute(CpmAttribute.REFERENCED_BUNDLE_SPECV, value);
  }

  @Override
  public Entity newCpmEntity(QualifiedName id, CpmType type, Collection<Attribute> attributes) {
    attributes.add(newCpmType(type));
    return pF.newEntity(id, attributes);
  }

  @Override
  public Entity newCpmIdentifierEntity(QualifiedName id, Collection<Attribute> attributes) {
    attributes.add(newCpmType(CpmType.IDENTIFIER));
    return pF.newEntity(id, attributes);
  }

  @Override
  public Entity newCpmIdentifierEntity(QualifiedName id) {
    return this.newCpmIdentifierEntity(id, new ArrayList<Attribute>());
  }

  @Override
  public Entity newCpmBackwardConnector(QualifiedName id) {
    List<Attribute> attributes = List.of(
        newCpmType(CpmType.BACKWARD_CONNECTOR));
    return pF.newEntity(id, attributes);
  }

  @Override
  public Entity newCpmForwardConnector(QualifiedName id) {
    List<Attribute> attributes = List.of(
        newCpmType(CpmType.FORWARD_CONNECTOR));
    return pF.newEntity(id, attributes);
  }

  @Override
  public Entity newCpmSpecForwardConnector(QualifiedName id) {
    List<Attribute> attributes = List.of(
        newCpmType(CpmType.SPEC_FORWARD_CONNECTOR));
    return pF.newEntity(id, attributes);
  }

  @Override
  public Activity newCpmMainActivity(QualifiedName id, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime,
      Collection<Attribute> attributes) {
    attributes.add(newCpmType(CpmType.MAIN_ACTIVITY));
    return pF.newActivity(id, startTime, endTime, attributes);
  }

  @Override
  public Activity newCpmMainActivity(QualifiedName id, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime) {

    return this.newCpmMainActivity(id, startTime, endTime, new ArrayList<Attribute>());
  }

  @Override
  public Agent newCpmAgent(QualifiedName id, CpmType type, Collection<Attribute> attributes) {
    attributes.add(newCpmType(type));
    return pF.newAgent(id, attributes);
  }

  @Override
  public Agent newCpmSenderAgent(QualifiedName id) {
    return this.newCpmAgent(id, CpmType.SENDER_AGENT, new ArrayList<Attribute>());
  }

  @Override
  public Agent newCpmReceiverAgent(QualifiedName id) {
    return this.newCpmAgent(id, CpmType.RECEIVER_AGENT, new ArrayList<Attribute>());
  }

  @Override
  public Agent newCpmMergedAgent(QualifiedName id) {
    List<Attribute> attributes = List.of(
        this.newCpmType(CpmType.SENDER_AGENT),
        this.newCpmType(CpmType.RECEIVER_AGENT));
    return pF.newAgent(id, attributes);
  }

  public Namespace newCpmNamespace() {
    Namespace namespace = pF.newNamespace();
    namespace.addKnownNamespaces();

    namespace.getPrefixes().put(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
    namespace.getNamespaces().put(CpmNamespaceConstants.CPM_NS, CpmNamespaceConstants.CPM_PREFIX);
    namespace.getPrefixes().put(DctNamespaceConstants.DCT_PREFIX, DctNamespaceConstants.DCT_NS);
    namespace.getNamespaces().put(DctNamespaceConstants.DCT_NS, DctNamespaceConstants.DCT_PREFIX);

    return namespace;
  }

}
