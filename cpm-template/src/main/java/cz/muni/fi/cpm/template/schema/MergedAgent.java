package cz.muni.fi.cpm.template.schema;

import java.util.List;

import org.openprovenance.prov.model.QualifiedName;

import cz.muni.fi.cpm.constants.CpmType;

public class MergedAgent extends CpmAgent {

  public MergedAgent() {
    super();
  }

  public MergedAgent(QualifiedName id) {
    super(id);
  }

  public MergedAgent(QualifiedName id, String contactIdPid) {
    super(id, contactIdPid);
  }

  public static MergedAgent from(SenderAgent agent) {
    return new MergedAgent(agent.getId(), agent.getContactIdPid());
  }

  public static MergedAgent from(ReceiverAgent agent) {
    return new MergedAgent(agent.getId(), agent.getContactIdPid());
  }

  @Override
  public List<CpmType> getType() {
    return List.of(CpmType.SENDER_AGENT, CpmType.RECEIVER_AGENT);
  }

}
