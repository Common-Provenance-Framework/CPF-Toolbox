package cz.muni.fi.cpm.template.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cz.muni.fi.cpm.template.deserialization.AttributesDeserializer;
import cz.muni.fi.cpm.template.serialization.AttributesSeserializer;

import java.util.ArrayList;
import java.util.List;

import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.QualifiedName;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IdentifierEntity {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The identifier of the entity")
    private QualifiedName id;
    @JsonPropertyDescription("The external identifier")
    private String externalId;
    @JsonPropertyDescription("The type of the external identifier")
    private String externalIdType;
    @JsonPropertyDescription("A comment")
    private String comment;

    @JsonDeserialize(using = AttributesDeserializer.class)
    @JsonSerialize(using = AttributesSeserializer.class)
    @JsonPropertyDescription("Other arbitrary attributes in IdentifierEntity")
    private List<Attribute> attributes;

    public IdentifierEntity(String externalIdType, String externalId, QualifiedName id) {
        this.externalIdType = externalIdType;
        this.externalId = externalId;
        this.id = id;
    }

    public IdentifierEntity() {
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

    public String getExternalIdType() {
        return externalIdType;
    }

    public void setExternalIdType(String externalIdType) {
        this.externalIdType = externalIdType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    public List<Attribute> getAttributes() {
      return this.attributes == null
          ? new ArrayList<Attribute>()
          : this.attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
      this.attributes = attributes;
    }
}
