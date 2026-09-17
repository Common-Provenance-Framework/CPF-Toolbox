package org.commonprovenanceframework.cpm.template.deserialization.embrc;

import com.apicatalog.jsonld.JsonLdError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.commonprovenanceframework.cpm.merged.CpmMergedFactory;
import org.commonprovenanceframework.cpm.model.CpmDocument;
import org.commonprovenanceframework.cpm.model.ICpmFactory;
import org.commonprovenanceframework.cpm.model.ICpmProvFactory;
import org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.cpm.*;
import org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.jsonld.EmbrcTransformer;
import org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.jsonld.ProvContextManager;
import org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.storage.EmbrcProvStorageTransformer;
import org.commonprovenanceframework.cpm.template.util.GraphvizChecker;
import org.commonprovenanceframework.cpm.vanilla.CpmProvFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.interop.Formats;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static org.commonprovenanceframework.cpm.template.constants.PathConstants.TEST_RESOURCES;
import static org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.storage.EmbrcProvStorageTransformer.V0_SUFFIX;
import static org.commonprovenanceframework.cpm.template.deserialization.embrc.transform.storage.EmbrcProvStorageTransformer.V1_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CpmEmbrcTest {
    public static final String EMBRC_FOLDER = "embrc" + File.separator;

    private final ProvFactory pF;
    private final ICpmProvFactory cPF;
    private final ICpmFactory cF;

    public CpmEmbrcTest() {
        pF = new org.openprovenance.prov.vanilla.ProvFactory();
        cPF = new CpmProvFactory(pF);
        cF = new CpmMergedFactory(pF);
    }

    // 5 in TI for dataset 2, because sender and receiver agents are merged
    private static Stream<Object[]> documentProvider() {
        return Stream.of(
                new Object[]{Dataset1Transformer.class, 1, 9, 35, 2},
                new Object[]{Dataset2Transformer.class, 2, 5, 11, 2},
                new Object[]{Dataset3Transformer.class, 3, 5, 7, 2},
                new Object[]{Dataset4Transformer.class, 4, 4, 42, 2}
        );
    }

    @Order(0)
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    public void transformEmbrcToProvJsonLD(int datasetNum) throws IOException, JsonLdError {
        String datasetFolder = "dataset" + datasetNum + File.separator;
        String inputFile = TEST_RESOURCES + EMBRC_FOLDER + datasetFolder + "Dataset" + datasetNum + "_ProvenanceMetadata.jsonld";
        String outputFile = TEST_RESOURCES + EMBRC_FOLDER + datasetFolder + "Dataset" + datasetNum + "_transformed";
        String context = TEST_RESOURCES + EMBRC_FOLDER + "context.jsonld";

        ObjectMapper mapper = new ObjectMapper();

        ProvContextManager pCM = new ProvContextManager(mapper);
        ObjectNode root = pCM.enforce(inputFile, context);

        EmbrcTransformer transformer = new EmbrcTransformer(mapper, root);
        ObjectNode jsonLd = transformer.toProvJsonLD();

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile + ".jsonld"), jsonLd);

        try (InputStream inputStream = new FileInputStream(outputFile + ".jsonld")) {
            InteropFramework interop = new InteropFramework();
            Document doc = interop.readDocument(inputStream, Formats.ProvFormat.JSONLD);
            if (GraphvizChecker.isGraphvizInstalled()) {
                interop.writeDocument(outputFile + ".svg", doc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Order(1)
    @ParameterizedTest
    @MethodSource("documentProvider")
    public void toDocument_withEmbrcDataset_serialisesSuccessfully(Class<DatasetTransformer> dTClass,
                                                                   int datasetNum, int tiCount, int dsCount, int cPCount) {
        String datasetFolder = "dataset" + datasetNum + File.separator;
        try (InputStream inputStream = new FileInputStream(TEST_RESOURCES + EMBRC_FOLDER + datasetFolder + "Dataset" + datasetNum + "_transformed.jsonld")) {
            InteropFramework interop = new InteropFramework();
            Document dsDoc = interop.readDocument(inputStream, Formats.ProvFormat.JSONLD);
            DatasetTransformer dT = dTClass.getDeclaredConstructor(ProvFactory.class, ICpmProvFactory.class).newInstance(pF, cPF);

            Document doc = dT.toDocument(dsDoc);

            String fileName = TEST_RESOURCES + EMBRC_FOLDER + datasetFolder + "Dataset" + datasetNum + "_cpm";
            interop.writeDocument(fileName + ".jsonld", doc);
            if (GraphvizChecker.isGraphvizInstalled()) {
                interop.writeDocument(fileName + ".svg", doc);
            }

            CpmDocument cpmDoc = new CpmDocument(doc, pF, cPF, cF);
            assertEquals(tiCount, cpmDoc.getTraversalInformationPart().size());
            assertEquals(dsCount, cpmDoc.getDomainSpecificPart().size());
            assertEquals(cPCount, cpmDoc.getCrossPartEdges().size());
        } catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void transformCpmToProvStorageFormatV0(int datasetNum) throws IOException {
        String datasetFolder = "dataset" + datasetNum + File.separator;
        String filePath = TEST_RESOURCES + EMBRC_FOLDER + datasetFolder + "Dataset" + datasetNum + "_cpm";
        EmbrcProvStorageTransformer pST = new EmbrcProvStorageTransformer(pF);

        try (InputStream inputStream = new FileInputStream(filePath + ".jsonld")) {
            ByteArrayOutputStream outputStreamV0 = pST.transformToV0(inputStream);
            outputStreamV0.writeTo(new FileOutputStream(filePath + "_storage_v0.json"));
        }
    }

    @Order(4)
    @ParameterizedTest
    @ValueSource(ints = {4, 3, 2, 1})
    public void transformCpmToProvStorageFormatV1(int datasetNum) throws IOException {
        String datasetFolder = "dataset" + datasetNum + File.separator;
        String filePath = TEST_RESOURCES + EMBRC_FOLDER + datasetFolder + "Dataset" + datasetNum + "_cpm";
        EmbrcProvStorageTransformer pST = new EmbrcProvStorageTransformer(pF);

        try (InputStream inputStream = new FileInputStream(filePath + ".jsonld")) {
            String suffix = datasetNum == 1 || datasetNum == 2 ? V1_SUFFIX : V0_SUFFIX;
            ByteArrayOutputStream outputStreamV1 = pST.transformToV1(inputStream, suffix);
            outputStreamV1.writeTo(new FileOutputStream(filePath + "_storage" + suffix + ".json"));

        }
    }
}
