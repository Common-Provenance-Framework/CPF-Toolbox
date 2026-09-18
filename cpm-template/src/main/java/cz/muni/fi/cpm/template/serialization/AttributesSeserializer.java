package cz.muni.fi.cpm.template.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.vanilla.ProvFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AttributesSeserializer extends StdSerializer<Object> {

  AttributesSeserializer() {
    super(Object.class);
  }

  @Override
  public boolean isEmpty(SerializerProvider provider, Object value) {
    return value == null || !(value instanceof List<?> list) || list.isEmpty();
  }

  @Override
  public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {

    if (!(o instanceof List<?> list))
      throw new IOException("Expected a List<?>");

    jsonGenerator.writeStartObject();
    for (Entry<QualifiedName, Set<Attribute>> entry : toMap(list).entrySet()) {
      jsonGenerator.writeFieldName(serializeQN(entry.getKey()));

      Set<Attribute> attributeValues = entry.getValue();
      if (attributeValues.size() > 1)
        jsonGenerator.writeStartArray();

      for (Attribute attr : attributeValues) {
        if (attr.getValue() instanceof LangString langString)
          writeLangString(langString, jsonGenerator);
        else if (attr.getValue() instanceof QualifiedName qn)
          writeTypeObject(serializeQN(qn), attr.getType(), jsonGenerator);
        else if (attr.getType().equals(ProvFactory.getFactory().getName().XSD_STRING))
          jsonGenerator.writeString((String) attr.getValue());
        else if (attr.getType().equals(ProvFactory.getFactory().getName().XSD_BOOLEAN))
          jsonGenerator.writeBoolean(Boolean.parseBoolean((String) attr.getValue()));
        else if (attr.getType().equals(ProvFactory.getFactory().getName().XSD_INT)
            || attr.getType().equals(ProvFactory.getFactory().getName().XSD_INTEGER))
          jsonGenerator.writeNumber(Integer.parseInt((String) attr.getValue()));
        else {
          writeTypeObject((String) attr.getValue(), attr.getType(), jsonGenerator);
        }
      }

      if (attributeValues.size() > 1)
        jsonGenerator.writeEndArray();
    }
    jsonGenerator.writeEndObject();
  }

  private Map<QualifiedName, Set<Attribute>> toMap(List<?> items) {
    Map<QualifiedName, Set<Attribute>> result = new HashMap<>();
    items.stream()
        .filter(Attribute.class::isInstance)
        .map(Attribute.class::cast)
        .forEach(attr -> result.computeIfAbsent(attr.getElementName(), _ -> new java.util.HashSet<>()).add(attr));
    return result;
  }

  private String serializeQN(QualifiedName qn) {
    return qn.getPrefix() != null
        ? qn.getPrefix() + ":" + qn.getLocalPart()
        : qn.getLocalPart();
  }

  private void writeLangString(LangString langString, JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeFieldName("$");
    jsonGenerator.writeString(langString.getValue());
    if (langString.getLang() != null) {
      jsonGenerator.writeFieldName("lang");
      jsonGenerator.writeString(langString.getLang());
    } else {
      jsonGenerator.writeFieldName("type");
      jsonGenerator.writeString(serializeQN(ProvFactory.getFactory().getName().PROV_LANG_STRING));
    }
    jsonGenerator.writeEndObject();

  }

  private void writeTypeObject(String value, QualifiedName type, JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeFieldName("$");
    jsonGenerator.writeString(value);
    jsonGenerator.writeFieldName("type");
    jsonGenerator.writeString(serializeQN(type));
    jsonGenerator.writeEndObject();
  }

}