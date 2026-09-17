package org.commonprovenanceframework.cpm.template.deserialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openprovenance.prov.core.json.serialization.deserial.CustomAttributeDeserializerWithRootName;
import org.openprovenance.prov.core.json.serialization.deserial.CustomKeyDeserializer;
import org.openprovenance.prov.model.Attribute;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class AttributesDeserializer extends JsonDeserializer<List<Attribute>> {

  private final CustomAttributeDeserializerWithRootName delegateValue;
  private final CustomKeyDeserializer delegateKey;

  AttributesDeserializer() {
    this.delegateValue = new CustomAttributeDeserializerWithRootName();
    this.delegateKey = new CustomKeyDeserializer();
  }

  @Override
  public List<Attribute> deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    Set<Attribute> result = new HashSet<>();
    Iterator<Map.Entry<String, JsonNode>> attrs = node.fields();
    while (attrs.hasNext()) {
      Map.Entry<String, JsonNode> pair = attrs.next();
      String key = pair.getKey();
      JsonNode value = pair.getValue();
      this.delegateKey.deserializeKey(key, deserializationContext);

      if (value.isArray()) {
        Iterator<JsonNode> elements = value.elements();
        while (elements.hasNext()) {
          JsonNode next = elements.next();
          result.add(delegateValue.deserialize(next, deserializationContext));
        }
      } else {
        result.add(delegateValue.deserialize(value, deserializationContext));
      }

    }

    return new ArrayList<>(result);
  }

}