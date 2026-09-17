package org.commonprovenanceframework.cpm.template.deserialization.mou.schema;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class STS {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "diagnosisMaterial")
    private List<DiagnosisMaterial> diagnosisMaterials = new ArrayList<>();

    public List<DiagnosisMaterial> getDiagnosisMaterials() {
        return diagnosisMaterials;
    }

    public void setDiagnosisMaterials(List<DiagnosisMaterial> diagnosisMaterials) {
        this.diagnosisMaterials = diagnosisMaterials;
    }
}
