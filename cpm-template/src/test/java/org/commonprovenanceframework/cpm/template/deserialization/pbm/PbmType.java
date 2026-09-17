package org.commonprovenanceframework.cpm.template.deserialization.pbm;

public enum PbmType {
    SAMPLE("sample"),
    CONTAINER("container"),
    DEVICE("device"),
    REAGENT("reagent"),
    SOP("sop"),
    SOURCE("source"),
    DISTRIBUTION_ACTIVITY("distributionActivity"),
    RETRIEVAL_ACTIVITY("retrievalActivity"),
    STORAGE_ACTIVITY("storageActivity"),
    PROCESSING_ACTIVITY("processingActivity"),
    RECEIVEMENT_ACTIVITY("receivementActivity"),
    TRANSPORT_ACTIVITY("transportActivity"),
    ACQUISITION_ACTIVITY("acquisitionActivity");

    private final String value;

    PbmType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
