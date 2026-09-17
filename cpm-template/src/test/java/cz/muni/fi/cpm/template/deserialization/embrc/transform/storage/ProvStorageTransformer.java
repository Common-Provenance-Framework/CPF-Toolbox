package cz.muni.fi.cpm.template.deserialization.embrc.transform.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.divided.ordered.CpmOrderedFactory;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.template.deserialization.embrc.transform.jsonld.constants.JsonLDMetaConstants;
import cz.muni.fi.cpm.template.schema.HashAlgorithms;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import org.openprovenance.prov.core.json.serialization.SortedBundle;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.interop.Formats;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

public class ProvStorageTransformer {

    private final ProvFactory pF;
    private final CpmProvFactory cPF;
    private final ICpmFactory cF;

    private final InteropFramework interop;


    public ProvStorageTransformer(ProvFactory pF) {
        this.pF = pF;
        this.cPF = new CpmProvFactory(pF);
        this.cF = new CpmOrderedFactory(pF);
        this.interop = new InteropFramework(pF);
    }


    public void addFileReferenceToConnector(Entity con, String filePath, Formats.ProvFormat format) throws IOException {
        if (!CpmUtilities.isConnector(con)) {
            return;
        }

        InputStream iS = new FileInputStream(filePath);
        Document doc = (Formats.ProvFormat.JSON.equals(format)) ?
                readProvStorageCompatibleJSON(iS) :
                interop.readDocument(iS, format);

        CpmDocument cpmDoc = new CpmDocument(doc, pF, cPF, cF);

        QualifiedName metaBundleId = Optional.ofNullable(cpmDoc.getMainActivity())
                .flatMap(mA -> mA.getElements().stream()
                        .map(e -> CpmUtilities.getCpmAttributeValue(e, CpmAttribute.REFERENCED_META_BUNDLE_ID))
                        .filter(o -> o instanceof QualifiedName)
                        .map(o -> (QualifiedName) o)
                        .findFirst())
                .orElse(null);

        QualifiedName bundleId = cpmDoc.getBundleId();

        con.getOther().add(cPF.newCpmAttribute(CpmAttribute.REFERENCED_BUNDLE_ID, bundleId));

        con.getOther().add(cPF.newCpmAttribute(CpmAttribute.REFERENCED_META_BUNDLE_ID, metaBundleId));

        con.getOther().add(cPF.newCpmAttribute(CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE,
                getHashOfFile(filePath), pF.getName().XSD_STRING));

        con.getOther().add(cPF.newCpmAttribute(CpmAttribute.HASH_ALG,
                HashAlgorithms.SHA256.toString(), pF.getName().XSD_STRING));

    }


    private String getHashOfFile(String filePath) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(inputStream, digest)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                }
            }
            return String.format("%064x", new BigInteger(1, digest.digest()));
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public ByteArrayOutputStream writeProvStorageCompatibleJSON(Document doc) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        interop.writeDocument(outputStream, doc, Formats.ProvFormat.JSON);

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jNode = mapper.readTree(inputStream);

        // workaround for this issue: https://github.com/lucmoreau/ProvToolbox/issues/222
        ((ObjectNode) jNode.get("bundle").fields().next().getValue()).remove(JsonLDMetaConstants.JSONLD_ID);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writerWithDefaultPrettyPrinter().writeValue(baos, jNode);
        return baos;
    }

    public Document readProvStorageCompatibleJSON(InputStream iS) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jNode = mapper.readTree(iS);

        // workaround for this issue: https://github.com/lucmoreau/ProvToolbox/issues/222
        // String bundleId = jNode.get("bundle").fieldNames().next();
        // System.out.println("bundleId: " + bundleId);
        // ((ObjectNode) jNode.get("bundle").fields().next().getValue())
        //         .set(JsonLDMetaConstants.JSONLD_ID, new TextNode(bundleId));

        // escape explicit blank ns, because identifiers with blank ns uri are ignored by ProvToolBox
        String blankPrefix = jNode.get("prefix").properties().stream()
                .filter(e -> e.getValue() instanceof TextNode tN && SortedBundle.bnNS.equals(tN.asText()))
                .peek(e -> e.setValue(new TextNode("_" + SortedBundle.bnNS + "_")))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        InputStream is = new ByteArrayInputStream(jNode.toString().getBytes(StandardCharsets.UTF_8));

        Document doc = interop.readDocument(is, Formats.ProvFormat.JSON);
        Optional.ofNullable(blankPrefix).ifPresent(p -> {
            doc.getNamespace().unregister(p, "_" + SortedBundle.bnNS + "_");
            doc.getNamespace().register(p, SortedBundle.bnNS);
        });

        if (doc.getStatementOrBundle().getFirst() instanceof Bundle bun) {
            bun.setId(pF.newQualifiedName(doc.getNamespace().lookupPrefix(bun.getId().getPrefix()),
                    bun.getId().getLocalPart(), bun.getId().getPrefix()));
        }

        return doc;
    }

}
