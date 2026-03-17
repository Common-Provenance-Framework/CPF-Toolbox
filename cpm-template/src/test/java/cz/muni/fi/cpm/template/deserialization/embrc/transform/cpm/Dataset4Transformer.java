package cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm;

import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.template.schema.*;
import org.openprovenance.prov.model.*;

import java.util.List;
import java.util.Objects;

import static cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset1Transformer.*;

public class Dataset4Transformer extends DatasetTransformer {
    static final String DNA_SEQUENCING = "DnaSequencing";
    static final String FILTERED_SEQUENCES_CON = "FilteredSequencesCon";

    private static final String STORING_ACTIVITY_1 = "StoringActivity";
    private static final String STORING_ACTIVITY_2 = "StoringActivity2";
    private static final String TRANSPORTING_ACTIVITY = "TransportingDNAFiltersToSequencingIsUs";
    private static final String ACQUIRING_ACTIVITY = "MaterialAcquiringActivity1";
    private static final String M_PROCESSING_ACTIVITY = "MaterialProcessingActivity1";
    private static final String M_PROCESSING_ACTIVITY_4 = "MaterialProcessingActivity4";

    private static final String FILTERED_SEQUENCES = "6dff3f85a8f1fc9ddb2b58fd748861080efab7de4e1360c39d24feb8050a297c";
    private static final String DIGITAL_SEQUENCES_OBJ = "9f1c2750a08dc77b9fdb815d05a476f27af1131e3af5003b73d5d7d6c3503b47";
    private static final String DIGITAL_SEQUENCES_RES = "d02fd085b9e9d705a7c90797480b2357d2cbdddcdd9549a03318d1f9b2da7199";

    public Dataset4Transformer(ProvFactory pF, ICpmProvFactory cPF) {
        super(pF, cPF);
    }

    @Override
    protected Document createTI(IndexedDocument indexedDS) {
        TraversalInformation ti = new TraversalInformation();
        ti.setBundleName(newQNWithBlankNS(DNA_SEQUENCING + "Bundle"));

        MainActivity mA = new MainActivity(newQNWithBlankNS(DNA_SEQUENCING));
        ti.setMainActivity(mA);

        SenderAgent stationAg = new SenderAgent(newQNWithBlankNS(NICE_MARINE_STATION));
        ti.setSenderAgents(List.of(stationAg));

        mA.setHasPart(indexedDS.getActivities().stream().map(Identifiable::getId).toList());

        BackwardConnector bC = new BackwardConnector(newQNWithBlankNS(STORED_SAMPLE_CON_R2_3UM));
        bC.setReferencedBundleId(newQNWithBlankNS(SAMPLING + "Bundle"));
        bC.setAttributedTo(new ConnectorAttributed(stationAg.getId()));
        ti.setBackwardConnectors(List.of(bC));

        mA.setUsed(List.of(new MainActivityUsed(bC.getId())));

        ForwardConnector fC = new ForwardConnector(newQNWithBlankNS(FILTERED_SEQUENCES_CON));
        fC.setDerivedFrom(List.of(bC.getId()));
        ti.setForwardConnectors(List.of(fC));

        mA.setGenerated(List.of(fC.getId()));

        return mapper.toProvDocument(ti);
    }

