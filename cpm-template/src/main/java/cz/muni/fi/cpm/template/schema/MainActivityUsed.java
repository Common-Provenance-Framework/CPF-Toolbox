package cz.muni.fi.cpm.template.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.openprovenance.prov.model.QualifiedName;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MainActivityUsed {
    @JsonPropertyDescription("The identifier of the usage relation")
    private QualifiedName id;
    @JsonProperty(required = true)
    @JsonPropertyDescription("The identifier of the backward connector")
    private QualifiedName backwardConnectorId;

    public QualifiedName getId() {
        return id;
    }

    public void setId(QualifiedName id) {
        this.id = id;
    }

    public QualifiedName getBackwardConnectorId() {
        return backwardConnectorId;
    }

    public void setBackwardConnectorId(QualifiedName bcId) {
        this.backwardConnectorId = bcId;
    }

    public MainActivityUsed() {
    }

    public MainActivityUsed(QualifiedName bcId) {
        this.backwardConnectorId = bcId;
    }

    public MainActivityUsed(QualifiedName id, QualifiedName bcId) {
        this.id = id;
        this.backwardConnectorId = bcId;
    }
}
