package cz.muni.fi.cpm.template.deserialization.mou.transform.storage;

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

public class CpmStorageTransformer extends StorageTransformer {
  public CpmStorageTransformer(Patient patient, ProvFactory pF, ICpmProvFactory cPF, PbmFactory pbmF) {
    super(patient, pF, cPF, pbmF);
  }

  @Override
  protected Document createTI(String suffix) {
    TraversalInformation ti = new TraversalInformation();
    ti.setPrefixes(Map.of(BBMRI_PREFIX, BBMRI_NS, PBM_PREFIX, PBM_NS));
    ti.setBundleName(ti.getNamespace().qualifiedName(BBMRI_PREFIX, STORAGE + "Bundle" + suffix, pF));

    MainActivity mA = new MainActivity(ti.getNamespace().qualifiedName(BBMRI_PREFIX, STORAGE + suffix, pF));
    ti.setMainActivity(mA);

    QualifiedName aCID = ti.getNamespace().qualifiedName(BBMRI_PREFIX, ACQUISITION_CON + suffix, pF);

    BackwardConnector bC = new BackwardConnector(aCID);
    bC.setReferencedBundleId(ti.getNamespace().qualifiedName(BBMRI_PREFIX, ACQUISITION + "Bundle" + suffix, pF));
    ti.getBackwardConnectors().add(bC);

    MainActivityUsed used = new MainActivityUsed(aCID);
    mA.setUsed(List.of(used));

    QualifiedName fcID = ti.getNamespace().qualifiedName(BBMRI_PREFIX, STOR_CON + suffix, pF);
    mA.setGenerated(List.of(fcID));

    ForwardConnector fC = new ForwardConnector(fcID);
    fC.setDerivedFrom(List.of(bC.getId()));
    ti.getForwardConnectors().add(fC);

    QualifiedName agentId = ti.getNamespace().qualifiedName(BBMRI_PREFIX, "UNI", pF);
    ti.setSenderAgents(List.of(new SenderAgent(agentId)));

    bC.setAttributedTo(new ConnectorAttributed(agentId));

    return mapper.toProvDocument(ti);
  }

}
