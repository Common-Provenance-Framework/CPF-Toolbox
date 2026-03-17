package cz.muni.fi.cpm.template.schema;

import java.util.List;

import org.openprovenance.prov.model.QualifiedName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import cz.muni.fi.cpm.constants.CpmType;

public abstract class Connector {
  @JsonProperty(required = true)
  @JsonPropertyDescription("The identifier of the connector")
  private QualifiedName id;
  private String externalId;

  @JsonPropertyDescription("The identifier's of connector's from which this connector is derived from")
  private List<QualifiedName> derivedFrom;

  public Connector() {
  }

  public Connector(QualifiedName id) {
    this.id = id;
  }

  public QualifiedName getId() {
    return id;
  }

  public void setId(QualifiedName id) {
    this.id = id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public List<QualifiedName> getDerivedFrom() {
    return derivedFrom;
  }

  public void setDerivedFrom(List<QualifiedName> derivedFrom) {
    this.derivedFrom = derivedFrom;
  }

  @JsonIgnore
  public abstract CpmType getType();
}
