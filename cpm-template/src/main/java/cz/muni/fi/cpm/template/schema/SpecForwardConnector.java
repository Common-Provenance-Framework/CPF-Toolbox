package cz.muni.fi.cpm.template.schema;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import cz.muni.fi.cpm.constants.CpmType;
import org.openprovenance.prov.model.QualifiedName;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SpecForwardConnector extends Connector {

  @JsonPropertyDescription("The referenced bundle's identifier")
  private QualifiedName referencedBundleId;

  @JsonPropertyDescription("The referenced meta bundle's identifier")
  private QualifiedName referencedMetaBundleId;

  @JsonPropertyDescription("The content version of the referenced finalized provenance component.")
  private String referencedBundleSpecV;

  // @JsonPropertyDescription("The content version of the referenced meta-component.")
  // private String referencedMetaBundleSpecV;

  @JsonPropertyDescription("The URI of the provenance service")
  private String provenanceServiceUri;

  @JsonPropertyDescription("The referenced bundle's hash value's algorithm")
  private HashAlgorithms hashAlg;

  @JsonPropertyDescription("The referenced bundle's hash value")
  private Object referencedBundleHashValue;

  @JsonPropertyDescription("The identifier of the connector of which this connector is a specialisation of")
  private QualifiedName specializationOf;

  @JsonPropertyDescription("The identifier of the agent t which this connector is attributed to")
  private ConnectorAttributed attributedTo;

  public SpecForwardConnector() {
  }

  public SpecForwardConnector(QualifiedName id) {
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
  //   return referencedMetaBundleSpecV;
  // }

  // public void setReferencedMetaBundleSpecV(String referencedMetaBundleSpecV) {
  //   this.referencedMetaBundleSpecV = referencedMetaBundleSpecV;
  // }

  public String getProvenanceServiceUri() {
    return provenanceServiceUri;
  }

  public void setProvenanceServiceUri(String provenanceServiceUri) {
    this.provenanceServiceUri = provenanceServiceUri;
  }

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

  public QualifiedName getSpecializationOf() {
    return specializationOf;
  }

  public void setSpecializationOf(QualifiedName specializationOf) {
    this.specializationOf = specializationOf;
  }

  public ConnectorAttributed getAttributedTo() {
    return attributedTo;
  }

  public void setAttributedTo(ConnectorAttributed attributedTo) {
    this.attributedTo = attributedTo;
  }

  @Override
  public CpmType getType() {
    return CpmType.SPEC_FORWARD_CONNECTOR;
  }

}
