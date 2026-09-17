package org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.jsonld;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import static org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.jsonld.constants.JsonLDMetaConstants.JSONLD_CONTEXT;

public class ProvContextManager {
    private final ObjectMapper mapper;

    public ProvContextManager(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectNode enforce(String inputFile, String context) throws IOException, JsonLdError {
        JsonNode root = mapper.readTree(new FileInputStream(inputFile));
        for (var entry : root.get(JSONLD_CONTEXT).properties()) {
            if (Pattern.matches(".*[a-zA-Z0-9]$", entry.getValue().asText())) {
                ((ObjectNode) root.get(JSONLD_CONTEXT)).set(entry.getKey(), new TextNode(entry.getValue().asText() + "/"));
            }
        }
        JsonReader jsonReader = Json.createReader(new StringReader(root.toString()));

        JsonObject jsonObject = JsonLd.compact(JsonDocument.of(jsonReader.read()), JsonDocument.of(new FileInputStream(context)))
                .compactToRelative()
                .get();
        return (ObjectNode) mapper.readTree(jsonObject.toString());
    }
}
