package org.commonprovenanceframework.cpm.template.deserialization.mou.schema;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class LTS {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "tissue")
    private List<Tissue> tissues = new ArrayList<>();

    public List<Tissue> getTissues() {
        return tissues;
    }

    public void setTissues(List<Tissue> tissues) {
        this.tissues = tissues;
    }
}
