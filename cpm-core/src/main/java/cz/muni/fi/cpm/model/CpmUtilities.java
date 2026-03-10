package cz.muni.fi.cpm.model;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.constants.CpmType;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Utilities for manipulating CPM Descriptions.
 */
public class CpmUtilities {

    /**
     * Checks if the given {@link INode} has any supported CPM type.
     * This method verifies if the types of the elements in the node contain only types that match
     * the CPM namespace, prefix, are listed in {@link CpmType#STRING_VALUES}, the combination of {@link CpmType}
     * and {@link Kind} is valid
     *
     * @param node the {@link INode} to check
     * @return {@code true} if the node has any element with a supported CPM type, {@code false} otherwise
     */
    public static boolean hasValidCpmType(INode node) {
        if (node == null) return false;

        List<Type> types = node.getElements().stream().flatMap(e -> e.getType().stream()).distinct().toList();

        // All types must be QualifiedName
        if (types.stream().anyMatch(t -> !(t.getValue() instanceof QualifiedName))) return false;

        // All types must be QualifiedName
        Set<String> typeStrings = types.stream()
                .map(t -> ((QualifiedName) t.getValue()))
                .filter(CpmUtilities::belongsToCpmNs)
                .map(QualifiedName::getLocalPart)
                .collect(Collectors.toSet());

        // Two subtypes allowed only for sender and receiver agent
        if (typeStrings.size() != 1 && !CpmType.AGENTS.equals(typeStrings)) return false;

        return typeStrings.stream().allMatch(lp ->
                CpmType.STRING_VALUES.contains(lp) &&
                        CpmType.CPM_TYPE_TO_KIND.get(CpmType.fromString(lp)) == node.getKind());
    }

    /**
     * Checks if the given {@link INode} has a specific CPM type.
     * This method verifies if any of the elements in the node contain a type that matches
     * the CPM namespace, prefix, and the specified {@link CpmType}.
     *
     * @param node the {@link INode} to check
     * @param type the {@link CpmType} to compare against
     * @return {@code true} if the node has any element with the specified CPM type, {@code false} otherwise
     */
    public static boolean hasCpmType(INode node, CpmType type) {
        if (node == null) return false;

        return node.getElements().stream().anyMatch(element -> hasCpmType(element, type));
    }

    /**
     * Checks if the given {@link Statement} has a specific CPM type.
     * This method verifies if the statement contains a type that matches
     * the CPM namespace, prefix, the specified {@link CpmType} and the combination of {@link CpmType} and {@link Kind}.
     *
     * @param statement the {@link Statement} to check
     * @param type      the {@link CpmType} to compare against
     * @return {@code true} if the node has any element with the specified CPM type, {@code false} otherwise
     */
    public static boolean hasCpmType(Statement statement, CpmType type) {
        if (statement == null || type == null) return false;

        if (CpmType.CPM_TYPE_TO_KIND.get(type) != statement.getKind()) return false;

        return statement instanceof HasType element &&
                element.getType().stream().anyMatch(x ->
                        x.getValue() instanceof QualifiedName qN &&
                                belongsToCpmNs(qN) && Objects.equals(type.toString(), qN.getLocalPart()));
    }

    /**
     * Checks if the given {@link HasOther} contains a specific CPM attribute.
     * This method verifies if the statement includes an "other" attribute matching
     * the CPM namespace, prefix, and the specified {@link CpmAttribute}.
     *
     * @param statement the {@link HasOther} to check
     * @param attr      the {@link CpmAttribute} to compare against
     * @return {@code true} if the statement contains the specified CPM attribute, {@code false} otherwise
     */
    public static boolean containsCpmAttribute(HasOther statement, CpmAttribute attr) {
        if (statement == null || attr == null) return false;

        return statement.getOther().stream().anyMatch(x ->
                x.getElementName() instanceof QualifiedName qN && belongsToCpmNs(qN) &&
                        Objects.equals(attr.toString(), qN.getLocalPart()));
    }

    /**
     * Retrieves the value of a specific CPM attribute from the given {@link HasOther} statement.
     * This method searches the "other" attributes for one that matches the CPM namespace and
     * the specified {@link CpmAttribute}. If found, the value of the attribute is returned.
     *
     * @param statement the {@link HasOther} to search
     * @param attr      the {@link CpmAttribute} to retrieve
     * @return the value of the specified CPM attribute, or {@code null} if not present
     */
    public static Object getCpmAttributeValue(HasOther statement, CpmAttribute attr) {
        if (statement == null || attr == null) return null;
        return statement.getOther().stream().filter(x ->
                        x.getElementName() instanceof QualifiedName qN &&
                                belongsToCpmNs(qN) && Objects.equals(attr.toString(), qN.getLocalPart()))
                .findFirst()
                .map(Attribute::getValue)
                .orElse(null);
    }

    /**
     * Checks if the given {@link INode} is considered a connector node.
     * A connector node is defined as having any element with a type matching either
     * {@link CpmType#FORWARD_CONNECTOR}, {@link CpmType#SPEC_FORWARD_CONNECTOR} or {@link CpmType#BACKWARD_CONNECTOR}.
     *
     * @param node the {@link INode} to check
     * @return {@code true} if the node contains a connector element, {@code false} otherwise
     */
    public static boolean isConnector(INode node) {
        if (node == null) return false;
        return node.getElements().stream().anyMatch(e -> e instanceof Entity en && isConnector(en));
    }

    /**
     * Checks if the given {@link Entity} is considered a connector.
     * A connector is defined as having a type matching either
     * {@link CpmType#FORWARD_CONNECTOR}, {@link CpmType#SPEC_FORWARD_CONNECTOR} or {@link CpmType#BACKWARD_CONNECTOR}.
     *
     * @param entity the {@link Entity} to check
     * @return {@code true} if the entity is a connector, {@code false} otherwise
     */
    public static boolean isConnector(Entity entity) {
        return entity != null && entity.getType().stream().anyMatch(x ->
                x.getValue() instanceof QualifiedName qN &&
                        belongsToCpmNs(qN) &&
                                (CpmType.FORWARD_CONNECTOR.toString().equals(qN.getLocalPart()) ||
                                CpmType.SPEC_FORWARD_CONNECTOR.toString().equals(qN.getLocalPart()) ||
                                CpmType.BACKWARD_CONNECTOR.toString().equals(qN.getLocalPart())));
    }

    /**
     * Checks if the given {@link QualifiedName} belongs to the CPM namespace.
     *
     * @param qN the qualified name to check
     * @return true if the name is in the CPM namespace with the correct prefix, false otherwise
     */
    public static boolean belongsToCpmNs(QualifiedName qN) {
        return qN != null &&
                Objects.equals(qN.getNamespaceURI(), CpmNamespaceConstants.CPM_NS) &&
                Objects.equals(qN.getPrefix(), CpmNamespaceConstants.CPM_PREFIX);
    }
}
