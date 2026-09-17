package org.commonprovenanceframework.cpm.template.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.openprovenance.prov.model.QualifiedName;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConnectorAttributed {
    @JsonPropertyDescription("The identifier of the attribution relation")
    private QualifiedName id;
    @JsonProperty(required = true)
    @JsonPropertyDescription("The identifier of the agent")
    private QualifiedName agentId;

    public QualifiedName getId() {
        return id;
    }

    public void setId(QualifiedName id) {
        this.id = id;
    }

    public QualifiedName getAgentId() {
        return agentId;
    }

    public void setAgentId(QualifiedName agentId) {
        this.agentId = agentId;
    }

    public ConnectorAttributed() {
    }

    public ConnectorAttributed(QualifiedName agentId) {
        this.agentId = agentId;
    }

    public ConnectorAttributed(QualifiedName id, QualifiedName agentId) {
        this.id = id;
        this.agentId = agentId;
    }
}
