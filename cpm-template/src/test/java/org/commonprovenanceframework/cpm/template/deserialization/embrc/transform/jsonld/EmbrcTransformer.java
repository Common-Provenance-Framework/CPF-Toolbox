package org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.jsonld;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.commonprovenanceframework.cpm.exception.NoSpecificKind;
import org.commonprovenanceframework.cpm.model.ProvUtilities2;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.jsonld.constants.JsonLDMetaConstants.*;
import static org.openprovenance.prov.core.jsonld11.serialization.Constants.*;


public class EmbrcTransformer {

    private final Map<String, String> EMBRC_REL_TYPE_TO_JSONLD = Map.ofEntries(
            Map.entry("used", PROPERTY_PROV_USED),
            Map.entry("generated", PROPERTY_PROV_GENERATION),
            Map.entry("qualifiedAssociation", PROPERTY_PROV_ASSOCIATION),
            Map.entry("wasAssociatedWith", PROPERTY_PROV_ASSOCIATION),
            Map.entry("qualifiedAttribution", PROPERTY_PROV_ATTRIBUTION),
            Map.entry("wasAttributedTo", PROPERTY_PROV_ATTRIBUTION),
            Map.entry("wasInfluencedBy", PROPERTY_PROV_INFLUENCE)
    );

    private final Map<String, Kind> JSONLD_TYPE_TO_KIND = Map.ofEntries(
            Map.entry(PROPERTY_PROV_USED, Kind.PROV_USAGE),
            Map.entry(PROPERTY_PROV_GENERATION, Kind.PROV_GENERATION),
            Map.entry(PROPERTY_PROV_ASSOCIATION, Kind.PROV_ASSOCIATION),
            Map.entry(PROPERTY_PROV_ATTRIBUTION, Kind.PROV_ATTRIBUTION),
            Map.entry(PROPERTY_PROV_INFLUENCE, Kind.PROV_INFLUENCE)
    );

    private final Map<Kind, String> KIND_TO_JSONLD_ATTRIBUTE = Map.ofEntries(
            Map.entry(Kind.PROV_ENTITY, "entity"),
            Map.entry(Kind.PROV_AGENT, "agent"),
            Map.entry(Kind.PROV_ACTIVITY, "activity")
    );
    private final String INFLUENCER = "influencer";
    private final String INFLUENCEE = "influencee";

    private final Set<String> ARRAY_OF_VALUES_ATTRIBUTES = Set.of("value", "location", "label", "role");
    private final String QN_PATTERN = "^[A-Za-z0-9_]+:(.*)$";

    private final String GEN_PREFIX = "gen:";

    private final ObjectMapper mapper;
    private ObjectNode root = null;

    public EmbrcTransformer(ObjectMapper mapper, ObjectNode root) {
        this.mapper = mapper;
        this.root = root;
    }


    public ObjectNode toProvJsonLD() {
        root.putIfAbsent(JSONLD_GRAPH, root.remove(JSONLD_GRAPH));

        addDcatNs();

        List<JsonNode> nodesToAdd = new ArrayList<>();
        for (JsonNode jNode : root.get(JSONLD_GRAPH)) {
            if (!(jNode instanceof ObjectNode node)) {
                continue;
            }
            removeDuplicateRelations(node);
            transformProvRelations(node, nodesToAdd);
            inferUsedAndGenerated(node, nodesToAdd);
            extractCustomNodes(node, nodesToAdd);
        }

        nodesToAdd.forEach(entry -> ((ArrayNode) root.get(JSONLD_GRAPH)).add(entry));

        for (JsonNode jNode : root.get(JSONLD_GRAPH)) {
            if (!(jNode instanceof ObjectNode node)) {
                continue;
            }

            transformKnownAttributes(node);
            wrapCustomProperties(node);
            transformTypes(node);
            inferNsFromID(node);
            transformRole(node);
        }
        return root;
    }

    private void addDcatNs() {
        ((ObjectNode) root.get(JSONLD_CONTEXT).get(0)).set("dcat", new TextNode("http://www.w3.org/ns/dcat#"));
    }

    private void inferNsFromID(ObjectNode jNode) {
        if (!jNode.has(JSONLD_ID)) {
            return;
        }
        try {
            URL url = new URI(jNode.get(JSONLD_ID).asText()).toURL();

            String ns = url.toString().substring(0, url.toString().lastIndexOf("/") + 1);
            String[] host = url.getHost().split("\\.");
            String prefix = host[host.length - 2];
            String id = prefix + ":" + url.getPath().substring(url.getPath().lastIndexOf('/') + 1);

            ((ObjectNode) root.get(JSONLD_CONTEXT).get(0)).set(prefix, new TextNode(ns));
            jNode.set(JSONLD_ID, new TextNode(id));
            replaceValues(root, url.toString(), id);
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException ignored) {
        }
    }

