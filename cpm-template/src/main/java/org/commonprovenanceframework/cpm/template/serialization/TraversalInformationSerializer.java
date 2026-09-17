package org.commonprovenanceframework.cpm.template.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.commonprovenanceframework.cpm.template.schema.TraversalInformation;
import org.openprovenance.prov.core.json.serialization.serial.CustomDateSerializer;
import org.openprovenance.prov.core.json.serialization.serial.CustomQualifiedNameSerializer;
import org.openprovenance.prov.vanilla.QualifiedName;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;

import static org.openprovenance.prov.core.json.serialization.deserial.CustomThreadConfig.JSON_CONTEXT_KEY_NAMESPACE;
import static org.openprovenance.prov.core.json.serialization.deserial.CustomThreadConfig.getAttributes;

public class TraversalInformationSerializer implements ITraversalInformationSerializer {
    private final ObjectMapper mapper;

    public TraversalInformationSerializer() {
        this(new ObjectMapper());
    }

    public TraversalInformationSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
        customize(mapper);
    }

    @Override
    public void serializeTI(TraversalInformation ti, File file) throws IOException {
        getAttributes().get().remove(JSON_CONTEXT_KEY_NAMESPACE);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, ti);
    }

    private void customize(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("CustomKindSerializer");
        module.addSerializer(QualifiedName.class, new CustomQualifiedNameSerializer());
        module.addSerializer(XMLGregorianCalendar.class, new CustomDateSerializer());
        mapper.registerModule(module);
    }
}
