package org.commonprovenanceframework.cpm.exception;

public class NoSpecificKind extends Exception {
    public NoSpecificKind() {
        super("The specific kind for the associated relation is not defined");
    }
}
