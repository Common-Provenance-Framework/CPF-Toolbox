package cz.muni.fi.cpm.template.schema;

import java.util.List;

import org.openprovenance.prov.model.QualifiedName;

import cz.muni.fi.cpm.constants.CpmType;

public class SenderAgent extends CpmAgent {

  public SenderAgent() {
    super();
  }

  public SenderAgent(QualifiedName id) {
    super(id);
  }

  public SenderAgent(QualifiedName id, String contactIdPid) {
    super(id, contactIdPid);
  }

  @Override
  public List<CpmType> getType() {
    return List.of(CpmType.SENDER_AGENT);
  }

}
