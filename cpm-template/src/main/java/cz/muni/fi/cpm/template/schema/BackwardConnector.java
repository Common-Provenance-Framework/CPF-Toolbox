package cz.muni.fi.cpm.template.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import cz.muni.fi.cpm.constants.CpmType;
import org.openprovenance.prov.model.QualifiedName;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BackwardConnector extends Connector {

  @JsonPropertyDescription("The referenced bundle's identifier")
  private QualifiedName referencedBundleId;

  @JsonPropertyDescription("The referenced meta bundle's identifier")
  private QualifiedName referencedMetaBundleId;

  @JsonPropertyDescription("The content version of the referenced finalized provenance component.")
  private String referencedBundleSpecV;

  // @JsonPropertyDescription("The content version of the referenced
  // meta-component.")
  // private String referencedMetaBundleSpecV;

  @JsonPropertyDescription("The referenced bundle's hash value's algorithm")
  private HashAlgorithms hashAlg;

  @JsonPropertyDescription("The referenced bundle's hash value")
  private Object referencedBundleHashValue;

  @JsonPropertyDescription("The identifier of the agent t which this connector is attributed to")
  private ConnectorAttributed attributedTo;

  public BackwardConnector() {
  }

  public BackwardConnector(QualifiedName id) {
    super(id);
  }

  public QualifiedName getReferencedBundleId() {
    return referencedBundleId;
  }

  public void setReferencedBundleId(QualifiedName referencedBundleId) {
    this.referencedBundleId = referencedBundleId;
  }

  public QualifiedName getReferencedMetaBundleId() {
    return referencedMetaBundleId;
  }

  public void setReferencedMetaBundleId(QualifiedName referencedMetaBundleId) {
    this.referencedMetaBundleId = referencedMetaBundleId;
  }

  public String getReferencedBundleSpecV() {
    return referencedBundleSpecV;
  }

  public void setReferencedBundleSpecV(String referencedBundleSpecV) {
    this.referencedBundleSpecV = referencedBundleSpecV;
  }

  // public String getReferencedMetaBundleSpecV() {
  // return referencedMetaBundleSpecV;
  // }

  // public void setReferencedMetaBundleSpecV(String referencedMetaBundleSpecV) {
  // this.referencedMetaBundleSpecV = referencedMetaBundleSpecV;
  // }

  public HashAlgorithms getHashAlg() {
    return hashAlg;
  }

  public void setHashAlg(HashAlgorithms hashAlg) {
    this.hashAlg = hashAlg;
  }

  public Object getReferencedBundleHashValue() {
    return referencedBundleHashValue;
  }

  public void setReferencedBundleHashValue(Object referencedBundleHashValue) {
    this.referencedBundleHashValue = referencedBundleHashValue;
  }

  public ConnectorAttributed getAttributedTo() {
    return attributedTo;
  }

  public void setAttributedTo(ConnectorAttributed attributedTo) {
    this.attributedTo = attributedTo;
  }

  @Override
  public CpmType getType() {
    return CpmType.BACKWARD_CONNECTOR;
  }

}
