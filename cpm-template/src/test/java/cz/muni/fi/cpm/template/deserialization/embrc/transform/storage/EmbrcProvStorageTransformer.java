package cz.muni.fi.cpm.template.deserialization.embrc.transform.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.divided.ordered.CpmOrderedFactory;
import cz.muni.fi.cpm.model.*;
import cz.muni.fi.cpm.template.deserialization.embrc.transform.cpm.Dataset3Transformer;
import cz.muni.fi.cpm.template.deserialization.embrc.transform.jsonld.constants.JsonLDMetaConstants;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.interop.Formats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cz.muni.fi.cpm.template.constants.PathConstants.TEST_RESOURCES;
import static cz.muni.fi.cpm.template.deserialization.embrc.CpmEmbrcTest.EMBRC_FOLDER;
import static cz.muni.fi.cpm.template.deserialization.embrc.transform.storage.constants.ProvStorageNamespaceConstants.*;

public class EmbrcProvStorageTransformer {

    public static final String V0_SUFFIX = "_V0";
    public static final String V1_SUFFIX = "_V1";

    private final Map<String, Integer> BUNDLE_ID_TO_DATASET = Map.of(
            "SamplingBundle", 1,
            "ProcessingBundle", 2,
            "SpeciesIdentificationBundle", 3,
            "DnaSequencingBundle", 4
    );

    private final ProvFactory pF;
    private final ICpmProvFactory cPF;
    private final ICpmFactory cF;
    private final InteropFramework interop;
    private final ProvStorageTransformer pST;

    private String metaNs;
    private String storageNs;
    private String metaPrefix;
    private String storagePrefix;

    public EmbrcProvStorageTransformer(ProvFactory pF) {
        this.pF = pF;
        this.cPF = new CpmProvFactory(pF);
        this.cF = new CpmOrderedFactory(pF);
        this.interop = new InteropFramework(pF);
        this.pST = new ProvStorageTransformer(pF);
        setNs(STORAGE_PREFIX, STORAGE_NS, META_PREFIX, META_NS);
    }

    /**
     * Sets custom namespace prefixes and IRIs for storage and metadata to generate files for a different Provenance
     * Storage instance
     *
     * @param storagePrefix the prefix to use for bundle identifiers
     * @param storageNs     the namespace IRI for a custom Provenance Storage instance
     * @param metaPrefix    the prefix to use for meta bundles
     * @param metaNs        the namespace IRI for meta bundles
     */
    public void setNs(String storagePrefix, String storageNs, String metaPrefix, String metaNs) {
        this.storagePrefix = storagePrefix;
        this.storageNs = storageNs;
        this.metaNs = metaNs;
        this.metaPrefix = metaPrefix;
    }

    private Document addMissingStorageAndMetaNs(CpmDocument cpmDoc, String suffix) {
        String origBunId = cpmDoc.getBundleId().getLocalPart();
        cpmDoc.setBundleId(pF.newQualifiedName(storageNs, origBunId + suffix, storagePrefix));

        cpmDoc.getMainActivity().getElements().forEach(e -> {
                    Activity mainActivity = (Activity) e;
                    mainActivity.getOther().add(cPF.newCpmAttribute(CpmAttribute.REFERENCED_META_BUNDLE_ID,
                            pF.newQualifiedName(metaNs, origBunId + EmbrcProvStorageTransformer.V0_SUFFIX + "_meta", metaPrefix)));
                }
        );

        return cpmDoc.toDocument();
    }

