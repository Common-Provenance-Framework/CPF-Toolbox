package org.commonprovenanceframework.cpm.divided.ordered;

import org.openprovenance.prov.model.Statement;

import java.util.IdentityHashMap;
import java.util.Map;

public class OrderManager {
    private final Map<Statement, Integer> OrderMap = new IdentityHashMap<>();
    private int nextOrder = 0;

    public void assignOrder(Statement statement) {
        OrderMap.putIfAbsent(statement, nextOrder++);
    }

    public int getOrder(Statement statement) {
        return OrderMap.getOrDefault(statement, Integer.MAX_VALUE);
    }
}

