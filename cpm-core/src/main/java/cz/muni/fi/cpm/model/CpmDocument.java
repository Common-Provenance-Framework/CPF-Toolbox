package cz.muni.fi.cpm.model;

import cz.muni.fi.cpm.constants.CpmExceptionConstants;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.exception.NoSpecificKind;
import cz.muni.fi.cpm.strategy.AttributeTIStrategy;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.StatementOrBundle.Kind;
import org.openprovenance.prov.model.extension.QualifiedAlternateOf;
import org.openprovenance.prov.model.extension.QualifiedHadMember;
import org.openprovenance.prov.model.extension.QualifiedSpecializationOf;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Represents a CPM-specific document that models provenance information
 * while integrating with the PROV data model.
 * <p>
 * This class provides methods for constructing, transforming, and manipulating
 * a CPM document, allowing conversion to and from {@link Document} and CPM structures.
 * It maintains nodes ({@link INode}), edges ({@link IEdge}), and their relationships, ensuring proper
 * representation of provenance data as a traversable graph
 */
public class CpmDocument implements StatementAction {
    private static final ProvUtilities u = new ProvUtilities();
    private final ProvFactory pF;
    private final ICpmProvFactory cPF;
    private final ICpmFactory cF;

    private final Map<QualifiedName, Map<Kind, List<IEdge>>> effectEdges = new HashMap<>();
    private final Map<QualifiedName, Map<Kind, List<IEdge>>> causeEdges = new HashMap<>();
    private final Map<QualifiedName, Map<QualifiedName, List<IEdge>>> causeInfluences = new HashMap<>();
    private final Map<QualifiedName, Map<QualifiedName, List<IEdge>>> effectInfluences = new HashMap<>();

    private final Map<QualifiedName, Map<Kind, INode>> nodes = new HashMap<>();
    private final List<IEdge> edges = new ArrayList<>();
    private QualifiedName bundleId;

    private ITIStrategy tiStrategy = new AttributeTIStrategy();

    public CpmDocument(ProvFactory pF, ICpmProvFactory cPF, ICpmFactory cF) {
        this.pF = pF;
        this.cF = cF;
        this.cPF = cPF;
    }

    public CpmDocument(final Document document, ProvFactory provFactory, ICpmProvFactory cPF, ICpmFactory cF) {
        pF = provFactory;
        this.cPF = cPF;
        this.cF = cF;
        if (document == null) {
            throw new IllegalArgumentException(CpmExceptionConstants.NOT_NULL_DOCUMENT);
        }

        if (document.getStatementOrBundle().size() != 1 ||
                !(document.getStatementOrBundle().getFirst() instanceof Bundle docBundle)) {
            throw new IllegalArgumentException(CpmExceptionConstants.ONE_BUNDLE_REQUIRED);
        }

        this.bundleId = docBundle.getId();

        u.forAllStatement(docBundle.getStatement(), this);
    }

    public CpmDocument(final QualifiedName bundleId, List<INode> traversalInformationPart, List<INode> domainSpecificPart, List<IEdge> crossPartEdges, ProvFactory provFactory, ICpmProvFactory cPF, ICpmFactory cF) {
        this.pF = provFactory;
        this.cF = cF;
        this.cPF = cPF;
        this.bundleId = bundleId;

        if (bundleId == null || traversalInformationPart == null || domainSpecificPart == null || crossPartEdges == null) {
            throw new IllegalArgumentException(CpmExceptionConstants.NOT_NULL_PARTS);
        }

        addNodes(traversalInformationPart);
        addNodes(domainSpecificPart);
        addEdges(crossPartEdges);
    }

    public CpmDocument(List<Statement> traversalInformationPart, List<Statement> domainSpecificPart, List<Statement> crossPartEdges, final QualifiedName bundleId, ProvFactory provFactory, ICpmProvFactory cPF, ICpmFactory cF) {
        this.pF = provFactory;
        this.cF = cF;
        this.cPF = cPF;
        this.bundleId = bundleId;

        if (bundleId == null || traversalInformationPart == null || domainSpecificPart == null || crossPartEdges == null) {
            throw new IllegalArgumentException(CpmExceptionConstants.NOT_NULL_PARTS);
        }

        u.forAllStatement(traversalInformationPart, this);
        u.forAllStatement(domainSpecificPart, this);
        u.forAllStatement(crossPartEdges, this);
    }

    public CpmDocument(List<Statement> statements, final QualifiedName bundleId, ProvFactory provFactory, ICpmProvFactory cPF, ICpmFactory cF) {
        this.pF = provFactory;
        this.cF = cF;
        this.cPF = cPF;
        this.bundleId = bundleId;

        if (bundleId == null || statements == null) {
            throw new IllegalArgumentException(CpmExceptionConstants.NOT_NULL_PARTS);
        }

        u.forAllStatement(statements, this);
    }

    public void setTIStrategy(ITIStrategy strategy) {
        this.tiStrategy = strategy;
    }

    /**
     * Converts the current object to a {@link Document} representation.
     * This method creates a new {@link Document} and populates it with statements
     * from nodes and edges, and then wraps them in a new {@link Bundle}. The resulting
     * document includes the relevant gathered namespaces.
     *
     * @return the created {@link Document} containing the bundle and namespaces
     */
    public Document toDocument() {
        Document document = pF.newDocument();

        List<Component> components = new ArrayList<>();
        components.addAll(getNodes());
        components.addAll(edges);

        List<Statement> statements = cF.getComponentsTransformer().apply(components);

        Bundle newBundle = pF.newNamedBundle(bundleId, statements);
        Namespace bundleNs = pF.newNamespace();
        newBundle.setNamespace(bundleNs);

        document.getStatementOrBundle().add(newBundle);

        Namespace ns = Namespace.gatherNamespaces(newBundle);
        ns.extendWith(cPF.newCpmNamespace());
        document.setNamespace(ns);
        bundleNs.setParent(ns);

        return document;
    }


    private void addEdges(List<IEdge> edges) {
        for (IEdge edge : edges) {
            for (Relation r : edge.getRelations()) {
                u.doAction(r, this);
            }
        }
    }

    private void addEdge(Relation relation) {
        addEdge(cF.newEdge(relation), u.getEffect(relation), u.getCause(relation));
    }

    private void addEdge(HadMember hadMember) {
        for (IEdge existingEdge : edges) {
            if (ProvUtilities2.sameEdge(u, hadMember, existingEdge.getAnyRelation())) {
                existingEdge.handleDuplicate(hadMember);
            }
            return;
        }

        for (QualifiedName member : hadMember.getEntity()) {
            addEdge(cF.newEdgeWithoutCloning(hadMember), hadMember.getCollection(), member);
        }
    }

