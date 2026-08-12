# Building traversal information in Java

The [README](../README.md) covers the JSON path: write a template file, feed it to `TraversalInformationDeserializer`, get a `Document`. This page covers the alternative — constructing the traversal information programmatically and mapping it to a `Document` directly in memory, with no JSON involved.

Use this when the data comes from a running system rather than from hand-written files.

## In-memory instantiation

The following code produces the same result as the basic JSON example in the [README](../README.md#basic-example):

```java
DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
ProvFactory pF = new org.openprovenance.prov.vanilla.ProvFactory();
TraversalInformation ti = new TraversalInformation();

ti.setPrefixes(Map.of("ex", "www.example.com/"));
ti.setBundleName(ti.getNamespace().qualifiedName("ex", "bundle1", pF));

MainActivity mA = new MainActivity(ti.getNamespace().qualifiedName("ex", "activity1", pF));
mA.setStartTime(datatypeFactory.newXMLGregorianCalendar("2011-11-16T16:05:00"));
mA.setEndTime(datatypeFactory.newXMLGregorianCalendar("2011-11-16T18:05:00"));
ti.setMainActivity(mA);

QualifiedName bcID = ti.getNamespace().qualifiedName("ex", "backConnector1", pF);
BackwardConnector bC = new BackwardConnector(bcID);
ti.getBackwardConnectors().add(bC);

MainActivityUsed used = new MainActivityUsed(bcID);
mA.setUsed(List.of(used));

QualifiedName fcID = ti.getNamespace().qualifiedName("ex", "forwardConnector1", pF);
mA.setGenerated(List.of(fcID));

ForwardConnector fC = new ForwardConnector(fcID);
fC.setDerivedFrom(List.of(bC.getId()));
ti.getForwardConnectors().add(fC);

ITemplateProvMapper mapper = new TemplateProvMapper(new CpmProvFactory(pF));
Document doc = mapper.toProvDocument(ti);
```

The resulting `Document` is identical in kind to what the JSON path produces, so Step 2 and Step 3 from the [README workflow](../README.md#workflow) apply unchanged.

## Agent merging

By default, sender and receiver agents with the same identifier are treated as distinct. To enable automatic merging into a single agent with both types, configure the mapper as follows:

Using the constructor:

```java
ITemplateProvMapper mapper = new TemplateProvMapper(new CpmProvFactory(pF), true);
```

Or using the setter:

```java
mapper.setMergeAgents(true);
```

The mapper can be passed to the `TraversalInformationDeserializer` to merge agents during JSON deserialisation as well:

```java
ITraversalInformationDeserializer deserializer =
        new TraversalInformationDeserializer(new ObjectMapper(), mapper);
```
