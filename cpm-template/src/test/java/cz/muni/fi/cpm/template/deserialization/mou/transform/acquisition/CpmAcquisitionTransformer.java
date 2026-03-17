package cz.muni.fi.cpm.template.deserialization.mou.transform.acquisition;

import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.template.deserialization.mou.schema.Patient;
import cz.muni.fi.cpm.template.deserialization.pbm.PbmFactory;
import cz.muni.fi.cpm.template.schema.*;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;

import java.util.List;
import java.util.Map;

import static cz.muni.fi.cpm.template.deserialization.mou.constants.NameConstants.*;
import static cz.muni.fi.cpm.template.deserialization.pbm.PbmNamespaceConstants.PBM_NS;
import static cz.muni.fi.cpm.template.deserialization.pbm.PbmNamespaceConstants.PBM_PREFIX;

public class CpmAcquisitionTransformer extends AcquisitionTransformer {

    public CpmAcquisitionTransformer(Patient patient, ProvFactory pF, ICpmProvFactory cPF, PbmFactory pbmF) {
        super(patient, pF, cPF, pbmF);
    }

    @Override
    protected Document createTI(String suffix) {
        TraversalInformation ti = new TraversalInformation();

        ti.setPrefixes(Map.of(BBMRI_PREFIX, BBMRI_NS, PBM_PREFIX, PBM_NS));
        ti.setBundleName(ti.getNamespace().qualifiedName(BBMRI_PREFIX, ACQUISITION + "Bundle" + suffix, pF));

        MainActivity mA = new MainActivity(ti.getNamespace().qualifiedName(BBMRI_PREFIX, ACQUISITION + suffix, pF));
        ti.setMainActivity(mA);

        QualifiedName fcID = ti.getNamespace().qualifiedName(BBMRI_PREFIX, ACQUISITION_CON + suffix, pF);
        mA.setGenerated(List.of(fcID));

        ForwardConnector fC = new ForwardConnector(fcID);
        ti.getForwardConnectors().add(fC);

        QualifiedName fcSpecId = ti.getNamespace().qualifiedName(BBMRI_PREFIX, ACQUISITION_CON + "Spec" + suffix, pF);
        SpecForwardConnector fCSpec = new SpecForwardConnector(fcSpecId);
        fCSpec.setSpecializationOf(fcID);
        fCSpec.setReferencedBundleId(ti.getNamespace().qualifiedName(BBMRI_PREFIX, STORAGE + "Bundle" + suffix, pF));
        ti.getSpecForwardConnectors().add(fCSpec);

        QualifiedName agentId = ti.getNamespace().qualifiedName(BBMRI_PREFIX, patient.getBiobank(), pF);
        ReceiverAgent rA = new ReceiverAgent(agentId);
        ti.setReceiverAgents(List.of(rA));

        fCSpec.setAttributedTo(new ConnectorAttributed(agentId));

        return mapper.toProvDocument(ti);
    }
}