    private void addEdge(IEdge edge, QualifiedName effect, QualifiedName cause) {
        if (!Kind.PROV_MEMBERSHIP.equals(edge.getKind())) {
            for (IEdge existingEdge : edges) {
                if (ProvUtilities2.sameEdge(u, edge.getAnyRelation(), existingEdge.getAnyRelation())) {
                    existingEdge.handleDuplicate(edge.getAnyRelation());
                    return;
                }
            }
        }

        try {
            Kind nodeKind = ProvUtilities2.getEffectKind(edge.getKind());

            if (nodes.containsKey(effect) && nodes.get(effect).containsKey(nodeKind)) {
                Map<Kind, INode> kindToNodeMap = nodes.get(effect);
                INode node = kindToNodeMap.get(nodeKind);

                node.getEffectEdges().add(edge);
                edge.setEffect(node);
            } else {
                effectEdges.computeIfAbsent(effect, _ -> new HashMap<>())
                        .computeIfAbsent(nodeKind, _ -> new ArrayList<>())
                        .add(edge);
            }
        } catch (NoSpecificKind ignored) {
            // handled in doAction(WasInfluencedBy)
        }

        try {
            Kind nodeKind = ProvUtilities2.getCauseKind(edge.getKind());

            if (nodes.containsKey(cause) && nodes.get(cause).containsKey(nodeKind)) {
                Map<Kind, INode> kindToNodeMap = nodes.get(cause);
                INode node = kindToNodeMap.get(nodeKind);

                node.getCauseEdges().add(edge);
                edge.setCause(node);
            } else {
                causeEdges.computeIfAbsent(cause, _ -> new HashMap<>())
                        .computeIfAbsent(nodeKind, _ -> new ArrayList<>())
                        .add(edge);
            }
        } catch (NoSpecificKind ignored) {
            // handled in doAction(WasInfluencedBy)
        }

        edges.add(edge);
    }

    private void addNodes(List<INode> nodes) {
        for (INode node : nodes) {
            for (Element e : node.getElements()) {
                u.doAction(e, this);
            }
            addEdges(node.getCauseEdges());
        }
    }

    private void processEffectInfluence(QualifiedName effect, INode newNode) {
        if (!effectInfluences.containsKey(effect)) {
            return;
        }
        for (QualifiedName cause : effectInfluences.get(effect).keySet()) {
            List<IEdge> influences = effectInfluences.get(effect).get(cause);

            if (influences.getFirst().getEffect() == null) {
                for (IEdge edge : effectInfluences.get(effect).get(cause)) {
                    edge.setEffect(newNode);
                    newNode.getEffectEdges().add(edge);
                }
                continue;
            }

            for (INode causeNode : influences.stream().map(IEdge::getCause).distinct().toList()) {
                IEdge edge = cF.newEdgeWithoutCloning(influences.getFirst());
                edge.setEffect(newNode);
                newNode.getEffectEdges().add(edge);
                if (causeNode != null) {
                    edge.setCause(causeNode);
                    causeNode.getCauseEdges().add(edge);
                }
                influences.add(edge);
                causeInfluences.get(cause).get(effect).add(edge);
                edges.add(edge);
            }
        }
    }


    private void processCauseInfluence(QualifiedName cause, INode newNode) {
        if (!causeInfluences.containsKey(cause)) {
            return;
        }
        for (QualifiedName effect : causeInfluences.get(cause).keySet()) {
            List<IEdge> influences = causeInfluences.get(cause).get(effect);

            if (influences.getFirst().getCause() == null) {
                for (IEdge edge : causeInfluences.get(cause).get(effect)) {
                    edge.setCause(newNode);
                    newNode.getCauseEdges().add(edge);
                }
                continue;
            }

            for (INode effectNode : influences.stream().map(IEdge::getEffect).distinct().toList()) {
                IEdge edge = cF.newEdgeWithoutCloning(influences.getFirst());
                edge.setCause(newNode);
                newNode.getCauseEdges().add(edge);
                if (effectNode != null) {
                    edge.setEffect(effectNode);
                    effectNode.getEffectEdges().add(edge);
                }
                influences.add(edge);
                effectInfluences.get(effect).get(cause).add(edge);
                edges.add(edge);
            }
        }
    }

    private void addNode(Element element) {
        INode newNode = cF.newNode(element);
        QualifiedName id = element.getId();
        Kind kind = element.getKind();

        INode existingNode = getNode(id, kind);
        if (existingNode != null) {
            existingNode.handleDuplicate(newNode.getAnyElement());
            return;
        }

        addCompletelyNewNode(id, kind, newNode);
    }

    private void addCompletelyNewNode(QualifiedName id, Kind kind, INode newNode) {
        nodes.computeIfAbsent(id, _ -> new HashMap<>())
                .putIfAbsent(kind, newNode);

        processCauseAndEffectEdges(id, kind, newNode);

        processCauseInfluence(id, newNode);
        processEffectInfluence(id, newNode);
    }

    private void processCauseAndEffectEdges(QualifiedName id, Kind kind, INode newNode) {
        if (effectEdges.containsKey(id) && effectEdges.get(id).containsKey(kind)) {
            List<IEdge> edgesToUpdate = effectEdges.get(id).get(kind);
            for (IEdge edge : edgesToUpdate) {
                edge.setEffect(newNode);
                newNode.getEffectEdges().add(edge);
            }
            effectEdges.get(id).remove(kind);
            if (effectEdges.get(id).isEmpty()) {
                effectEdges.remove(id);
            }
        }

        if (causeEdges.containsKey(id) && causeEdges.get(id).containsKey(kind)) {
            List<IEdge> edgesToUpdate = causeEdges.get(id).get(kind);
            for (IEdge edge : edgesToUpdate) {
                edge.setCause(newNode);
                newNode.getCauseEdges().add(edge);
            }
            causeEdges.get(id).remove(kind);
            if (causeEdges.get(id).isEmpty()) {
                causeEdges.remove(id);
            }
        }
    }

    @Override
    public void doAction(Activity activity) {
        addNode(activity);
    }

    @Override
    public void doAction(Used used) {
        addEdge(used);
    }

    @Override
    public void doAction(WasStartedBy wasStartedBy) {
        addEdge(wasStartedBy);
    }

    @Override
    public void doAction(Agent agent) {
        addNode(agent);
    }

    @Override
    public void doAction(AlternateOf alternateOf) {
        addEdge(alternateOf);
    }

    @Override
    public void doAction(WasAssociatedWith wasAssociatedWith) {
        addEdge(wasAssociatedWith);
    }

    @Override
    public void doAction(WasAttributedTo wasAttributedTo) {
        addEdge(wasAttributedTo);
    }

