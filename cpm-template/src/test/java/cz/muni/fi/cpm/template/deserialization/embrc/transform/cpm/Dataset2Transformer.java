package cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm;

import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.template.schema.*;
import org.openprovenance.prov.model.*;

import java.util.List;

import static cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset1Transformer.*;
import static cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset3Transformer.SPECIES_IDENTIFICATION;

public class Dataset2Transformer extends DatasetTransformer {
    static final String PROCESSING = "Processing";
    static final String PROCESSED_SAMPLE_CON = "ProcessedSampleCon";

    private static final String IMAGES = "5017bc3edda5006a4c29737b48585fefdfd4ef740bed03ab10730ec86e4153d7";

    public Dataset2Transformer(ProvFactory pF, ICpmProvFactory cPF) {
        super(pF, cPF);
    }

    @Override
    protected Document createTI(IndexedDocument indexedDS) {
        TraversalInformation ti = new TraversalInformation();
        ti.setBundleName(newQNWithBlankNS(PROCESSING + "Bundle"));

        MainActivity mA = new MainActivity(newQNWithBlankNS(PROCESSING));
        ti.setMainActivity(mA);

        SenderAgent stationSenderAg = new SenderAgent(newQNWithBlankNS(NICE_MARINE_STATION));
//        SenderAgent stationSenderAg = new SenderAgent(newQNWithBlankNS("NiceMarineStationSender"));
        ti.setSenderAgents(List.of(stationSenderAg));

        ReceiverAgent stationAg = new ReceiverAgent(newQNWithBlankNS(NICE_MARINE_STATION));
//        ReceiverAgent stationAg = new ReceiverAgent(newQNWithBlankNS("NiceMarineStationReceiver"));
        ti.setReceiverAgents(List.of(stationAg));

        mA.setHasPart(indexedDS.getActivities().stream().map(Identifiable::getId).toList());

        BackwardConnector bC = new BackwardConnector(newQNWithBlankNS(STORED_SAMPLE_CON_R1));
        bC.setReferencedBundleId(newQNWithBlankNS(SAMPLING + "Bundle"));
        bC.setAttributedTo(new ConnectorAttributed(stationSenderAg.getId()));
        ti.setBackwardConnectors(List.of(bC));

        mA.setUsed(List.of(new MainActivityUsed(bC.getId())));

        ForwardConnector fC = new ForwardConnector(newQNWithBlankNS(PROCESSED_SAMPLE_CON));
        fC.setDerivedFrom(List.of(bC.getId()));

        SpecForwardConnector fCSpec = new SpecForwardConnector(newQNWithBlankNS(PROCESSED_SAMPLE_CON + "Spec"));
        fCSpec.setReferencedBundleId(newQNWithBlankNS(SPECIES_IDENTIFICATION + "Bundle"));
        fCSpec.setAttributedTo(new ConnectorAttributed(stationAg.getId()));
        fCSpec.setSpecializationOf(fC.getId());

        mA.setGenerated(List.of(fC.getId()));

        ti.setForwardConnectors(List.of(fC));
        ti.setSpecForwardConnectors(List.of(fCSpec));

        return mapper.toProvDocument(ti);
    }

    @Override
    protected void modifyDS(IndexedDocument indexedDS) {
    }

    @Override
    protected void addCrossPartRelations(Bundle bun) {
        SpecializationOf specBc = pF.newSpecializationOf(newQNWithBlankNS(SAMPLE_R1), newQNWithBlankNS(STORED_SAMPLE_CON_R1));
        bun.getStatement().add(specBc);

        SpecializationOf specFc = pF.newSpecializationOf(newQnWithGenNS(IMAGES), newQNWithBlankNS(PROCESSED_SAMPLE_CON));
        bun.getStatement().add(specFc);
    }
}