    private void setReferenceAttributes(List<INode> connectors) {
        connectors.forEach(n ->
                n.getElements().forEach(element -> {
                  Entity connector = Entity.class.cast(element);
                    connector.getOther().stream()
                            .filter(o -> CpmAttribute.REFERENCED_BUNDLE_ID.toString().equals(o.getElementName().getLocalPart()))
                            .findFirst().ifPresent(o -> {
                                QualifiedName val = (QualifiedName) o.getValue();
                                connector.getOther().remove(o);
                                try {
                                    pST.addFileReferenceToConnector(connector, getFilePath(val.getLocalPart()), Formats.ProvFormat.JSON);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
                }));
    }

    private String getFilePath(String bunIdentifier) {
        int datasetNum = BUNDLE_ID_TO_DATASET.get(bunIdentifier);
        String datasetFolder = "dataset" + datasetNum + File.separator;
        return TEST_RESOURCES + EMBRC_FOLDER + datasetFolder + "Dataset" + datasetNum + "_cpm_storage_v0.json";
    }

    public InputStream replaceBlankNs(InputStream stream) throws IOException {

        String original = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        String modified = original
                .replaceAll("_:", "blank:");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jNode = mapper.readTree(modified);
        ObjectNode context = ((ObjectNode) jNode.get(JsonLDMetaConstants.JSONLD_CONTEXT).get(0));
        context.set("blank", context.remove("_"));

        return new ByteArrayInputStream(jNode.toString().getBytes(StandardCharsets.UTF_8));
    }


    public ByteArrayOutputStream transformToV0(InputStream stream) throws IOException {
        InputStream iS = replaceBlankNs(stream);
        Document doc = interop.readDocument(iS, Formats.ProvFormat.JSONLD);
        ProvUtilities u = new ProvUtilities();

        Bundle bun = (Bundle) doc.getStatementOrBundle().getFirst();
        List<Statement> toRemove = new ArrayList<>();

        bun.getStatement().stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(CpmUtilities::isSpecForwardConnector)
                .forEach(toRemove::add);

        bun.getStatement().stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(CpmUtilities::isForwardConnector)
                .filter(e -> Dataset3Transformer.IDENTIFIED_SPECIES_CON.equals(e.getId().getLocalPart()))
                .findFirst().ifPresent(toRemove::add);

        List<QualifiedName> ids = toRemove.stream().map(e -> ((Identifiable) e).getId()).toList();

        bun.getStatement().stream()
                .filter(st -> SpecializationOf.class.isInstance(st)
                    || WasDerivedFrom.class.isInstance(st)
                    || WasAttributedTo.class.isInstance(st))
                .map(Relation.class::cast)
                .filter(r -> ids.contains(u.getEffect(r)))
                .forEach(toRemove::add);

        bun.getStatement().stream()
                .filter(Agent.class::isInstance)
                .map(Agent.class::cast)
                .filter(CpmUtilities::isReceiverAgent)
                .forEach(agent -> {
                    if (CpmUtilities.isSenderAgent(agent)) {
                      agent.getType().removeIf(t -> t.getValue() instanceof QualifiedName qN
                        && CpmType.RECEIVER_AGENT.toString().equals(qN.getLocalPart()));
                    } else {
                        toRemove.add(agent);
                    }
                });

        bun.getStatement().removeAll(toRemove);
        CpmDocument cpmDoc = new CpmDocument(doc, pF, cPF, cF);
        setReferenceAttributes(cpmDoc.getSpecForwardConnectors());

        setReferenceAttributes(cpmDoc.getBackwardConnectors());

        return pST.writeProvStorageCompatibleJSON(addMissingStorageAndMetaNs(cpmDoc, V0_SUFFIX));
    }

    public ByteArrayOutputStream transformToV1(InputStream stream, String suffix) throws IOException {
        InputStream iS = replaceBlankNs(stream);

        Document doc = interop.readDocument(iS, Formats.ProvFormat.JSONLD);
        CpmDocument cpmDoc = new CpmDocument(doc, pF, cPF, cF);
        setReferenceAttributes(cpmDoc.getSpecForwardConnectors());

        setReferenceAttributes(cpmDoc.getBackwardConnectors());

        return pST.writeProvStorageCompatibleJSON(addMissingStorageAndMetaNs(cpmDoc, suffix));
    }
}
