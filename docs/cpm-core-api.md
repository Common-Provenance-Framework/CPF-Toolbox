# `cpm-core` — reading and manipulating CPM bundles

The [README](../README.md) covers `cpm-template`, which *writes* traversal information. This page covers `cpm-core`, which *reads and manipulates* it.

The `cpm-core` module centers around the `CpmDocument` class, which enables graph-based traversal and querying of provenance documents. Internally, the document is represented as a traversable graph composed of nodes and edges.

## Initialization of `CpmDocument`

The `CpmDocument` class can be initialized in multiple ways. The two most commonly used approaches are:

### Initialization from a ProvToolBox `Document`

This method is suitable when working with a ProvToolBox `Document` containing exactly one bundle:

```java
ProvFactory pF = new ProvFactory();
ICpmFactory cF = new CpmMergedFactory(pF);
ICpmProvFactory cPF = new CpmProvFactory(pF);

Document document = pF.newDocument();
document.setNamespace(cPF.newCpmNamespace());

QualifiedName id = pF.newQualifiedName("uri", "bundle", "ex");
Bundle bundle = pF.newNamedBundle(id, new ArrayList<>());
document.getStatementOrBundle().add(bundle);

QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
Entity entity = cPF.getProvFactory().newEntity(id1);

QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
Agent agent = cPF.getProvFactory().newAgent(id2);

Relation relation = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);

bundle.getStatement().add(entity, agent, relation);

CpmDocument doc = new CpmDocument(document, pF, cPF, cF);
```

### Initialization from a List of Statements

This alternative approach allows for initialization using a list of statements and an explicit bundle identifier:

```java
ProvFactory pF = new ProvFactory();
ICpmFactory cF = new CpmMergedFactory(pF);
ICpmProvFactory cPF = new CpmProvFactory(pF);

QualifiedName id1 = cPF.newCpmQualifiedName("qN1");
Entity entity = cPF.getProvFactory().newEntity(id1);

QualifiedName id2 = cPF.newCpmQualifiedName("qN2");
Agent agent = cPF.getProvFactory().newAgent(id2);

Relation relation = cPF.getProvFactory().newWasAttributedTo(cPF.newCpmQualifiedName("attr"), id1, id2);

QualifiedName bundleId = pF.newQualifiedName("uri", "bundle", "ex");

CpmDocument doc = new CpmDocument(List.of(entity, agent, relation), bundleId, pF, cPF, cF);
```

## `ICpmFactory` Interface

The `ICpmFactory` interface defines how statements with identical identifiers are processed within the graph structure. The module provides the following core implementations:

### Merged Implementation

* **`CpmMergedFactory`**
  Merges statements with the same identifier using custom algorithms provided by `ProvUtilities2`.

### Divided Implementations

These implementations retain all statements sharing the same identifier, differing in how they handle statement ordering:

* **`CpmUnorderedFactory`**
  Does not preserve the original order of statements during conversions between `CpmDocument` and ProvToolBox `Document`.

* **`CpmOrderedFactory`**
  Preserves the original statement order from the source ProvToolBox `Document`.

## Traversing the Graph

The `CpmDocument` graph structure allows standard traversal algorithms. The following example demonstrates breadth-first search to extract a connected subgraph:

```java
public List<INode> getConnectedSubgraph(CpmDocument cpmDoc, QualifiedName startNodeIdentifier) {
    List<INode> result = new ArrayList<>();
    Queue<INode> toProcess = new LinkedList<>();
    
    INode startNode = cpmDoc.getNode(startNodeIdentifier);
    
    toProcess.add(startNode);
    result.add(startNode);

    while (!toProcess.isEmpty()) {
        INode current = toProcess.poll();

        for (IEdge edge : current.getCauseEdges()) {
            toProcess.add(edge.getEffect());
            result.add(edge.getEffect());
        }
    }

    return result;
}
```

## Functionalities Provided by `CpmDocument`

The `CpmDocument` class supports a wide range of operations, including:

1. Retrieving entities, agents, or activities by identifier.
2. Retrieving relations by identifier, or based on source and target identifiers.
3. Identifying the main activity.
4. Accessing forward and backward connectors.
5. Navigating preceding or successive connectors by identifier.
6. Extracting the traversal information subgraph of the bundle.
7. Extracting domain-specific provenance as a subgraph.
8. Identifying relations between traversal and domain-specific provenance parts.
9. Reconstructing a full document from traversal and domain-specific subgraphs, and cross-part relations.

## Document Modification

The `CpmDocument` supports mutation through a set of defined operations:

* **Addition**
  Use `doAction` methods to add statements.

* **Removal**
  Use `remove`-prefixed methods to delete statements or nodes.

* **Modification**
  Use methods prefixed with `setNew` or `setCollectionMembers` to update identifiers and collection memberships.

## Customizing Traversal Strategy

The classification of nodes into traversal or domain-specific components is governed by the `ITIStrategy` interface. The default implementation relies on attributes of the underlying PROV elements.

To apply a custom strategy, implement `ITIStrategy` and register it with the document:

```java
cpmDoc.setTIStrategy(customTiStrategy);
```
