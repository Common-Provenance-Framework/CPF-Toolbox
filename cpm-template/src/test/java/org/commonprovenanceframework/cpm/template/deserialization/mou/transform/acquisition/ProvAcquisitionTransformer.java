package org.commonprovenanceframework.cpm.template.deserialization.mou.transform.acquisition;

import org.commonprovenanceframework.cpm.model.ICpmProvFactory;
import org.commonprovenanceframework.cpm.template.deserialization.mou.schema.Patient;
import org.commonprovenanceframework.cpm.template.deserialization.mou.transform.ProvTemplateHandler;
import org.commonprovenanceframework.cpm.template.deserialization.pbm.PbmFactory;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.template.json.Bindings;

import java.util.Map;

import static org.commonprovenanceframework.cpm.constants.DctNamespaceConstants.DCT_NS;
import static org.commonprovenanceframework.cpm.constants.DctNamespaceConstants.DCT_PREFIX;
import static org.commonprovenanceframework.cpm.template.deserialization.mou.constants.NameConstants.*;
import static org.commonprovenanceframework.cpm.template.deserialization.pbm.PbmNamespaceConstants.PBM_NS;
import static org.commonprovenanceframework.cpm.template.deserialization.pbm.PbmNamespaceConstants.PBM_PREFIX;

public class ProvAcquisitionTransformer extends AcquisitionTransformer implements ProvTemplateHandler {

    public ProvAcquisitionTransformer(Patient patient, ProvFactory pF, ICpmProvFactory cPF, PbmFactory pbmF) {
        super(patient, pF, cPF, pbmF);
    }

    @Override
    protected Document createTI(String suffix) {
        Bindings bindings = new Bindings();
        bindings.var = Map.of(
                "main_activity_id", newQDescriptor(BBMRI_PREFIX + ":" + ACQUISITION + suffix),
                "forward_conn_id", newQDescriptor(BBMRI_PREFIX + ":" + ACQUISITION_CON + suffix),
                "receiver_id", newQDescriptor(BBMRI_PREFIX + ":" + patient.getBiobank()),
                "forward_conn_id_spec", newQDescriptor(BBMRI_PREFIX + ":" + ACQUISITION_CON + "Spec" + suffix),
                "bndl", newQDescriptor(BBMRI_PREFIX + ":" + ACQUISITION + "Bundle" + suffix),
                "ref_id", newQDescriptor(BBMRI_PREFIX + ":" + STORAGE + "Bundle" + suffix)
        );

        bindings.context = Map.of(
                BBMRI_PREFIX, BBMRI_NS, PBM_PREFIX, PBM_NS, DCT_PREFIX, DCT_NS
        );

        return newDocument(pF, bindings, "backbone_tmpl_acq");
    }
}