    @Override
    public void doAction(WasInfluencedBy wasInfluencedBy) {
        QualifiedName effect = u.getEffect(wasInfluencedBy);
        QualifiedName cause = u.getCause(wasInfluencedBy);
        WasInfluencedBy clonedInfluence = pF.newStatement(wasInfluencedBy);

        for (IEdge existingEdge : edges) {
            if (ProvUtilities2.sameEdge(u, clonedInfluence, existingEdge.getAnyRelation())) {
                existingEdge.handleDuplicate(clonedInfluence);
            }
            return;
        }

        List<INode> effectNodes = new ArrayList<>();
        List<INode> causeNodes = new ArrayList<>();
        List<IEdge> finalEdges = new ArrayList<>();

        if (nodes.containsKey(effect)) {
            effectNodes.addAll(nodes.get(effect).values());
        }

        if (nodes.containsKey(cause)) {
            causeNodes.addAll(nodes.get(cause).values());
        }

        if (effectNodes.isEmpty() && causeNodes.isEmpty()) {
            IEdge edge = cF.newEdgeWithoutCloning(clonedInfluence);
            finalEdges.add(edge);
        } else if (effectNodes.isEmpty()) {
            for (INode causeNode : causeNodes) {
                IEdge edge = cF.newEdgeWithoutCloning(clonedInfluence);
                edge.setCause(causeNode);
                causeNode.getCauseEdges().add(edge);
                finalEdges.add(edge);
            }
        } else if (causeNodes.isEmpty()) {
            for (INode effectNode : effectNodes) {
                IEdge edge = cF.newEdgeWithoutCloning(clonedInfluence);
                edge.setEffect(effectNode);
                effectNode.getEffectEdges().add(edge);
                finalEdges.add(edge);
            }
        } else {
            for (INode effectNode : effectNodes) {
                for (INode causeNode : causeNodes) {
                    IEdge edge = cF.newEdgeWithoutCloning(clonedInfluence);
                    edge.setEffect(effectNode);
                    effectNode.getEffectEdges().add(edge);
                    edge.setCause(causeNode);
                    causeNode.getCauseEdges().add(edge);
                    finalEdges.add(edge);
                }
            }
        }

        for (IEdge edge : finalEdges) {
            causeInfluences.computeIfAbsent(cause, _ -> new HashMap<>())
                    .computeIfAbsent(effect, _ -> new ArrayList<>())
                    .add(edge);
            effectInfluences.computeIfAbsent(effect, _ -> new HashMap<>())
                    .computeIfAbsent(cause, _ -> new ArrayList<>())
                    .add(edge);
            edges.add(edge);
        }
    }

    @Override
    public void doAction(ActedOnBehalfOf actedOnBehalfOf) {
        addEdge(actedOnBehalfOf);
    }

    @Override
    public void doAction(WasDerivedFrom wasDerivedFrom) {
        addEdge(wasDerivedFrom);
    }

    @Override
    public void doAction(DictionaryMembership dictionaryMembership) {
        throw new UnsupportedOperationException(CpmExceptionConstants.NOT_SUPPORTED);
    }

    @Override
    public void doAction(DerivedByRemovalFrom derivedByRemovalFrom) {
        throw new UnsupportedOperationException(CpmExceptionConstants.NOT_SUPPORTED);
    }

    @Override
    public void doAction(WasEndedBy wasEndedBy) {
        addEdge(wasEndedBy);
    }

    @Override
    public void doAction(Entity entity) {
        addNode(entity);
    }

    @Override
    public void doAction(WasGeneratedBy wasGeneratedBy) {
        addEdge(wasGeneratedBy);
    }

    @Override
    public void doAction(WasInvalidatedBy wasInvalidatedBy) {
        addEdge(wasInvalidatedBy);
    }

    @Override
    public void doAction(HadMember hadMember) {
        HadMember clonedHadMember = pF.newStatement(hadMember);

        addEdge(clonedHadMember);
    }

    @Override
    public void doAction(MentionOf mentionOf) {
        addEdge(mentionOf);
    }

    @Override
    public void doAction(SpecializationOf specializationOf) {
        addEdge(specializationOf);
    }

    @Override
    public void doAction(QualifiedSpecializationOf qualifiedSpecializationOf) {
        addEdge(qualifiedSpecializationOf);
    }

    @Override
    public void doAction(QualifiedAlternateOf qualifiedAlternateOf) {
        addEdge(qualifiedAlternateOf);
    }

    @Override
    public void doAction(QualifiedHadMember qualifiedHadMember) {
        doAction((HadMember) qualifiedHadMember);
    }

    @Override
    public void doAction(DerivedByInsertionFrom derivedByInsertionFrom) {
        throw new UnsupportedOperationException(CpmExceptionConstants.NOT_SUPPORTED);
    }

    @Override
    public void doAction(WasInformedBy wasInformedBy) {
        addEdge(wasInformedBy);
    }

    @Override
    public void doAction(Bundle bundle, ProvUtilities provUtilities) {
        throw new UnsupportedOperationException(CpmExceptionConstants.NOT_SUPPORTED);
    }

    /**
     * Gathers the namespaces from the current set of nodes and edges, and extends them with CPM and PROV namespaces.
     *
     * @return the extended Namespace that includes the gathered namespaces, CPM namespace and PROV namespace
     */
    public Namespace getNamespaces() {
        List<StatementOrBundle> statements = new ArrayList<>();
        statements.addAll(getNodes().stream().flatMap(x -> x.getElements().stream()).distinct().toList());
        statements.addAll(edges.stream().flatMap(x -> x.getRelations().stream()).distinct().toList());

        NamespaceGatherer gatherer = new NamespaceGatherer();
        u.forAllStatementOrBundle(statements, gatherer);
        Namespace ns = gatherer.getNamespace();
        ns.extendWith(cPF.newCpmNamespace());
        return ns;
    }

    /**
     * Checks if all relations are mapped. When all relations are mapped, both effect and cause nodes on all relations are not {@code null}
     *
     * @return {@code true} if all relations are mapped;
     * {@code false} otherwise
     */
    public boolean areAllRelationsMapped() {
        return edges.stream().allMatch(x -> x.getCause() != null && x.getEffect() != null);
    }


    /**
     * Retrieves the main activity node from the traversal information.
     * This method iterates through the nodes in the traversal information and returns the first node
     * that has the {@link CpmType#MAIN_ACTIVITY} type. If no such node is found, it returns {@code null}.
     *
     * @return the main activity node, or {@code null} if not found
     */
    public INode getMainActivity() {
        for (INode node : getNodes()) {
            if (tiStrategy.belongsToTraversalInformation(node) &&
                    CpmUtilities.hasCpmType(node, CpmType.MAIN_ACTIVITY)) {
                return node;
            }
        }
        return null;
    }

