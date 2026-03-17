package cz.muni.fi.cpm.template.schema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.openprovenance.prov.core.json.serialization.deserial.CustomNamespacePrefixDeserializer;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.QualifiedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"prefixes"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TraversalInformation {
    @JsonIgnore
    @JsonDeserialize(using = CustomNamespacePrefixDeserializer.class)
    private Namespace namespace = new Namespace();

    @JsonProperty(required = true)
    @JsonPropertyDescription("The main activity of the traversal information part")
    private MainActivity mainActivity;

    @JsonProperty(required = true)
    @JsonPropertyDescription("The bundle's identifier")
    private QualifiedName bundleName;

    @JsonPropertyDescription("The backward connectors")
    private List<BackwardConnector> backwardConnectors;

    // TODO: Maybe there is exact one forward connector, so we can change it to a single object instead of a list?
    @JsonPropertyDescription("The forward connectors")
    private List<ForwardConnector> forwardConnectors;

    @JsonPropertyDescription("The specialization forward connectors")
    private List<SpecForwardConnector> specForwardConnectors;

    @JsonPropertyDescription("The sender agents")
    private List<SenderAgent> senderAgents;

    @JsonPropertyDescription("The receiver agents")
    private List<ReceiverAgent> receiverAgents;

    @JsonPropertyDescription("The identifier entities")
    private List<IdentifierEntity> identifierEntities;

    @JsonPropertyDescription("The namespace declarations")
    @JsonDeserialize(using = CustomNamespacePrefixDeserializer.class)
    @JsonProperty(required = true)
    public Map<String, String> getPrefixes() {
        return namespace.getPrefixes();
    }

    public void setPrefixes(Map<String, String> prefixes) {
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            this.namespace.register(entry.getKey(), entry.getValue());
        }
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public QualifiedName getBundleName() {
        return bundleName;
    }

    public void setBundleName(QualifiedName bundleName) {
        this.bundleName = bundleName;
    }

    public List<BackwardConnector> getBackwardConnectors() {
        if (backwardConnectors == null) {
            backwardConnectors = new ArrayList<>();
        }
        return backwardConnectors;
    }

    public void setBackwardConnectors(List<BackwardConnector> backwardConnectors) {
        this.backwardConnectors = backwardConnectors;
    }

    public List<ForwardConnector> getForwardConnectors() {
        if (forwardConnectors == null) {
            forwardConnectors = new ArrayList<>();
        }
        return forwardConnectors;
    }

    public void setForwardConnectors(List<ForwardConnector> forwardConnectors) {
      this.forwardConnectors = forwardConnectors;
    }

    public List<SpecForwardConnector> getSpecForwardConnectors() {
        if (specForwardConnectors == null) {
            specForwardConnectors = new ArrayList<>();
        }
        return specForwardConnectors;
    }

    public void setSpecForwardConnectors(List<SpecForwardConnector> specForwardConnectors) {
        this.specForwardConnectors = specForwardConnectors;
    }

    public List<SenderAgent> getSenderAgents() {
        if (senderAgents == null) {
            senderAgents = new ArrayList<>();
        }
        return senderAgents;
    }

    public void setSenderAgents(List<SenderAgent> senderAgents) {
        this.senderAgents = senderAgents;
    }

    public List<ReceiverAgent> getReceiverAgents() {
        if (receiverAgents == null) {
            receiverAgents = new ArrayList<>();
        }
        return receiverAgents;
    }

    public void setReceiverAgents(List<ReceiverAgent> receiverAgents) {
        this.receiverAgents = receiverAgents;
    }

    public List<IdentifierEntity> getIdentifierEntities() {
        if (identifierEntities == null) {
            identifierEntities = new ArrayList<>();
        }
        return identifierEntities;
    }

    public void setIdentifierEntities(List<IdentifierEntity> identifierEntities) {
        this.identifierEntities = identifierEntities;
    }
}
