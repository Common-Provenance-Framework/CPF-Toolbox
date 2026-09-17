package org.commonprovenanceframework.cpm.template.deserialization.mou.transform.storage;

import org.commonprovenanceframework.cpm.constants.CpmType;
import org.commonprovenanceframework.cpm.model.CpmUtilities;
import org.commonprovenanceframework.cpm.model.ICpmProvFactory;
import org.commonprovenanceframework.cpm.template.deserialization.mou.schema.DiagnosisMaterial;
import org.commonprovenanceframework.cpm.template.deserialization.mou.schema.Patient;
import org.commonprovenanceframework.cpm.template.deserialization.mou.schema.Tissue;
import org.commonprovenanceframework.cpm.template.deserialization.mou.transform.PatientTransformer;
import org.commonprovenanceframework.cpm.template.deserialization.mou.transform.ProvTemplateHandler;
import org.commonprovenanceframework.cpm.template.deserialization.pbm.PbmFactory;
import org.commonprovenanceframework.cpm.template.deserialization.pbm.PbmType;
import org.openprovenance.prov.model.*;

import java.util.List;
import java.util.function.Function;

import static org.commonprovenanceframework.cpm.constants.DctAttributeConstants.HAS_PART;
import static org.commonprovenanceframework.cpm.constants.DctNamespaceConstants.DCT_NS;
import static org.commonprovenanceframework.cpm.constants.DctNamespaceConstants.DCT_PREFIX;
import static org.commonprovenanceframework.cpm.template.deserialization.mou.constants.NameConstants.ACQUISITION_CON;
import static org.commonprovenanceframework.cpm.template.deserialization.mou.constants.NameConstants.STOR_CON;

public abstract class StorageTransformer extends PatientTransformer implements ProvTemplateHandler {
    public StorageTransformer(Patient patient, ProvFactory pF, ICpmProvFactory cPF, PbmFactory pbmF) {
        super(patient, pF, cPF, pbmF);
    }

    private void addStorageDSToDocument(Document doc, String suffix, String sampleId, Function<Entity, Void> populateStor) {
        Type sampleType = pbmF.newType(PbmType.SAMPLE);

        QualifiedName patientQN = pF.newQualifiedName(BBMRI_NS, "patient-" + patient.getId(), BBMRI_PREFIX);
        Entity patientE = pF.newEntity(patientQN);
        patientE.getOther().add(newOther("sex", patient.getSex()));
        patientE.getOther().add(newOther("birthYear", patient.getYear()));
        patientE.getOther().add(newOther("month", patient.getMonth()));
        patientE.getOther().add(newOther("gaveConsent", patient.isConsent()));
        patientE.getType().add(pbmF.newType(PbmType.SOURCE));

        QualifiedName transActivity = pF.newQualifiedName(BBMRI_NS, "transport" + suffix, BBMRI_PREFIX);
        Activity trans = pF.newActivity(transActivity);
        trans.getType().add(pbmF.newType(PbmType.TRANSPORT_ACTIVITY));

        Other sampleIdTransOther = newOther("sampleId", sampleId);
        QualifiedName sampleTransQN = pF.newQualifiedName(BBMRI_NS, "sampleTrans" + suffix, BBMRI_PREFIX);
        Entity sampleTrans = pF.newEntity(sampleTransQN, List.of(sampleType, sampleIdTransOther));

        WasDerivedFrom sampleTransDer = pF.newWasDerivedFrom(sampleTransQN, patientQN);

        SpecializationOf bcSpec = pF.newSpecializationOf(sampleTransQN, doc.getNamespace().qualifiedName(BBMRI_PREFIX, ACQUISITION_CON + suffix, pF));

        WasGeneratedBy transGeneratedBy = pF.newWasGeneratedBy(null, sampleTransQN, transActivity);

        QualifiedName storActivity = pF.newQualifiedName(BBMRI_NS, "storageAct" + suffix, BBMRI_PREFIX);
        Activity storAc = pF.newActivity(storActivity);
        storAc.getType().add(pbmF.newType(PbmType.STORAGE_ACTIVITY));

        Used storUsed = pF.newUsed(storActivity, sampleTransQN);

        Other sampleIdStorOther = newOther("sampleId", sampleId);
        QualifiedName sampleStorQN = pF.newQualifiedName(BBMRI_NS, "sampleStorage" + suffix, BBMRI_PREFIX);
        Entity sampleStor = pF.newEntity(sampleStorQN, List.of(sampleType, sampleIdStorOther));

        WasDerivedFrom sampleStorDer = pF.newWasDerivedFrom(sampleStorQN, patientQN);

        populateStor.apply(sampleStor);

        WasGeneratedBy storGeneratedBy = pF.newWasGeneratedBy(null, sampleStorQN, storActivity);

        SpecializationOf fcSpec = pF.newSpecializationOf(sampleStorQN, doc.getNamespace().qualifiedName(BBMRI_PREFIX, STOR_CON + suffix, pF));

        Bundle bundle = (Bundle) doc.getStatementOrBundle().getFirst();
        bundle.getStatement().addAll(List.of(bcSpec, trans, sampleTrans, transGeneratedBy, storAc, storUsed, sampleStor, storGeneratedBy, fcSpec, patientE, sampleTransDer, sampleStorDer));

        for (Statement s : bundle.getStatement()) {
            if (CpmUtilities.hasCpmType(s, CpmType.MAIN_ACTIVITY)) {
                Activity mainActivity = (Activity) s;
                mainActivity.getOther().add(pF.newOther(
                        pF.newQualifiedName(DCT_NS, HAS_PART, DCT_PREFIX),
                        transActivity,
                        pF.getName().PROV_QUALIFIED_NAME));
                mainActivity.getOther().add(pF.newOther(
                        pF.newQualifiedName(DCT_NS, HAS_PART, DCT_PREFIX),
                        storActivity,
                        pF.getName().PROV_QUALIFIED_NAME));
            }
        }
    }