    private JsonNode replaceValues(JsonNode node, String oldValue, String newValue) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            node.fieldNames().forEachRemaining(field ->
                    objNode.set(field, replaceValues(node.get(field), oldValue, newValue))
            );
            return objNode;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, replaceValues(arrayNode.get(i), oldValue, newValue));
            }
            return arrayNode;
        } else if (node.isTextual() && node.asText().equals(oldValue)) {
            return new TextNode(newValue);
        }
        return node;
    }

    private void removeDuplicateRelations(ObjectNode node) {
        if (node.has("wasAttributedTo") && node.has("qualifiedAttribution")) {
            node.remove("wasAttributedTo");
        }
        if (node.has("wasAssociatedWith") && node.has("qualifiedAssociation")) {
            node.remove("wasAssociatedWith");
        }
    }

    private void extractCustomNodes(ObjectNode node, List<JsonNode> nodesToAdd) {
        List<String> fieldsToModify = new ArrayList<>();
        for (var entry : node.properties()) {
            if (entry.getValue() instanceof ObjectNode &&
                    (Pattern.matches(QN_PATTERN, entry.getKey()) ||
                            ARRAY_OF_VALUES_ATTRIBUTES.contains(entry.getKey()))) {
                fieldsToModify.add(entry.getKey());
            }
        }

        for (String field : fieldsToModify) {
            ObjectNode oNode = (ObjectNode) node.get(field);
            ensurePresenceOfId(oNode);
            if (nodeNotInAllNodes(oNode, nodesToAdd)) {
                nodesToAdd.add(oNode);
            }
            node.set(field, mapper.createObjectNode().set(JSONLD_VALUE, oNode.get(JSONLD_ID)));
        }
    }

    private void wrapCustomProperties(ObjectNode node) {
        List<String> fieldsToModify = new ArrayList<>();
        for (var entry : node.properties()) {
            if (Pattern.matches(QN_PATTERN, entry.getKey()) ||
                    ARRAY_OF_VALUES_ATTRIBUTES.contains(entry.getKey())) {
                fieldsToModify.add(entry.getKey());
            }
        }

        for (String fieldName : fieldsToModify) {
            ArrayNode arrayNode = mapper.createArrayNode();
            JsonNode childNode = node.get(fieldName);

            if (childNode instanceof ArrayNode aNode) {
                for (JsonNode jNode : aNode) {
                    arrayNode.add(wrapCustomProperty(jNode));
                }
            } else {
                arrayNode.add(wrapCustomProperty(childNode));
            }
            node.set(fieldName, arrayNode);
        }
    }

    private JsonNode wrapCustomProperty(JsonNode childNode) {
        if (childNode.size() == 1 && childNode.has(JSONLD_ID)) {
            return childNode.get(JSONLD_ID);
        }

        if (childNode instanceof ValueNode) {
            return mapper.createObjectNode()
                    .set(JSONLD_VALUE, childNode);
        }

        if (childNode instanceof ObjectNode) {
            return childNode;
        }
        throw new IllegalStateException("We should not have any other types of nodes");
    }

    private void transformTypes(ObjectNode node) {
        if (node.has("type")) {
            return;
        }
        ArrayNode typeArray = mapper.createArrayNode();
        JsonNode provType = null;
        if (node.get(JSONLD_TYPE) instanceof TextNode tNode) {
            provType = modifyType(tNode, provType, typeArray);
        } else if (node.get(JSONLD_TYPE) instanceof ArrayNode aNode) {
            for (JsonNode type : aNode) {
                provType = modifyType(type, provType, typeArray);
            }
        }
        if (!typeArray.isEmpty()) {
            node.set("type", typeArray);
        }
        if (provType == null) {
            provType = new TextNode(PROPERTY_PROV_ENTITY);
        }
        node.set(JSONLD_TYPE, provType);
    }

    private JsonNode modifyType(JsonNode type, JsonNode provType, ArrayNode typeArray) {
        if (type.asText().equals("schema:Person") || type.asText().equals("schema:Organization")) {
            provType = new TextNode(PROPERTY_PROV_AGENT);
            typeArray.add(type);
        } else if (type.asText().equals("Location")) {
            provType = new TextNode(PROPERTY_PROV_ENTITY);
            typeArray.add(new TextNode("prov:Location"));
        } else if (Pattern.matches(QN_PATTERN, type.asText())) {
            typeArray.add(type);
        } else {
            provType = type;
        }
        return provType;
    }

    private void transformKnownAttributes(ObjectNode node) {
        transformKnownAttribute(node, "schema:startTime", "startTime");
        transformKnownAttribute(node, "schema:endTime", "endTime");
        transformKnownAttribute(node, "prov:location", "location");
    }

    private void transformKnownAttribute(ObjectNode node, String fieldName, String provName) {
        if (node.has(fieldName)) {
            node.putIfAbsent(provName, node.remove(fieldName));
        }
    }

    private void transformRole(ObjectNode node) {
        if (node.has("dcat:hadRole") && !PROPERTY_PROV_ATTRIBUTION.equals(node.get(JSONLD_TYPE).asText())) {
            node.putIfAbsent("role", node.remove("dcat:hadRole"));
        }
    }


    private void inferUsedAndGenerated(ObjectNode node, List<JsonNode> nodesToAdd) {
        if (!isOfType(node, PROPERTY_PROV_ACTIVITY.toLowerCase())) {
            return;
        }

        transformSchemaRelations(node, "schema:object", PROPERTY_PROV_USED, nodesToAdd);
        transformSchemaRelations(node, "schema:result", PROPERTY_PROV_GENERATION, nodesToAdd);
    }

    private void transformSchemaRelations(ObjectNode node, String propertyName, String propertyProvGeneration, List<JsonNode> nodesToAdd) {
        JsonNode resNode = node.get(propertyName);
        if (resNode == null) {
            return;
        }
        if (resNode instanceof ArrayNode aNode) {
            for (JsonNode childNode : aNode) {
                transformRelationWithElementInfo(propertyProvGeneration, node, nodesToAdd, (ObjectNode) childNode);
            }
        } else if (resNode instanceof ObjectNode childNode) {
            transformRelationWithElementInfo(propertyProvGeneration, node, nodesToAdd, childNode);
        }
        if (resNode instanceof ValueNode vNode) {
            ObjectNode oNode = mapper.createObjectNode();
            oNode.putIfAbsent("schema:description", vNode);
            transformRelationWithElementInfo(propertyProvGeneration, node, nodesToAdd, oNode);
        }
    }


    private void transformProvRelations(ObjectNode node, List<JsonNode> nodesToAdd) {
        ensurePresenceOfId(node);
        for (var entry : EMBRC_REL_TYPE_TO_JSONLD.entrySet()) {
            if (!node.has(entry.getKey())) {
                continue;
            }
            if (node.get(entry.getKey()) instanceof ObjectNode childNode) {
                if (containsRelationInformation(childNode)) {
                    transformRelationWithRelInfo(entry.getValue(), node, nodesToAdd, childNode);
                } else {
                    transformRelationWithElementInfo(entry.getValue(), node, nodesToAdd, childNode);
                }
                node.remove(entry.getKey());
            } else if (node.get(entry.getKey()) instanceof ArrayNode aNode) {
                for (JsonNode childNode : aNode) {
                    if (containsRelationInformation((ObjectNode) childNode)) {
                        transformRelationWithRelInfo(entry.getValue(), node, nodesToAdd, (ObjectNode) childNode);
                    } else {
                        transformRelationWithElementInfo(entry.getValue(), node, nodesToAdd, (ObjectNode) childNode);
                    }
                }
                node.remove(entry.getKey());
            }
        }
    }

    private boolean containsRelationInformation(ObjectNode node) {
        if (!node.has(JSONLD_TYPE)) {
            return true;
        }

        if (node.get(JSONLD_TYPE) instanceof TextNode tNode &&
                (EMBRC_REL_TYPE_TO_JSONLD.containsKey(tNode.asText()) ||
                        EMBRC_REL_TYPE_TO_JSONLD.containsValue(tNode.asText()))) {
            return true;
        }

        if (node.get(JSONLD_TYPE) instanceof ArrayNode aNode) {
            for (JsonNode childNode : aNode) {
                if (childNode instanceof TextNode tNode &&
                        (EMBRC_REL_TYPE_TO_JSONLD.containsKey(tNode.asText()) ||
                                EMBRC_REL_TYPE_TO_JSONLD.containsValue(tNode.asText()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void transformRelationWithRelInfo(String jsonLdType, ObjectNode node, List<JsonNode> nodesToAdd, ObjectNode childNode) {
        ensurePresenceOfId(childNode);

        ObjectNode relation = mapper.createObjectNode();
        String type = childNode.has(JSONLD_TYPE) ? childNode.get(JSONLD_TYPE).asText() : jsonLdType;
        if (EMBRC_REL_TYPE_TO_JSONLD.containsKey(type)) {
            type = EMBRC_REL_TYPE_TO_JSONLD.get(type);
        }
        relation.putIfAbsent(JSONLD_TYPE, new TextNode(type));

        setCauseAndEffect(jsonLdType, node, childNode, relation);

        for (var entry : childNode.properties()) {
            if (!JSONLD_ID.equals(entry.getKey())) {
                relation.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        nodesToAdd.add(relation);
    }

    private void setCauseAndEffect(String jsonLdType, ObjectNode node, ObjectNode childNode, ObjectNode relation) {
        String causeProperty = getCauseProperty(jsonLdType);
        String effectProperty = getEffectProperty(jsonLdType);
        if (isOfType(node, causeProperty)) {
            relation.putIfAbsent(causeProperty, new TextNode(node.get(JSONLD_ID).asText()));
            JsonNode effectId = childNode.has(effectProperty) ? childNode.get(effectProperty) : childNode.get(JSONLD_ID);
            relation.putIfAbsent(effectProperty, new TextNode(effectId.asText()));
        } else if (isOfType(node, effectProperty) || INFLUENCER.equals(causeProperty)) {
            JsonNode causeId = childNode.has(causeProperty) ? childNode.get(causeProperty) : childNode.get(JSONLD_ID);
            relation.putIfAbsent(causeProperty, new TextNode(causeId.asText()));
            relation.putIfAbsent(effectProperty, new TextNode(node.get(JSONLD_ID).asText()));
        } else {
            throw new IllegalStateException("Node is not of required type");
        }
    }

    private boolean isOfType(ObjectNode oNode, String type) {
        if (!oNode.has(JSONLD_TYPE)) {
            return false;
        }

        return StreamSupport.stream(oNode.get(JSONLD_TYPE).spliterator(), false)
                .anyMatch(node -> node.isTextual() && node.asText().toLowerCase().equals(type));
    }

    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private void ensurePresenceOfId(ObjectNode node) {
        if (!node.has(JSONLD_ID)) {
            if (node.has("schema:identifier")) {
                node.putIfAbsent(JSONLD_ID, new TextNode(node.get("schema:identifier").asText()));
            } else {
                String id = hashSHA256(node.toString());
                node.putIfAbsent(JSONLD_ID, new TextNode(GEN_PREFIX + id));
            }
        }
    }

    private void transformRelationWithElementInfo(String jsonLdType, ObjectNode node, List<JsonNode> nodesToAdd, ObjectNode childNode) {
        ensurePresenceOfId(childNode);

        if (childNode.size() != 1 && nodeNotInAllNodes(childNode, nodesToAdd)) {
            nodesToAdd.add(childNode);
        }

        transformProvRelations(childNode, nodesToAdd);

        ObjectNode relation = mapper.createObjectNode();
        relation.putIfAbsent(JSONLD_TYPE, new TextNode(jsonLdType));
        setCauseAndEffect(jsonLdType, node, childNode, relation);
        nodesToAdd.add(relation);
    }

    private boolean nodeNotInAllNodes(ObjectNode childNode, List<JsonNode> nodesToAdd) {
        Stream<JsonNode> allNodes = Stream.concat(
                StreamSupport.stream(root.get(JSONLD_GRAPH).spliterator(), false),
                nodesToAdd.stream()
        );

        return allNodes
                .map(n -> n.get(JSONLD_ID))
                .filter(Objects::nonNull)
                .noneMatch(id -> Objects.equals(id.asText(), childNode.get(JSONLD_ID).asText()));
    }

    private String getCauseProperty(String jsonLdType) {
        try {
            return KIND_TO_JSONLD_ATTRIBUTE.get(ProvUtilities2.getCauseKind(JSONLD_TYPE_TO_KIND.get(jsonLdType)));
        } catch (NoSpecificKind ignored) {
            return INFLUENCER;
        }
    }

    private String getEffectProperty(String jsonLdType) {
        try {
            return KIND_TO_JSONLD_ATTRIBUTE.get(ProvUtilities2.getEffectKind(JSONLD_TYPE_TO_KIND.get(jsonLdType)));
        } catch (NoSpecificKind ignored) {
            return INFLUENCEE;
        }
    }
}
