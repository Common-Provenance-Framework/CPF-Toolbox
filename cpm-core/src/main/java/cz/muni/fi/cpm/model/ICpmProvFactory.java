package cz.muni.fi.cpm.model;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import org.openprovenance.prov.model.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collection;


/**
 * Factory interface for creating CPM-specific PROV model components.
 */
public interface ICpmProvFactory {

    ProvFactory getProvFactory();


    /**
     * Creates a new CPM type with the given CpmType.
     *
     * @param type the CpmType to create
     * @return a new Type
     */
    Type newCpmType(CpmType type);

    /**
     * Creates a new CPM qualified name with the given local name, CPM uri and CPM prefix
     *
     * @param local the local part of the qualified name
     * @return a new QualifiedName
     */
    QualifiedName newCpmQualifiedName(String local);


    /**
     * Creates a new CPM attribute with the specified {@link CpmAttribute} and value based of {@link Other}.
     *
     * @param attr  the {@link CpmAttribute} enum constant representing the attribute name
     * @param value the value of the attribute
     * @return a new Attribute
     */
    Other newCpmAttribute(CpmAttribute attr, QualifiedName value);

    /**
     * Creates a new CPM attribute with the specified {@link CpmAttribute} and value based of {@link Other}.
     *
     * @param attr  the {@link CpmAttribute} enum constant representing the attribute name
     * @param value the value of the attribute
     * @return a new Attribute
     */
    Other newCpmAttribute(CpmAttribute attr, String value);

    /**
     * Creates a new CPM attribute with the specified {@link CpmAttribute} and value based of {@link Other}.
     *
     * @param attr  the {@link CpmAttribute} enum constant representing the attribute name
     * @param value the value of the attribute
     * @return a new Attribute
     */
    Other newCpmAttribute(CpmAttribute attr, Object value);

    /**
     * Creates a new CPM attribute with the specified {@link CpmAttribute}, value, and type based of {@link Other}.
     *
     * @param attr the  CPM attribute
     * @param value the value of the attribute
     * @param type  the type of the attribute
     * @return a new Attribute
     */
    Other newCpmAttribute(CpmAttribute attr, Object value, QualifiedName type);

    /**
     * Creates a new external ID attribute with the specified value.
     *
     * @param value the value for the external ID attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeExternalId(String value);

    /**
     * Creates a new external ID Type attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeExternalIdType(String value);

    /**
     * Creates a new Comment attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeComment(String value);

    /**
     * Creates a new contactIdPid attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeContactIdPid(String value);

    /**
     * Creates a new hashAlg attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeHashAlg(String value);

    /**
     * Creates a new provenanceServiceUri attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeProvenanceServiceUri(String value);

    /**
     * Creates a new referencedBundleHashValue attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeReferencedBundleHashValue(Object value);

    /**
     * Creates a new referencedBundleId attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeReferencedBundleId(QualifiedName value);

    /**
     * Creates a new referencedMetaBundleId attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeReferencedMetaBundleId(QualifiedName value);

    /**
     * Creates a new referencedBundleSpecV attribute with the specified value.
     *
     * @param value the value for the attribute
     * @return a new {@link Other} instance configured with the provided value
     */
    Other newCpmAttributeReferencedBundleSpecV(String value);


    /**
     * Creates a new DCT attribute with the specified qualified name value.
     *
     * @param value the qualified name value for the DCT attribute
     * @return a new DCT attribute instance
     */
    Other newDctAttribute(QualifiedName value);

    /**
     * Creates a new CPM entity with the given ID, type, and attributes.
     *
     * @param id         the ID of the entity
     * @param type       the type of the entity
     * @param attributes the attributes of the entity
     * @return a new Entity
     */
    Entity newCpmEntity(QualifiedName id, CpmType type, Collection<Attribute> attributes);

    /**
     * Creates a new CPM identifier entity with the specified qualified name and attributes.
     *
     * @param id the qualified name that uniquely identifies the entity
     * @param attributes a collection of attributes to be associated with the entity
     * @return a new {@link Entity} instance representing a CPM identifier entity
     */
    Entity newCpmIdentifierEntity(QualifiedName id, Collection<Attribute> attributes);

    /**
     * Creates a new CPM identifier entity with the specified qualified name and attributes.
     *
     * @param id the qualified name that uniquely identifies the entity
     * @return a new {@link Entity} instance representing a CPM identifier entity
     */
    Entity newCpmIdentifierEntity(QualifiedName id);

    /**
     * Creates a new CPM backward connector entity.
     *
     * @param id the qualified name identifier for the backward connector
     * @param attributes a collection of attributes to be assigned to the backward connector
     * @return a new {@link Entity} representing the CPM backward connector
     */
    Entity newCpmBackwardConnector(QualifiedName id);

    /**
     * Creates a new CPM forward connector entity.
     *
     * @param id the qualified name identifier for the forward connector
     * @return a new {@link Entity} representing the CPM forward connector
     */
    Entity newCpmForwardConnector(QualifiedName id);

    /**
     * Creates a new CPM specialization forward connector entity.
     *
     * @param id the qualified name identifier for the forward connector
     * @return a new {@link Entity} representing the CPM specialization forward connector
     */
    Entity newCpmSpecForwardConnector(QualifiedName id);

    /**
     * Creates a new CPM activity with the given ID, start time, end time, type, and attributes.
     *
     * @param id         the ID of the activity
     * @param startTime  the start time of the activity
     * @param endTime    the end time of the activity
     * @param type       the type of the activity
     * @param attributes the attributes of the activity
     * @return a new Activity
     */
    Activity newCpmMainActivity(QualifiedName id, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, Collection<Attribute> attributes);

    /**
     * Creates a new CPM activity with the given ID, start time, end time, type, and attributes.
     *
     * @param id         the ID of the activity
     * @param startTime  the start time of the activity
     * @param endTime    the end time of the activity
     * @param type       the type of the activity
     * @return a new Activity
     */
    Activity newCpmMainActivity(QualifiedName id, XMLGregorianCalendar startTime, XMLGregorianCalendar endTime);

    /**
     * Creates a new CPM agent with the given ID, type, and attributes.
     *
     * @param id         the ID of the agent
     * @param type       the type of the agent
     * @param attributes the attributes of the agent
     * @return a new Agent
     */
    Agent newCpmAgent(QualifiedName id, CpmType type, Collection<Attribute> attributes);

    /**
     * Creates a new CPM sender agent with the given ID.
     *
     * @param id         the ID of the agent
     * @return a new Agent
     */
    Agent newCpmSenderAgent(QualifiedName id);

    /**
     * Creates a new CPM receiver agent with the given ID.
     *
     * @param id         the ID of the agent
     * @return a new Agent
     */
    Agent newCpmReceiverAgent(QualifiedName id);

    /**
     * Creates a new CPM agent which is sender and receiver with the given ID.
     *
     * @param id         the ID of the agent
     * @return a new Agent
     */
    Agent newCpmMergedAgent(QualifiedName id);

    /**
     * Creates a new PROV namespace and adds known CPM namespaces.
     *
     * @return a new Namespace
     */
    Namespace newCpmNamespace();
}
