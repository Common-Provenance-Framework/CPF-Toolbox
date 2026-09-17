package org.commonprovenanceframework.cpm.divided.ordered;

import org.commonprovenanceframework.cpm.divided.AbstractDividedFactory;
import org.commonprovenanceframework.cpm.model.Component;
import org.commonprovenanceframework.cpm.model.IEdge;
import org.commonprovenanceframework.cpm.model.INode;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.Statement;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CpmOrderedFactory extends AbstractDividedFactory {
    private final OrderManager oM;

    public CpmOrderedFactory() {
        this(new org.openprovenance.prov.vanilla.ProvFactory());
    }

    public CpmOrderedFactory(ProvFactory pF) {
        super(pF);
        this.oM = new OrderManager();
    }

    @Override
    protected IEdge processEdge(IEdge edge) {
        edge.getRelations().forEach(oM::assignOrder);
        return edge;
    }

    @Override
    protected INode processNode(INode node) {
        node.getElements().forEach(oM::assignOrder);
        return node;
    }

    @Override
    public Function<List<Component>, List<Statement>> getComponentsTransformer() {
        return statements -> statements.stream()
                .flatMap(x -> {
                    if (x instanceof INode n) {
                        return n.getElements().stream();
                    }
                    return ((IEdge) x).getRelations().stream();
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> Collections.newSetFromMap(new IdentityHashMap<>())),
                        Collection::stream
                ))
                .sorted((s1, s2) -> Integer.compare(oM.getOrder(s1), oM.getOrder(s2)))
                .toList();
    }
}
