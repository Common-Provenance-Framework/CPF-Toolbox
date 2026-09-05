package cz.muni.fi.cpm.template.schema;

import java.util.List;

import org.openprovenance.prov.model.QualifiedName;

import cz.muni.fi.cpm.constants.CpmType;

public class CurrentAgent extends CpmAgent {

  public CurrentAgent() {
    super();
  }

  public CurrentAgent(QualifiedName id) {
    super(id);
  }

  public CurrentAgent(QualifiedName id, String contactIdPid) {
    super(id, contactIdPid);
  }

  @Override
  public List<CpmType> getType() {
    return List.of(CpmType.CURRENT_AGENT);
  }

}