    @Override
    protected void modifyDS(IndexedDocument indexedDS) {
        Entity r23um_stored1 = pF.newEntity(newQNWithBlankNS(SAMPLE_R2_3UM + "_stored1"));
        indexedDS.add(r23um_stored1);

        WasGeneratedBy genStored1 = pF.newWasGeneratedBy(null, r23um_stored1.getId(), newQNWithBlankNS(STORING_ACTIVITY_1));
        indexedDS.add(genStored1);

        Used usedStored = pF.newUsed(newQNWithBlankNS(TRANSPORTING_ACTIVITY), r23um_stored1.getId());
        indexedDS.add(usedStored);

        Entity r23um_transported = pF.newEntity(newQNWithBlankNS(SAMPLE_R2_3UM + "_transported"));
        indexedDS.add(r23um_transported);

        WasGeneratedBy genTrans = pF.newWasGeneratedBy(null, r23um_transported.getId(), newQNWithBlankNS(TRANSPORTING_ACTIVITY));
        indexedDS.add(genTrans);

        indexedDS.getUsed().removeIf(u ->
                Objects.equals(u.getEntity(), newQNWithBlankNS(SAMPLE_R2_3UM))
                        && Objects.equals(u.getActivity(), newQNWithBlankNS(TRANSPORTING_ACTIVITY)));

        Used usedAcq = pF.newUsed(newQNWithBlankNS(ACQUIRING_ACTIVITY), r23um_transported.getId());
        indexedDS.add(usedAcq);

        indexedDS.getUsed().removeIf(u ->
                Objects.equals(u.getEntity(), newQNWithBlankNS(SAMPLE_R2_3UM))
                        && Objects.equals(u.getActivity(), newQNWithBlankNS(ACQUIRING_ACTIVITY)));

        Entity r23um_acquired = pF.newEntity(newQNWithBlankNS(SAMPLE_R2_3UM + "_acquired"));
        indexedDS.add(r23um_acquired);

        WasGeneratedBy genAcq = pF.newWasGeneratedBy(null, r23um_acquired.getId(), newQNWithBlankNS(ACQUIRING_ACTIVITY));
        indexedDS.add(genAcq);

        Used usedStore2 = pF.newUsed(newQNWithBlankNS(STORING_ACTIVITY_2), r23um_acquired.getId());
        indexedDS.add(usedStore2);

        Entity r23um_stored2 = pF.newEntity(newQNWithBlankNS(SAMPLE_R2_3UM + "_stored2"));
        indexedDS.add(r23um_stored2);

        WasGeneratedBy genStored2 = pF.newWasGeneratedBy(null, r23um_stored2.getId(), newQNWithBlankNS(STORING_ACTIVITY_2));
        indexedDS.add(genStored2);

        indexedDS.getUsed().removeIf(u ->
                Objects.equals(u.getEntity(), newQNWithBlankNS(SAMPLE_R2_3UM))
                        && Objects.equals(u.getActivity(), newQNWithBlankNS(STORING_ACTIVITY_2)));

        Used usedProc1 = pF.newUsed(newQNWithBlankNS(M_PROCESSING_ACTIVITY), r23um_stored2.getId());
        indexedDS.add(usedProc1);

        Entity rawSequencesRes = indexedDS.getEntity(newQnWithGenNS(DIGITAL_SEQUENCES_RES));
        Entity rawSequencesObj = indexedDS.getEntity(newQnWithGenNS(DIGITAL_SEQUENCES_OBJ));
        rawSequencesObj.getOther().addAll(rawSequencesRes.getOther());

        WasGeneratedBy genProc4 = pF.newWasGeneratedBy(null, rawSequencesObj.getId(), newQNWithBlankNS(M_PROCESSING_ACTIVITY_4));
        indexedDS.add(genProc4);

        indexedDS.getWasGeneratedBy()
                .removeIf(w -> Objects.equals(w.getEntity(), rawSequencesRes.getId())
                        && Objects.equals(w.getActivity(), newQNWithBlankNS(M_PROCESSING_ACTIVITY_4)));
        indexedDS.getEntities().remove(rawSequencesRes);
    }

    @Override
    protected void addCrossPartRelations(Bundle bun) {
        SpecializationOf specBc = pF.newSpecializationOf(newQNWithBlankNS(SAMPLE_R2_3UM), newQNWithBlankNS(STORED_SAMPLE_CON_R2_3UM));
        bun.getStatement().add(specBc);

        SpecializationOf specFc = pF.newSpecializationOf(newQnWithGenNS(FILTERED_SEQUENCES), newQNWithBlankNS(FILTERED_SEQUENCES_CON));
        bun.getStatement().add(specFc);
    }
}
