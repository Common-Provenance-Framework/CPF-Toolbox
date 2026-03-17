package cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm;

import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.template.schema.*;
import org.openprovenance.prov.model.*;

import java.util.List;

import static cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset1Transformer.SAMPLING;
import static cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset1Transformer.STORED_SAMPLE_CON_R1;
import static cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset2Transformer.PROCESSED_SAMPLE_CON;
import static cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset2Transformer.PROCESSING;

public class Dataset3Transformer extends DatasetTransformer {
    public static final String IDENTIFIED_SPECIES_CON = "IdentifiedSpeciesCon";
    static final String SPECIES_IDENTIFICATION = "SpeciesIdentification";
    private static final String IMAGES = "db2aef1f9e9cdf745897953efba7931099655ee21f323526decd0ad11b4cc0e0";
    private static final String RESULTS = "ac6f25ecc17ae20cbb5789ee654db6d401c771b4ad4c9e971a0d960c3b596ac4";

    public Dataset3Transformer(ProvFactory pF, ICpmProvFactory cPF) {
        super(pF, cPF);
    }

    @Override
    protected Document createTI(IndexedDocument indexedDS) {
        TraversalInformation ti = new TraversalInformation();
        ti.setBundleName(newQNWithBlankNS(SPECIES_IDENTIFICATION + "Bundle"));

        MainActivity mA = new MainActivity(newQNWithBlankNS(SPECIES_IDENTIFICATION));
        ti.setMainActivity(mA);

        SenderAgent stationAg = new SenderAgent(newQNWithBlankNS(NICE_MARINE_STATION));
        ti.setSenderAgents(List.of(stationAg));

        mA.setHasPart(indexedDS.getActivities().stream().map(Identifiable::getId).toList());

        BackwardConnector bC = new BackwardConnector(newQNWithBlankNS(PROCESSED_SAMPLE_CON));
        bC.setAttributedTo(new ConnectorAttributed(stationAg.getId()));
        bC.setReferencedBundleId(newQNWithBlankNS(PROCESSING + "Bundle"));

        mA.setUsed(List.of(new MainActivityUsed(bC.getId())));

        BackwardConnector bcStored = new BackwardConnector(newQNWithBlankNS(STORED_SAMPLE_CON_R1));
        bcStored.setReferencedBundleId(newQNWithBlankNS(SAMPLING + "Bundle"));
        bcStored.setAttributedTo(new ConnectorAttributed(stationAg.getId()));
        bC.setDerivedFrom(List.of(bcStored.getId()));

        ti.setBackwardConnectors(List.of(bC, bcStored));

        ForwardConnector fC = new ForwardConnector(newQNWithBlankNS(IDENTIFIED_SPECIES_CON));
        fC.setDerivedFrom(List.of(bC.getId()));
        ti.setForwardConnectors(List.of(fC));

        mA.setGenerated(List.of(fC.getId()));
        return mapper.toProvDocument(ti);
    }

    @Override
    protected void modifyDS(IndexedDocument indexedDS) {
    }

    @Override
    protected void addCrossPartRelations(Bundle bun) {
        SpecializationOf specBc = pF.newSpecializationOf(newQnWithGenNS(IMAGES), newQNWithBlankNS(PROCESSED_SAMPLE_CON));
        bun.getStatement().add(specBc);

        SpecializationOf specFc = pF.newSpecializationOf(newQnWithGenNS(RESULTS), newQNWithBlankNS(IDENTIFIED_SPECIES_CON));
        bun.getStatement().add(specFc);
    }
}
