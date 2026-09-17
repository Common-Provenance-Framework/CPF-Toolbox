package org.commonprovenanceframework.cpm.template.schema;

import java.util.ArrayList;
import java.util.List;

import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.QualifiedName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.commonprovenanceframework.cpm.constants.CpmType;
import org.commonprovenanceframework.cpm.template.deserialization.AttributesDeserializer;

public abstract class Connector {
  @JsonProperty(required = true)
  @JsonPropertyDescription("The identifier of the connector")
  private QualifiedName id;
  private String externalId;

  @JsonDeserialize(using = AttributesDeserializer.class)
  @JsonPropertyDescription("Other arbitrary attributes in Connector")
  private List<Attribute> attributes;

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

  public List<Attribute> getAttributes() {
    return this.attributes == null
        ? new ArrayList<Attribute>()
        : this.attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
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
