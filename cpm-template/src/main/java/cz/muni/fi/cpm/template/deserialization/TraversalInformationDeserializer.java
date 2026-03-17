package cz.muni.fi.cpm.template.deserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import cz.muni.fi.cpm.template.mapper.ITemplateProvMapper;
import cz.muni.fi.cpm.template.mapper.TemplateProvMapper;
import cz.muni.fi.cpm.template.schema.TraversalInformation;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.openprovenance.prov.core.json.serialization.deserial.CustomQualifiedNameDeserializer;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.QualifiedName;

import java.io.IOException;
import java.io.InputStream;

import static org.openprovenance.prov.core.json.serialization.deserial.CustomThreadConfig.JSON_CONTEXT_KEY_NAMESPACE;
import static org.openprovenance.prov.core.json.serialization.deserial.CustomThreadConfig.getAttributes;

public class TraversalInformationDeserializer implements ITraversalInformationDeserializer {
    private final ObjectMapper mapper;
    private final ITemplateProvMapper templateMapper;

    public TraversalInformationDeserializer(ObjectMapper mapper, ITemplateProvMapper tM) {
        this.mapper = mapper;
        customize(mapper);
        this.templateMapper = tM;
    }

    public TraversalInformationDeserializer() {
        this(new ObjectMapper(), new TemplateProvMapper(new CpmProvFactory()));
    }

    @Override
    public TraversalInformation deserializeTI(InputStream in) throws IOException {
        getAttributes().get().remove(JSON_CONTEXT_KEY_NAMESPACE);

        return mapper.readValue(in, TraversalInformation.class);
    }

    @Override
    public Document deserializeDocument(InputStream in) throws IOException {
        return templateMapper.toProvDocument(deserializeTI(in));
    }

    private void customize(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("CustomKindDeserializer");

        module.addDeserializer(QualifiedName.class, new CustomQualifiedNameDeserializer());
        mapper.registerModule(module);
    }
}
