# Example datasets

Real CPM bundles produced from two independent sources. These are the most complete worked examples in the repository — useful as a reference for what a finished bundle looks like beyond the small examples in the [README](../README.md).

## MMCI Dataset

The CPM files derived from the [MMCI XML test data](https://github.com/BBMRI-cz/fhir-module/blob/main/test/xml_data/MMCI_1.xml) are available in PROV-N format [here](https://github.com/Common-Provenance-Framework/CPF-Toolbox/tree/main/cpm-template/src/test/resources/mou). For each sample in the original XML data, two bundles are generated—**acquisition** and **storage**—and are stored in their respective directories.

To obtain the acquisition bundle in a different serialization format, rerun the [CpmMouTest](https://github.com/Common-Provenance-Framework/CPF-Toolbox/blob/main/cpm-template/src/test/java/cz/muni/fi/cpm/template/deserialization/mou/CpmMouTest.java) with the `OUTPUT_FORMAT_EXTENSION` variable set to the desired format.

> **Note:** These files are not directly compatible with Provenance Storage ([reference](https://is.muni.cz/auth/th/mo8f1/)). To enable compatibility, they must be transformed in a manner similar to the EMBRC dataset. For guidance, refer to the transformation logic implemented in [these classes](https://github.com/Common-Provenance-Framework/CPF-Toolbox/tree/main/cpm-template/src/test/java/cz/muni/fi/cpm/template/deserialization/embrc/transform/storage) and the test methods `transformCpmToProvStorageFormatV0` and `transformCpmToProvStorageFormatV1` in the [CpmEmbrcTest](https://github.com/Common-Provenance-Framework/CPF-Toolbox/blob/main/cpm-template/src/test/java/cz/muni/fi/cpm/template/deserialization/embrc/CpmEmbrcTest.java) class.

## EMBRC Dataset

The CPM representations of the four datasets developed by EMBRC ([source](https://github.com/vliz-be-opsci/embrc-prov-model)) are available in multiple formats [here](https://github.com/Common-Provenance-Framework/CPF-Toolbox/tree/main/cpm-template/src/test/resources/embrc). Each subdirectory corresponds to one dataset and includes the following:

* Original provenance files (suffixed with `_ProvenanceMetadata`)
* Transformed PROV JSON-LD files (suffixed with `_transformed`)
* CPM format files (suffixed with `_cpm`)
* Provenance Storage-compatible files (suffixed with `_storage_v0` and `_storage_v1`) ([see reference](https://is.muni.cz/auth/th/mo8f1/))

### Uploading to a Custom Provenance Storage Deployment
To upload these files to a Provenance Storage instance:

1. Upload all `_storage_v0` files in order (1 to 4).
2. Then upload all `_storage_v1` files in order.

Make sure the host component and organization identifier in the namespace IRIs matches your target Provenance Storage instance. If you're targeting a custom instance, update the IRIs in the [ProvStorageNamespaceConstants](https://github.com/Common-Provenance-Framework/CPF-Toolbox/blob/main/cpm-template/src/test/java/cz/muni/fi/cpm/template/deserialization/embrc/transform/storage/constants/ProvStorageNamespaceConstants.java) class and rerun the [CpmEmbrcTest](https://github.com/Common-Provenance-Framework/CPF-Toolbox/blob/main/cpm-template/src/test/java/cz/muni/fi/cpm/template/deserialization/embrc/CpmEmbrcTest.java) to regenerate the files accordingly.

When uploading files via a custom script, ensure the files are not altered in any way, as this will affect their content hash and prevent successful upload. For example, when reading the files in Python, use the following to preserve encoding and newline consistency:

```python
open(filename, encoding="utf-8", newline="")
```
