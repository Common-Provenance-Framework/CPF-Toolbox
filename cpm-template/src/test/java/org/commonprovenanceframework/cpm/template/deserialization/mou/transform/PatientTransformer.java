package org.commonprovenanceframework.cpm.template.deserialization.mou.transform;

import org.commonprovenanceframework.cpm.model.ICpmProvFactory;
import org.commonprovenanceframework.cpm.template.deserialization.mou.schema.DiagnosisMaterial;
import org.commonprovenanceframework.cpm.template.deserialization.mou.schema.Patient;
import org.commonprovenanceframework.cpm.template.deserialization.mou.schema.Tissue;
import org.commonprovenanceframework.cpm.template.deserialization.pbm.PbmFactory;
import org.commonprovenanceframework.cpm.template.mapper.ITemplateProvMapper;
import org.commonprovenanceframework.cpm.template.mapper.TemplateProvMapper;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.ValueConverter;

import java.util.ArrayList;
import java.util.List;


public abstract class PatientTransformer {

    public static final String BBMRI_NS = "http://www.bbmri.cz/schemas/biobank/data#";
    public static final String BBMRI_PREFIX = "bbmri";

    public static final String SUFFIX_TEMPLATE = "-%s-%s";

    protected final ProvFactory pF;
    protected final ICpmProvFactory cPF;
    protected final PbmFactory pbmF;
    protected final Patient patient;
    protected final ITemplateProvMapper mapper;

    public PatientTransformer(Patient patient, ProvFactory pF, ICpmProvFactory cPF, PbmFactory pbmF) {
        this.patient = patient;
        this.pF = pF;
        this.cPF = cPF;
        this.pbmF = pbmF;
        this.mapper = new TemplateProvMapper(cPF);
    }

    public List<Document> toDocuments() {
        List<Document> docs = new ArrayList<>();
        for (Tissue tissue : patient.getLts().getTissues()) {
            String suffix = String.format(SUFFIX_TEMPLATE, patient.getId(), tissue.getSampleId());
            Document doc = createTI(suffix);

            addTissueDSToDoc(doc, suffix, tissue);
            docs.add(doc);
        }

        for (DiagnosisMaterial dM : patient.getSts().getDiagnosisMaterials()) {
            String suffix = String.format(SUFFIX_TEMPLATE, patient.getId(), dM.getSampleId());
            Document doc = createTI(suffix);

            addDMDSToDoc(doc, suffix, dM);
            docs.add(doc);
        }
        return docs;
    }

    protected abstract Document createTI(String suffix);

    protected abstract void addTissueDSToDoc(Document doc, String suffix, Tissue tissue);

    protected abstract void addDMDSToDoc(Document doc, String suffix, DiagnosisMaterial dM);


    protected Other newOther(String qNLocal, Object o) {
        return pF.newOther(
                pF.newQualifiedName(BBMRI_NS, qNLocal, BBMRI_PREFIX),
                o,
                new ValueConverter(pF).getXsdType(o));
    }
}
