package org.commonprovenanceframework.cpm.template.serialization;

import org.commonprovenanceframework.cpm.template.deserialization.ITraversalInformationDeserializer;
import org.commonprovenanceframework.cpm.template.deserialization.TraversalInformationDeserializer;
import org.commonprovenanceframework.cpm.template.mapper.ITemplateProvMapper;
import org.commonprovenanceframework.cpm.template.mapper.TemplateProvMapper;
import org.commonprovenanceframework.cpm.template.schema.TraversalInformation;
import org.commonprovenanceframework.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.vanilla.ProvFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.commonprovenanceframework.cpm.template.constants.PathConstants.TEST_RESOURCES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TraversalInformationSerializerTest {
  private static final String SERIALIZE_FOLDER = "serialization" + File.separator;

  @Test
  public void serializeTI_pure_deserializesSuccessfully() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    ProvFactory pF = new ProvFactory();
    CpmProvFactory cF = new CpmProvFactory(pF);
    ITemplateProvMapper mapper = new TemplateProvMapper(cF);

    try (InputStream inputStream = classLoader.getResourceAsStream(SERIALIZE_FOLDER + "test.json")) {
      ITraversalInformationDeserializer deserializer = new TraversalInformationDeserializer();
      TraversalInformation ti = deserializer.deserializeTI(inputStream);

      ITraversalInformationSerializer ser = new TraversalInformationSerializer();
      File output = new File(TEST_RESOURCES + SERIALIZE_FOLDER + "output.json");
      ser.serializeTI(ti, output);
      TraversalInformation serTI = deserializer.deserializeTI(new FileInputStream(output));

      assertEquals(mapper.toProvDocument(ti), mapper.toProvDocument(serTI));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}