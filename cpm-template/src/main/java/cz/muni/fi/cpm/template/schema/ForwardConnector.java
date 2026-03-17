package cz.muni.fi.cpm.template.schema;

import org.openprovenance.prov.model.QualifiedName;

import com.fasterxml.jackson.annotation.JsonInclude;

import cz.muni.fi.cpm.constants.CpmType;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ForwardConnector extends Connector {

  public ForwardConnector() {
  }

  public ForwardConnector(QualifiedName id) {
    super(id);
  }

  @Override
  public CpmType getType() {
    return CpmType.FORWARD_CONNECTOR;
  }

}
