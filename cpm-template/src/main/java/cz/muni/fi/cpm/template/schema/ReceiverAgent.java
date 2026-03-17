package cz.muni.fi.cpm.template.schema;

import java.util.List;

import org.openprovenance.prov.model.QualifiedName;

import cz.muni.fi.cpm.constants.CpmType;

public class ReceiverAgent extends CpmAgent {
  public ReceiverAgent() {
    super();
  }

  public ReceiverAgent(QualifiedName id) {
    super(id);
  }

  public ReceiverAgent(QualifiedName id, String contactIdPid) {
    super(id, contactIdPid);
  }

  @Override
  public List<CpmType> getType() {
    return List.of(CpmType.RECEIVER_AGENT);
  }
}
