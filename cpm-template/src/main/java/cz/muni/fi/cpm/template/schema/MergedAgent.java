package cz.muni.fi.cpm.template.schema;

import java.util.ArrayList;
import java.util.List;

import org.openprovenance.prov.model.QualifiedName;

import cz.muni.fi.cpm.constants.CpmType;

/**
 * A CPM agent holding more than one role, emitted as a single agent statement
 * carrying every role it holds.
 */
public class MergedAgent extends CpmAgent {
  private final List<CpmType> types = new ArrayList<>();

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
    return seed(agent);
  }

  public static MergedAgent from(ReceiverAgent agent) {
    return seed(agent);
  }

  public static MergedAgent from(CurrentAgent agent) {
    return seed(agent);
  }

  private static MergedAgent seed(CpmAgent agent) {
    MergedAgent merged = new MergedAgent(agent.getId(), agent.getContactIdPid());
    merged.types.addAll(agent.getType());
    return merged;
  }

  /**
   * Folds another agent's roles into this one. The first non-null contact
   * identifier encountered wins.
   *
   * @param other the agent whose roles are added to this one
   * @return this agent
   */
  public MergedAgent merge(CpmAgent other) {
    other.getType().stream().filter(type -> !types.contains(type)).forEach(types::add);

    if (getContactIdPid() == null) {
      setContactIdPid(other.getContactIdPid());
    }

    return this;
  }

  @Override
  public List<CpmType> getType() {
    return types;
  }

}
