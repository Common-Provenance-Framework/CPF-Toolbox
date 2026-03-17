package cz.muni.fi.cpm.constants;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum CpmAttribute {
    EXTERNAL_ID("externalId"),
    EXTERNAL_ID_TYPE("externalIdType"),
    HAS_ID("hasId"),
    DESCRIBED_OBJECT_TYPE("describedObjectType"),
    COMMENT("comment"),
    REFERENCED_BUNDLE_ID("referencedBundleId"),
    REFERENCED_META_BUNDLE_ID("referencedMetaBundleId"),
    REFERENCED_BUNDLE_SPECV("referencedBundleSpecV"),
    PROVENANCE_SERVICE_URI("provenanceServiceUri"),
    HASH_VALUE("hashValue"),
    REFERENCED_BUNDLE_HASH_VALUE("referencedBundleHashValue"),
    HASH_ALG("hashAlg"),
    CONTACT_ID_PID("contactIdPid");

    public static final Set<String> STRING_VALUES = Arrays.stream(CpmAttribute.values())
            .map(CpmAttribute::toString)
            .collect(Collectors.toSet());

    private final String value;

    CpmAttribute(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
