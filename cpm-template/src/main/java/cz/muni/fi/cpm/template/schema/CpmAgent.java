package cz.muni.fi.cpm.template.schema;

import java.util.List;

import org.openprovenance.prov.model.QualifiedName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import cz.muni.fi.cpm.constants.CpmType;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class CpmAgent {
  @JsonProperty(required = true)
  @JsonPropertyDescription("The identifier of the agent")
  private QualifiedName id;
  private String contactIdPid;

  public CpmAgent() {
  }

  public CpmAgent(QualifiedName id) {
    this.id = id;
  }

  public CpmAgent(QualifiedName id, String contactIdPid) {
    this.id = id;
    this.contactIdPid = contactIdPid;
  }

  public QualifiedName getId() {
    return id;
  }

  public void setId(QualifiedName id) {
    this.id = id;
  }

  public String getContactIdPid() {
    return contactIdPid;
  }

  public void setContactIdPid(String contactIdPid) {
    this.contactIdPid = contactIdPid;
  }

  @JsonIgnore
  public abstract List<CpmType> getType();

}
