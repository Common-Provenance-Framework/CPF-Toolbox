package org.commonprovenanceframework.cpm.template.deserialization.mou.transform;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.template.expander.Expand;
import org.openprovenance.prov.template.json.Bindings;
import org.openprovenance.prov.template.json.Descriptors;
import org.openprovenance.prov.template.json.QDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ProvTemplateHandler {
    default Descriptors newQDescriptor(String id) {
        QDescriptor qd = new QDescriptor();
        qd.id = id;
        Descriptors d = new Descriptors();
        d.values = List.of(qd);
        return d;
    }

    default Document newDocument(ProvFactory pF, Bindings bindings, String templateName) {
        Expand expand = new Expand(pF, false, false);

        InteropFramework intF = new InteropFramework();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = classLoader.getResourceAsStream("mou" + File.separator + "prov" + File.separator + templateName + ".provn");
            org.openprovenance.prov.model.Document doc = expand.expander(intF.readDocument(is, Formats.ProvFormat.PROVN), bindings);

            Bundle bundle = (Bundle) doc.getStatementOrBundle().getFirst();
            Namespace ns = bundle.getNamespace();
            bundle.getNamespace().setParent(null);
            doc.setNamespace(ns);
            Namespace newBundleNs = pF.newNamespace();
            newBundleNs.setParent(ns);
            bundle.setNamespace(newBundleNs);

            return doc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