    @Override
    protected void addTissueDSToDoc(Document doc, String suffix, Tissue tissue) {
        addStorageDSToDocument(doc, suffix, tissue.getSampleId(), entity ->
        {
            entity.getType().add(pF.newType(
                    pF.newQualifiedName(BBMRI_NS, "tissue", BBMRI_PREFIX),
                    pF.getName().PROV_QUALIFIED_NAME));
            entity.getOther().add(newOther("storageType", "LTS"));
            entity.getOther().add(newOther("year", tissue.getYear()));
            entity.getOther().add(newOther("samplesNo", tissue.getSamplesNo()));
            entity.getOther().add(newOther("availableSamplesNo", tissue.getAvailableSamplesNo()));
            entity.getOther().add(newOther("materialType", tissue.getMaterialType()));
            entity.getOther().add(newOther("pTNM", tissue.getpTNM()));
            entity.getOther().add(newOther("morphology", tissue.getMorphology()));
            entity.getOther().add(newOther("diagnosis", tissue.getDiagnosis()));
            entity.getOther().add(newOther("cutTime", tissue.getCutTime()));
            entity.getOther().add(newOther("freezeTime", tissue.getFreezeTime()));
            entity.getOther().add(newOther("retrieved", tissue.getRetrieved()));
            return null;
        });
    }

    @Override
    protected void addDMDSToDoc(Document doc, String suffix, DiagnosisMaterial dM) {
        addStorageDSToDocument(doc, suffix, dM.getSampleId(), entity ->
        {
            entity.getType().add(pF.newType(
                    pF.newQualifiedName(BBMRI_NS, "diagnosticMaterial", BBMRI_PREFIX),
                    pF.getName().PROV_QUALIFIED_NAME));
            entity.getOther().add(newOther("storageType", "STS"));
            entity.getOther().add(newOther("materialType", dM.getMaterialType()));
            entity.getOther().add(newOther("diagnosis", dM.getDiagnosis()));
            entity.getOther().add(newOther("takingDate", dM.getTakingDate()));
            entity.getOther().add(newOther("retrieved", dM.getRetrieved()));
            return null;
        });
    }

}
