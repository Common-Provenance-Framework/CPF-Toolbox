package cz.muni.fi.cpm.constants;

import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum CpmType {
    BACKWARD_CONNECTOR("backwardConnector"),
    FORWARD_CONNECTOR("forwardConnector"),
    SPEC_FORWARD_CONNECTOR("specForwardConnector"),
    IDENTIFIER("id"),
    MAIN_ACTIVITY("mainActivity"),
    SENDER_AGENT("senderAgent"),
    RECEIVER_AGENT("receiverAgent");

    public static final Set<String> STRING_VALUES = Arrays.stream(CpmType.values())
            .map(CpmType::toString)
            .collect(Collectors.toSet());

    public static final Map<CpmType, Kind> CPM_TYPE_TO_KIND = Map.of(
            BACKWARD_CONNECTOR, Kind.PROV_ENTITY,
            FORWARD_CONNECTOR, Kind.PROV_ENTITY,
            SPEC_FORWARD_CONNECTOR, Kind.PROV_ENTITY,
            IDENTIFIER, Kind.PROV_ENTITY,
            MAIN_ACTIVITY, Kind.PROV_ACTIVITY,
            SENDER_AGENT, Kind.PROV_AGENT,
            RECEIVER_AGENT, Kind.PROV_AGENT
    );

    private final String value;
    public static final Set<String> AGENTS = Set.of(SENDER_AGENT.toString(), RECEIVER_AGENT.toString());



    CpmType(String value) {
        this.value = value;
    }

    public static CpmType fromString(String value) {
        for (CpmType type : CpmType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public String toString() {
        return value;
    }
}