    private List<INode> getConnectors(CpmType type) {
        List<INode> result = new ArrayList<>();
        for (INode node : getNodes()) {
            if (tiStrategy.belongsToTraversalInformation(node) &&
                    CpmUtilities.hasCpmType(node, type)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Retrieves a list of forward connector nodes from the traversal information.
     *
     * @return a list of forward connector nodes
     */
    public List<INode> getForwardConnectors() {
        return getConnectors(CpmType.FORWARD_CONNECTOR);
    }

    /**
     * Retrieves a list of specialized forward connector nodes from the traversal information.
     *
     * @return a list of specialized forward connector nodes
     */
    public List<INode> getSpecForwardConnectors() {
        return getConnectors(CpmType.SPEC_FORWARD_CONNECTOR);
    }

    /**
     * Retrieves a list of backward connector nodes from the traversal information.
     *
     * @return a list of backward connector nodes
     */
    public List<INode> getBackwardConnectors() {
        return getConnectors(CpmType.BACKWARD_CONNECTOR);
    }


    private List<INode> getFilteredNodes(Predicate<INode> nodeFilter) {
        Map<INode, INode> clonedNodeMap = getNodes().stream()
                .filter(nodeFilter)
                .collect(Collectors.toMap(node -> node, cF::newNode));

        List<IEdge> edges = clonedNodeMap.keySet().stream()
                .flatMap(n -> n.getCauseEdges().stream()
                        .filter(e -> clonedNodeMap.containsKey(e.getEffect())))
                .toList();

        for (IEdge edge : edges) {
            IEdge clonedEdge = cF.newEdge(edge);
            clonedEdge.setCause(clonedNodeMap.get(edge.getCause()));
            clonedEdge.getCause().getCauseEdges().add(clonedEdge);
            clonedEdge.setEffect(clonedNodeMap.get(edge.getEffect()));
            clonedEdge.getEffect().getEffectEdges().add(clonedEdge);
        }

        return clonedNodeMap.values().stream().toList();
    }

    /**
     * Creates a clone of each node in the traversal information part of the document, keeping only relations that are between TI nodes.
     *
     * @return a list of cloned TI nodes
     */
    public List<INode> getTraversalInformationPart() {
        return getFilteredNodes(tiStrategy::belongsToTraversalInformation);
    }

    /**
     * Creates a clone of each node in the domain specific part of the document, keeping only relations that are between DS nodes.
     *
     * @return a list of cloned domain specific nodes
     */
    public List<INode> getDomainSpecificPart() {
        return getFilteredNodes(Predicate.not(tiStrategy::belongsToTraversalInformation));
    }

    /**
     * Retrieves all original edges that are between traversal information nodes and domain specific nodes.
     *
     * @return a list of original cross part edges
     */
    public List<IEdge> getCrossPartEdges() {
        Map<INode, Boolean> nodeTIMap = getNodes().stream()
                .collect(Collectors.toMap(node -> node, n -> tiStrategy.belongsToTraversalInformation(n)));

        return edges.stream().filter(e -> {
            if (e.getEffect() == null || e.getCause() == null) {
                return false;
            }
            boolean isCauseTI = nodeTIMap.get(e.getCause());
            boolean isEffectTI = nodeTIMap.get(e.getEffect());
            return ((isCauseTI && !isEffectTI) || (!isCauseTI && isEffectTI));
        }).toList();
    }

    /**
     * Removes a node with the specified ID and kind from the graph structure.
     * <p>
     * This method removes the node identified by the given {@code id} and {@code kind}.
     * It removes the node from the graph and its associated parts and updates the corresponding collections
     *
     * @param id   the qualified name of the node to be removed
     * @param kind the kind of the element within the node
     * @return {@code true} if the node was successfully removed; {@code false} otherwise
     */
    public boolean removeNode(QualifiedName id, Kind kind) {
        INode node = getNode(id, kind);
        if (node == null) {
            return false;
        }

        nodes.computeIfPresent(id, (_, kindNodeMap) -> {
            kindNodeMap.computeIfPresent(kind, (_, _) -> null);
            return kindNodeMap.isEmpty() ? null : kindNodeMap;
        });

        removeNodeFromInfluences(node.getEffectEdges(), node.getId(), effectInfluences, causeInfluences, u::getCause, IEdge::setEffect);
        removeNodeFromInfluences(node.getCauseEdges(), node.getId(), causeInfluences, effectInfluences, u::getEffect, IEdge::setCause);

        removeNodeFromNonInfluenceEdges(node, node.getEffectEdges(), effectEdges, IEdge::setEffect);
        removeNodeFromNonInfluenceEdges(node, node.getCauseEdges(), causeEdges, IEdge::setCause);

        return true;
    }

    /**
     * Removes a node from the graph based on its identifier.
     * If there are multiple nodes associated with the identifier,
     * the node will not be removed and the method will return false.
     *
     * @param id the unique identifier of the node to be removed
     * @return true if the node was successfully removed, false otherwise
     */
    public boolean removeNode(QualifiedName id) {
        if (!nodes.containsKey(id) || nodes.get(id).size() != 1) {
            return false;
        }

        return removeNode(id, getNode(id).getKind());
    }

    /**
     * Removes the specified element from the document.
     * The element is removed only if it is the exact same object (using {@code ==} comparison),
     * not just an equal object according to {@code equals()}.
     * If the element is the only one in its associated node, the entire node is removed.
     *
     * @param element the element to remove
     * @return {@code true} if the element was successfully removed,
     * {@code false} if the node containing the element was not found
     */
    public boolean removeElement(Element element) {
        INode node = getNode(element);

        if (node == null) {
            return false;
        }

        if (node.getElements().size() == 1 && element == node.getAnyElement()) {
            return removeNode(element.getId(), element.getKind());
        }

        return node.remove(element);
    }

    /**
     * Removes all nodes associated with the given qualified name.
     *
     * @param id the qualified name identifying the nodes to remove
     * @return {@code true} if at least one node was removed, {@code false} if no nodes were found
     */
    public boolean removeNodes(QualifiedName id) {
        List<INode> nodes = getNodes(id);
        if (nodes.isEmpty()) {
            return false;
        }

        nodes.forEach(n -> removeNode(n.getId(), n.getKind()));
        return true;
    }

    private void removeNodeFromNonInfluenceEdges(INode node, List<IEdge> nodeEdges, Map<QualifiedName,
            Map<Kind, List<IEdge>>> documentEdges, BiConsumer<IEdge, INode> edgeSetter) {
        List<IEdge> oldEdges = nodeEdges.stream()
                .filter(e -> e.getKind() != Kind.PROV_INFLUENCE)
                .toList();

        documentEdges.computeIfAbsent(node.getId(), _ -> new HashMap<>())
                .computeIfAbsent(node.getKind(), _ -> new ArrayList<>())
                .addAll(oldEdges);

        oldEdges.forEach(edge -> edgeSetter.accept(edge, null));
        nodeEdges.clear();
    }


    private void removeNodeFromInfluences(List<IEdge> nodeEdges, QualifiedName nodeIdentifier,
                                          Map<QualifiedName, Map<QualifiedName, List<IEdge>>> documentInfluences,
                                          Map<QualifiedName, Map<QualifiedName, List<IEdge>>> documentOtherInfluences,
                                          Function<Relation, QualifiedName> extractOtherId,
                                          BiConsumer<IEdge, INode> edgeSetter) {

        Map<QualifiedName, List<IEdge>> influences = nodeEdges.stream()
                .filter(x -> x.getKind() == Kind.PROV_INFLUENCE)
                .collect(Collectors.groupingBy(e -> extractOtherId.apply(e.getAnyRelation())));

        for (QualifiedName otherId : influences.keySet()) {
            List<IEdge> oldInfluences = influences.get(otherId);

            if (oldInfluences.size() == documentInfluences.get(nodeIdentifier).get(otherId).size()) {
                oldInfluences.forEach(e -> edgeSetter.accept(e, null));
                return;
            }

            oldInfluences.forEach(e -> {
                documentInfluences.get(nodeIdentifier).get(otherId).remove(e);
                documentOtherInfluences.get(otherId).get(nodeIdentifier).remove(e);
                edges.remove(e);
            });
        }
    }

    /**
     * Updates the identifier of an existing node, replacing the old identifier with a new one for each element in the node.
     * If the new identifier is {@code null} or the same as the old one, no changes are made.
     * If a node with the new identifier already exists, elements from the old node are merged into it.
     * Updating node identifier does not update cause and effect identifiers in relations in edges
     *
     * @param oldIdentifier the current identifier of the element of a node
     * @param kind          the kind of the node
     * @param newIdentifier the new identifier to assign to the elements
     * @return {@code true} if the identifier was successfully updated, {@code false} otherwise
     */
    public boolean setNewNodeIdentifier(QualifiedName oldIdentifier, Kind kind, QualifiedName newIdentifier) {
        if (Objects.equals(oldIdentifier, newIdentifier) || newIdentifier == null) {
            return false;
        }

        INode node = getNode(oldIdentifier, kind);

        if (!removeNode(oldIdentifier, kind)) {
            return false;
        }

        node.getElements().forEach(e -> e.setId(newIdentifier));

        INode existingNode = getNode(newIdentifier, kind);
        if (existingNode != null) {
            node.getElements().forEach(existingNode::handleDuplicate);
            return true;
        }

        addCompletelyNewNode(newIdentifier, kind, node);

        return true;
    }

    /**
     * Updates the identifier of the given element to a new identifier.
     * The element is first removed using identity-based comparison ({@code ==}) before updating its ID.
     * If the new identifier is {@code null} or the same as the current one, no changes are made.
     *
     * @param element       the element to update
     * @param newIdentifier the new identifier to assign to the element
     * @return {@code true} if the identifier was successfully updated, {@code false} otherwise
     */
    public boolean setNewElementIdentifier(Element element, QualifiedName newIdentifier) {
        if (Objects.equals(element, newIdentifier) || newIdentifier == null) {
            return false;
        }

        if (!removeElement(element)) {
            return false;
        }

        element.setId(newIdentifier);

        INode existingNode = getNode(newIdentifier, element.getKind());
        if (existingNode != null) {
            existingNode.handleDuplicate(element);
            return true;
        }

        addCompletelyNewNode(newIdentifier, element.getKind(), cF.newNodeWithoutCloning(element));
        return true;
    }

    /**
     * Retrieves a single {@link INode} associated with the given {@link QualifiedName}, if exactly one exists.
     * If multiple nodes are found, an {@link IllegalStateException} is thrown.
     *
     * @param id the {@link QualifiedName} to look up
     * @return the single {@link INode} for the given ID, or {@code null} if none exists
     * @throws IllegalStateException if multiple nodes are associated with the given ID
     */
    public INode getNode(QualifiedName id) {
        if (nodes.containsKey(id)) {
            if (nodes.get(id).size() == 1) {
                return nodes.get(id).values().iterator().next();
            }
            throw new IllegalStateException(CpmExceptionConstants.MULTIPLE_NODES);
        }
        return null;
    }


    /**
     * Retrieves a single {@link INode} containing the given {@link Element}
     *
     * @param element the {@link Element} to look up
     * @return the {@link INode} containing the given element, or {@code null} if none exists
     */
    public INode getNode(Element element) {
        INode node = getNode(element.getId(), element.getKind());
        if (node != null && node.getElements().stream().anyMatch(e -> e == element)) {
            return node;
        }
        return null;
    }

    /**
     * Retrieves a single {@link INode} associated with the given {@link QualifiedName} and {@link Kind}.
     *
     * @param id   the {@link QualifiedName} to look up
     * @param kind the {@link Kind} to look up
     * @return the {@link INode} matching the given ID and kind, or {@code null} if none exists
     */
    public INode getNode(QualifiedName id, Kind kind) {
        if (nodes.containsKey(id)) {
            return nodes.get(id).get(kind);
        }
        return null;
    }

    /**
     * Retrieves all {@link INode}s associated with the given {@link QualifiedName}.
     *
     * <p>Use this method to fetch all nodes for the given ID. For a specific node by kind,
     * see {@link #getNode(QualifiedName, Kind)}. To ensure only one node exists, use {@link #getNode(QualifiedName)}.</p>
     *
     * @param id the {@link QualifiedName} to look up
     * @return a {@link List} of {@link INode}s associated with the given ID, or {@code null} if none exist
     */
    public List<INode> getNodes(QualifiedName id) {
        if (nodes.containsKey(id)) {
            return nodes.get(id).values().stream().toList();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieves the entire mapping of {@link QualifiedName} to their associated nodes.
     *
     * @return a {@link Map} where keys are {@link QualifiedName}s and values are maps of
     * {@link Kind} to {@link INode}.
     */
    public Map<QualifiedName, Map<Kind, INode>> getNodesMap() {
        return new HashMap<>(nodes);
    }


    /**
     * Retrieves the list of all edges
     *
     * @return a {@link List} of all {@link IEdge}
     */
    public List<INode> getNodes() {
        return nodes.values().stream()
                .flatMap(map -> map.values().stream())
                .toList();
    }

    /**
     * Retrieves a single edge associated with a given relation identifier.
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param id the identifier of the relation to search for
     * @return the matching {@link IEdge}, or {@code null} if no matching edge is found
     * @throws IllegalStateException if multiple edges are found with the same identifier
     */
    public IEdge getEdge(QualifiedName id) {
        List<IEdge> edges = getEdges(id);
        if (edges.isEmpty()) {
            return null;
        }

        if (edges.size() != 1) {
            throw new IllegalStateException(CpmExceptionConstants.MULTIPLE_EDGES);
        }

        return edges.getFirst();
    }


    /**
     * Retrieves a single edge associated with a given relation identifier and kind.
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param id   the identifier of the relation to search for
     * @param kind the {@link Kind} to look up
     * @return the matching {@link IEdge}, or {@code null} if no matching edge is found
     * @throws IllegalStateException if multiple edges are found with the same identifier
     */
    public IEdge getEdge(QualifiedName id, Kind kind) {
        IEdge edge = getEdge(id);
        if (edge != null && Objects.equals(edge.getKind(), kind)) {
            return edge;
        }

        return null;
    }


    /**
     * Retrieves all edges associated with a given relation identifier.
     *
     * @param id the identifier of the relation to search for
     * @return a list of {@link IEdge} objects matching the specified identifier, or an empty list if none are found
     */
    public List<IEdge> getEdges(QualifiedName id) {
        List<IEdge> result = new ArrayList<>();
        for (IEdge edge : edges) {
            if (edge.getAnyRelation() instanceof Identifiable relWithId
                    && Objects.equals(id, relWithId.getId())) {
                result.add(edge);
            }
        }
        return result;
    }


    /**
     * Retrieves all edges associated with a given relation identifier and kind
     *
     * @param id   the identifier of the relation to search for
     * @param kind the {@link Kind} to look up
     * @return the matching {@link IEdge}, or {@code null} if no matching edge is found
     */
    public List<IEdge> getEdges(QualifiedName id, Kind kind) {
        List<IEdge> result = new ArrayList<>();
        for (IEdge edge : edges) {
            if (Objects.equals(kind, edge.getKind())
                    && edge.getAnyRelation() instanceof Identifiable relWithId
                    && Objects.equals(id, relWithId.getId())) {
                result.add(edge);
            }
        }
        return result;
    }


    /**
     * Retrieves a single edge that matches the specified effect and cause identifiers.
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param effect the identifier of the effect
     * @param cause  the identifier of the cause
     * @return the matching {@link IEdge}, or {@code null} if no matching edge is found
     * @throws IllegalStateException if multiple edges are found between the specified effect and cause
     */
    public IEdge getEdge(QualifiedName effect, QualifiedName cause) {
        List<IEdge> edges = getEdges(effect, cause);
        if (edges.isEmpty()) {
            return null;
        }

        if (edges.size() != 1) {
            throw new IllegalStateException(CpmExceptionConstants.MULTIPLE_EDGES_BETWEEN_NODES);
        }

        return edges.getFirst();
    }

    /**
     * Retrieves a single edge that matches the specified kind, effect and cause identifiers.
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param effect the identifier of the effect
     * @param cause  the identifier of the cause
     * @param kind   the {@link Kind} to look up
     * @return the matching {@link IEdge}, or {@code null} if no matching edge is found
     * @throws IllegalStateException if multiple edges are found between the specified effect and cause
     */
    public IEdge getEdge(QualifiedName effect, QualifiedName cause, Kind kind) {
        IEdge edge = getEdge(effect, cause);

        if (edge != null && Objects.equals(kind, edge.getKind())) {
            return edge;
        }

        return null;
    }


    /**
     * Retrieves all edges that match the specified effect and cause identifiers.
     * This method searches for edges where:
     * <ul>
     *     <li>The relation is a {@link HadMember} and the collection matches the effect and the cause is contained within the entity.</li>
     *     <li>The cause and effect of the relation match the specified identifiers.</li>
     * </ul>
     *
     * @param effect the identifier of the effect
     * @param cause  the identifier of the cause
     * @return a list of {@link IEdge} objects matching the specified effect and cause, or an empty list if none are found
     */
    public List<IEdge> getEdges(QualifiedName effect, QualifiedName cause) {
        List<IEdge> result = new ArrayList<>();
        for (IEdge edge : edges) {
            if (edge.getAnyRelation() instanceof HadMember hM) {
                if (Objects.equals(hM.getCollection(), effect) &&
                        hM.getEntity() != null && hM.getEntity().contains(cause) &&
                        (edge.getCause() != null && Objects.equals(edge.getCause().getId(), cause) ||
                                edge.getCause() == null && causeEdges.getOrDefault(cause, Map.of())
                                        .getOrDefault(Kind.PROV_ENTITY, List.of())
                                        .stream().anyMatch(x -> x == edge))) {
                    result.add(edge);
                }
            } else if (Objects.equals(u.getCause(edge.getAnyRelation()), cause) &&
                    Objects.equals(u.getEffect(edge.getAnyRelation()), effect)) {
                result.add(edge);
            }
        }
        return result;
    }


    /**
     * Retrieves all edges that matches the specified effect and cause identifiers and {@link Kind}.
     *
     * @param effect the identifier of the effect
     * @param cause  the identifier of the cause
     * @param kind   the {@link Kind} to look up
     * @return a list of {@link IEdge} objects matching the specified effect, cause and kind, or an empty list if none are found
     */
    public List<IEdge> getEdges(QualifiedName effect, QualifiedName cause, Kind kind) {
        List<IEdge> edges = getEdges(effect, cause);

        return edges.stream().filter(e -> Objects.equals(kind, e.getKind())).toList();
    }


    /**
     * Retrieves list of all edges containing the given {@link Relation}
     *
     * @param relation the {@link Relation} to look up
     * @return the {@link IEdge} containing the given element, or {@code null} if none exists
     */
    public List<IEdge> getEdges(Relation relation) {
        return edges.stream().filter(e -> e.getRelations().stream().anyMatch(r -> r == relation)).toList();
    }

    /**
     * Retrieves the list of all edges
     *
     * @return a {@link List} of all {@link IEdge}
     */
    public List<IEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    /**
     * Updates the cause and effect of existing edges, replacing the old identifiers with a new ones for each relation in the edges.
     * If the new cause or effect is {@code null} or if they are the same as the old ones, no changes are made.
     * If an edge with the new cause and effect already exists, relations from the old edges are merged into it.
     * Updating causes and effects does not update identifiers in elements in nodes
     *
     * @param oldEffect the current effect identifier in the relation
     * @param oldCause  the current cause identifier in the relation
     * @param kind      the kind of the node
     * @param newEffect the new effect identifier to assign to the relations
     * @param newCause  the new cause identifier to assign to the relations
     * @return {@code true} if the cause and effect were successfully updated, {@code false} otherwise
     * @throws IllegalArgumentException if the method is called for the {@code PROV_MEMBERSHIP} kind
     */
    public boolean setNewCauseAndEffect(QualifiedName oldEffect, QualifiedName oldCause, Kind kind,
                                        QualifiedName newEffect, QualifiedName newCause) {
        if ((Objects.equals(oldEffect, newEffect) && Objects.equals(oldCause, newCause)) ||
                newEffect == null || newCause == null) {
            return false;
        }

        if (Kind.PROV_MEMBERSHIP.equals(kind)) {
            throw new IllegalArgumentException(CpmExceptionConstants.MEMBERSHIP_MODIFICATION_NOT_SUPPORTED);
        }

        List<IEdge> edges = getEdges(oldEffect, oldCause, kind);
        if (!removeEdges(oldEffect, oldCause, kind)) {
            return false;
        }

        int effectSetter = Kind.PROV_SPECIALIZATION.equals(kind) ||
                Kind.PROV_ALTERNATE.equals(kind) ? 0 : 1;
        int causeSetter = effectSetter + 1;

        IEdge edge = edges.getFirst();
        edge.getRelations().forEach(r -> {
            u.setter(r, effectSetter, newEffect);
            u.setter(r, causeSetter, newCause);
        });

        if (Kind.PROV_INFLUENCE.equals(kind)) {
            for (Relation r : edge.getRelations()) {
                doAction((WasInfluencedBy) r);
            }
            return true;
        }

        addEdge(edge, newEffect, newCause);
        return true;
    }

    /**
     * Updates the {@link HadMember} relations by replacing an old collection id and members with new ones.
     * If the new collection id and members are the same as the old ones, or if the new parameters are {@code null},
     * the operation is not performed.
     * Updating collection id and members does not update identifiers in elements in nodes
     *
     * @param oldCollectionId The current collection identifier in relations
     * @param oldMembers      The list of members currently in the collection.
     * @param newCollectionId The identifier of the new collection.
     * @param newMembers      The list of members to be assigned to the new collection.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    public boolean setCollectionMembers(QualifiedName oldCollectionId, List<QualifiedName> oldMembers,
                                        QualifiedName newCollectionId, List<QualifiedName> newMembers) {
        if ((Objects.equals(oldCollectionId, newCollectionId) && Objects.equals(oldMembers, newMembers)) ||
                newCollectionId == null || newMembers == null) {
            return false;
        }

        List<IEdge> hadMemberEdges = edges.stream()
                .filter(e -> e.getAnyRelation() instanceof HadMember hM &&
                        Objects.equals(hM.getCollection(), oldCollectionId) &&
                        Objects.equals(hM.getEntity(), oldMembers))
                .toList();

        if (!removeEdges(hadMemberEdges)) {
            return false;
        }

        List<Relation> relations = hadMemberEdges.getFirst().getRelations();
        for (Relation r : relations) {
            HadMember hM = (HadMember) r;
            hM.setCollection(newCollectionId);
            hM.getEntity().clear();
            hM.getEntity().addAll(newMembers);
            addEdge(hM);
        }
        return true;
    }

    /**
     * Updates the cause and effect of the given relation to new cause and effect.
     * The relation is first removed using identity-based comparison ({@code ==}) before updating its cause and effect.
     * If the cause or effect is {@code null} or if they are the same as the current ones, no changes are made.
     *
     * @param relation  the element to update
     * @param newEffect the new effect to assign to the relation
     * @param newCause  the new cause to assign to the relation
     * @return {@code true} if the cause and effect was successfully updated, {@code false} otherwise
     */
    public boolean setNewCauseAndEffect(Relation relation, QualifiedName newEffect, QualifiedName newCause) {
        if ((Objects.equals(u.getEffect(relation), newEffect) && Objects.equals(u.getCause(relation), newCause)) ||
                newEffect == null || newCause == null) {
            return false;
        }

        if (Kind.PROV_MEMBERSHIP.equals(relation.getKind())) {
            throw new IllegalArgumentException(CpmExceptionConstants.MEMBERSHIP_MODIFICATION_NOT_SUPPORTED);
        }

        if (!removeRelation(relation)) {
            return false;
        }

        int effectSetter = Kind.PROV_SPECIALIZATION.equals(relation.getKind()) ||
                Kind.PROV_ALTERNATE.equals(relation.getKind()) ? 0 : 1;
        int causeSetter = effectSetter + 1;

        u.setter(relation, effectSetter, newEffect);
        u.setter(relation, causeSetter, newCause);

        if (Kind.PROV_INFLUENCE.equals(relation.getKind())) {
            doAction((WasInfluencedBy) relation);
            return true;
        }

        addEdge(cF.newEdgeWithoutCloning(relation), newEffect, newCause);
        return true;
    }


    private boolean removeEdges(List<IEdge> edgesToRemove) {
        if (edgesToRemove == null || edgesToRemove.isEmpty()) {
            return false;
        }

        edgesToRemove.forEach(this::removeEdge);
        return true;
    }

    private boolean removeEdge(IEdge edge) {
        if (edge == null) {
            return false;
        }
        if (edge.getCause() != null) {
            edge.getCause().getCauseEdges().remove(edge);
        } else {
            causeEdges.computeIfPresent(u.getCause(edge.getAnyRelation()), (_, nodeKindMap) -> {
                try {
                    nodeKindMap.computeIfPresent(ProvUtilities2.getCauseKind(edge.getKind()), (_, edgeList) -> {
                        edgeList.remove(edge);
                        return edgeList.isEmpty() ? null : edgeList;
                    });
                } catch (NoSpecificKind ignored) {
                }
                return nodeKindMap.isEmpty() ? null : nodeKindMap;
            });
        }

        if (edge.getEffect() != null) {
            edge.getEffect().getEffectEdges().remove(edge);
        } else {
            effectEdges.computeIfPresent(u.getEffect(edge.getAnyRelation()), (_, nodeKindMap) -> {
                try {
                    nodeKindMap.computeIfPresent(ProvUtilities2.getEffectKind(edge.getKind()), (_, edgeList) -> {
                        edgeList.remove(edge);
                        return edgeList.isEmpty() ? null : edgeList;
                    });
                } catch (NoSpecificKind ignored) {
                }
                return nodeKindMap.isEmpty() ? null : nodeKindMap;
            });
        }

        if (Kind.PROV_INFLUENCE.equals(edge.getKind())) {
            causeInfluences.computeIfPresent(u.getCause(edge.getAnyRelation()), (_, causeEdgeMap) -> {
                causeEdgeMap.computeIfPresent(u.getEffect(edge.getAnyRelation()), (_, _) -> null);
                return causeEdgeMap.isEmpty() ? null : causeEdgeMap;
            });
            effectInfluences.computeIfPresent(u.getEffect(edge.getAnyRelation()), (_, effectEdgeMap) -> {
                effectEdgeMap.computeIfPresent(u.getCause(edge.getAnyRelation()), (_, _) -> null);
                return effectEdgeMap.isEmpty() ? null : effectEdgeMap;
            });
        }

        edges.remove(edge);

        return true;
    }

    /**
     * Removes a single edge identified by the specified identifier.
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param id the unique identifier of the edge to be removed
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     * @throws IllegalStateException if multiple edges are found between the specified effect and cause
     */
    public boolean removeEdge(QualifiedName id) {
        return removeEdge(getEdge(id));
    }

    /**
     * Removes a single edge identified by the specified identifier and kind
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param id   the unique identifier of the edge to be removed
     * @param kind the {@link Kind} to look up
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     * @throws IllegalStateException if multiple edges are found between the specified effect and cause
     */
    public boolean removeEdge(QualifiedName id, Kind kind) {
        return removeEdge(getEdge(id, kind));
    }


    /**
     * Removes a single edge identified by the specified cause and effect identifiers.
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param effect the identifier of the effect
     * @param cause  the identifier of the cause
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     * @throws IllegalStateException if multiple edges are found between the specified effect and cause
     */
    public boolean removeEdge(QualifiedName effect, QualifiedName cause) {
        return removeEdge(getEdge(effect, cause));
    }


    /**
     * Removes a single edge identified by the specified cause and effect identifiers and kind.
     * If multiple edges are found, an {@link IllegalStateException} is thrown.
     *
     * @param effect the identifier of the effect
     * @param cause  the identifier of the cause
     * @param kind   the {@link Kind} to look up
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     * @throws IllegalStateException if multiple edges are found between the specified effect and cause
     */
    public boolean removeEdge(QualifiedName effect, QualifiedName cause, Kind kind) {
        return removeEdge(getEdge(effect, cause, kind));
    }

    /**
     * Removes all edges identified by the specified identifier.
     *
     * @param id the unique identifier of the edge to be removed
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     */
    public boolean removeEdges(QualifiedName id) {
        return removeEdges(getEdges(id));
    }

    /**
     * Removes all edges identified by the specified identifier and kind.
     *
     * @param id   the unique identifier of the edge to be removed
     * @param kind the {@link Kind} to look up
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     */
    public boolean removeEdges(QualifiedName id, Kind kind) {
        return removeEdges(getEdges(id, kind));
    }

    /**
     * Removes an edge based on its effect and cause identifiers.
     *
     * @param effect the identifier of the effect node
     * @param cause  the identifier of the cause node
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     */
    public boolean removeEdges(QualifiedName effect, QualifiedName cause) {
        return removeEdges(getEdges(effect, cause));
    }

    /**
     * Removes an edge based on its kind, effect and cause identifiers.
     *
     * @param effect the identifier of the effect node
     * @param cause  the identifier of the cause node
     * @param kind   the {@link Kind} to look up
     * @return {@code true} if the edge was successfully removed, {@code false} otherwise
     */
    public boolean removeEdges(QualifiedName effect, QualifiedName cause, Kind kind) {
        return removeEdges(getEdges(effect, cause, kind));
    }


    /**
     * Removes the specified relation from the every edge it is present in
     * The relation is removed only if it is the exact same object (using {@code ==} comparison),
     * not just an equal object according to {@code equals()}.
     * If the relation is the only one in the edge, the edge is removed entirely
     *
     * @param relation the relation to remove
     * @return {@code true} if the relation was successfully removed,
     * {@code false} if no edges containing the relation was not found
     */
    public boolean removeRelation(Relation relation) {
        List<IEdge> edges = getEdges(relation);

        if (edges.isEmpty()) {
            return false;
        }

        boolean removed = false;
        for (IEdge edge : edges) {
            if (edge.getRelations().size() == 1 && edge.getAnyRelation() == relation) {
                return removeEdges(edges);
            }

            removed = removed || edge.remove(relation);
        }
        return removed;
    }


    private List<INode> getRelatedConnectors(QualifiedName id, Function<IEdge, INode> extractNode, Function<INode, List<IEdge>> extractEdges) {
        INode node = getNode(id, Kind.PROV_ENTITY);
        if (!CpmUtilities.isConnector(node) || !tiStrategy.belongsToTraversalInformation(node)) {
            return null;
        }

        List<INode> result = new ArrayList<>();
        List<INode> toProcess = new ArrayList<>(extractEdges.apply(node).stream()
                .map(extractNode)
                .toList());

        while (!toProcess.isEmpty()) {
            INode current = toProcess.removeFirst();
            result.add(current);
            toProcess.addAll(extractEdges.apply(current).stream()
                    .filter(e -> Kind.PROV_DERIVATION.equals(e.getKind()))
                    .map(extractNode)
                    .filter(n -> node != n && !result.contains(n) &&
                            CpmUtilities.isConnector(n) && tiStrategy.belongsToTraversalInformation(node))
                    .toList());
        }
        return result;
    }

    /**
     * Retrieves a list of precursor nodes for the specified connector, based on the provided {@link QualifiedName}.
     * A precursor connector is defined as a connector node related to the given node via a derivation relation.
     *
     * @param id the identifier of the node for which to find the precursor nodes
     * @return a list of precursor nodes, or {@code null} the node is not a connector or is {@code null}
     */
    public List<INode> getPrecursors(QualifiedName id) {
        return getRelatedConnectors(id, IEdge::getEffect, INode::getCauseEdges);
    }

    /**
     * Retrieves a list of successor connectors for the specified connector, based on the provided {@link QualifiedName}.
     * A successor connector is defined as a connector node related to the given node via a derivation relation.
     *
     * @param id the identifier of the node for which to find the successor nodes
     * @return a list of successor nodes, or {@code null} the node is not a connector or is {@code null}
     */
    public List<INode> getSuccessors(QualifiedName id) {
        return getRelatedConnectors(id, IEdge::getCause, INode::getEffectEdges);
    }

    public QualifiedName getBundleId() {
        return this.bundleId;
    }

    public void setBundleId(QualifiedName id) {
        this.bundleId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CpmDocument document = (CpmDocument) o;
        return Objects.equals(effectEdges, document.effectEdges) &&
                Objects.equals(causeEdges, document.causeEdges) &&
                Objects.equals(nodes, document.nodes) &&
                Objects.equals(edges.size(), document.edges.size()) &&
                Objects.equals(new HashSet<>(edges), new HashSet<>(document.edges)) &&
                Objects.equals(bundleId, document.bundleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectEdges, causeEdges, nodes, edges, bundleId);
    }
}
