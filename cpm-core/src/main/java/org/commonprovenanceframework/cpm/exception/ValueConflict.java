package org.commonprovenanceframework.cpm.exception;

public class ValueConflict extends RuntimeException {
    public ValueConflict(String id, String type, String value1, String value2) {
        super("Conflict between value '" + value1 + "' and value '" + value2 + "' of type '" + type + "' on element with id '" + id);
    }
}
