package org.commonprovenanceframework.cpm.model;

import org.commonprovenanceframework.cpm.exception.NoSpecificKind;
import org.commonprovenanceframework.cpm.exception.ValueConflict;
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Extension of utilities for manipulating PROV Statements.
 */
public class ProvUtilities2 {
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final String VALUE = "value";

    /**
     * Retrieves the effect kind for the given relation.
     * This method returns the {@link Kind} based on the type of the relation.
     *
     * @param relKind the relation kind for which to determine the effect kind
     * @return the corresponding {@link Kind} for the effect
     * @throws NoSpecificKind if the relation is not recognized or doesn't have a specific effect kind
     */
    public static Kind getEffectKind(Kind relKind) throws NoSpecificKind {
        return switch (relKind) {
            case PROV_USAGE -> Kind.PROV_ACTIVITY;
            case PROV_START -> Kind.PROV_ACTIVITY;
            case PROV_END -> Kind.PROV_ACTIVITY;
            case PROV_GENERATION -> Kind.PROV_ENTITY;
            case PROV_DERIVATION -> Kind.PROV_ENTITY;
            case PROV_ASSOCIATION -> Kind.PROV_ACTIVITY;
            case PROV_INVALIDATION -> Kind.PROV_ENTITY;
            case PROV_ATTRIBUTION -> Kind.PROV_ENTITY;
            case PROV_ALTERNATE -> Kind.PROV_ENTITY;
            case PROV_SPECIALIZATION -> Kind.PROV_ENTITY;
            case PROV_MEMBERSHIP -> Kind.PROV_ENTITY;
            case PROV_COMMUNICATION -> Kind.PROV_ACTIVITY;
            case PROV_MENTION -> Kind.PROV_ENTITY;
            case PROV_INFLUENCE -> throw new NoSpecificKind();
            case PROV_DELEGATION -> Kind.PROV_AGENT;
            case PROV_DICTIONARY_INSERTION -> Kind.PROV_ENTITY;
            default -> {
                System.out.println("Unknown relation " + relKind);
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Retrieves the cause kind for the given relation.
     * This method returns the {@link Kind} based on the type of the relation.
     *
     * @param relKind the relation kind for which to determine the cause kind
     * @return the corresponding {@link Kind} for the cause
     * @throws NoSpecificKind if the relation is not recognized or doesn't have a specific cause kind
     */
    public static Kind getCauseKind(Kind relKind) throws NoSpecificKind {

        return switch (relKind) {
            case PROV_USAGE -> Kind.PROV_ENTITY;
            case PROV_GENERATION -> Kind.PROV_ACTIVITY;
            case PROV_INVALIDATION -> Kind.PROV_ACTIVITY;
            case PROV_START -> Kind.PROV_ENTITY;
            case PROV_END -> Kind.PROV_ENTITY;
            case PROV_DERIVATION -> Kind.PROV_ENTITY;
            case PROV_INFLUENCE -> throw new NoSpecificKind();
            case PROV_ASSOCIATION -> Kind.PROV_AGENT;
            case PROV_ATTRIBUTION -> Kind.PROV_AGENT;
            case PROV_ALTERNATE -> Kind.PROV_ENTITY;
            case PROV_SPECIALIZATION -> Kind.PROV_ENTITY;
            case PROV_MEMBERSHIP -> Kind.PROV_ENTITY;
            case PROV_MENTION -> Kind.PROV_ENTITY;
            case PROV_COMMUNICATION -> Kind.PROV_ACTIVITY;
            case PROV_DELEGATION -> Kind.PROV_AGENT;
            case PROV_DICTIONARY_INSERTION -> Kind.PROV_ENTITY;
            default -> {
                System.out.println("Unknown relation " + relKind);
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Merges the attributes from the new element into the existing one, ensuring no conflicts.
     * This method merges labels, locations, types, and other attributes, and checks for conflicts in time values for activities and value conflicts for entities.
     *
     * @param existing   the existing element to merge into
     * @param newElement the new element whose attributes will be merged
     * @throws ValueConflict if there is a conflict in values between the existing and new element (e.g., conflicting start or end times for activities)
     */
    public static <T extends Element> void mergeAttributes(T existing, T newElement) throws ValueConflict {
        Set<LangString> set = new HashSet<>(newElement.getLabel());
        existing.getLabel().forEach(set::remove);
        existing.getLabel().addAll(set);

        Set<Location> set2 = new HashSet<>(newElement.getLocation());
        existing.getLocation().forEach(set2::remove);
        existing.getLocation().addAll(set2);

        Set<Type> set3 = new HashSet<>(newElement.getType());
        existing.getType().forEach(set3::remove);
        existing.getType().addAll(set3);

        Set<Other> set4 = new HashSet<>(newElement.getOther());
        existing.getOther().forEach(set4::remove);
        existing.getOther().addAll(set4);

        if (existing instanceof Activity existingActivity) {
            Activity newActivity = (Activity) newElement;
            if (existingActivity.getStartTime() != null && newActivity.getStartTime() != null
                    && !existingActivity.getStartTime().equals(newActivity.getStartTime())) {
                throw new ValueConflict(existing.getId().toString(), START_TIME, existingActivity.getStartTime().toString(), newActivity.getStartTime().toString());
            }

            if (existingActivity.getEndTime() != null && newActivity.getEndTime() != null
                    && !existingActivity.getEndTime().equals(newActivity.getEndTime())) {
                throw new ValueConflict(existing.getId().toString(), END_TIME, existingActivity.getEndTime().toString(), newActivity.getEndTime().toString());
            }
        }

        if (existing instanceof Entity existingEntity) {
            Entity newEntity = (Entity) newElement;
            if (existingEntity.getValue() != null && newEntity.getValue() != null
                    && !existingEntity.getValue().equals(newEntity.getValue())) {
                throw new ValueConflict(existing.getId().toString(), VALUE, existingEntity.getValue().toString(), newEntity.getValue().toString());
            }
        }
    }

    public static void mergeAttributes(QualifiedRelation existing, QualifiedRelation newElement) {
        Set<LangString> set = new HashSet<>(newElement.getLabel());
        existing.getLabel().forEach(set::remove);
        existing.getLabel().addAll(set);

        if (existing instanceof HasLocation existing2) {
            Set<Location> set2 = new HashSet<>(((HasLocation) newElement).getLocation());
            existing2.getLocation().forEach(set2::remove);
            existing2.getLocation().addAll(set2);
        }

        Set<Type> set3 = new HashSet<Type>(newElement.getType());
        existing.getType().forEach(set3::remove);
        existing.getType().addAll(set3);

        Set<Other> set4 = new HashSet<Other>(newElement.getOther());
        existing.getOther().forEach(set4::remove);
        existing.getOther().addAll(set4);
    }

    public static boolean sameEdge(ProvUtilities u, Relation r1, Relation r2) {
        if (r1 instanceof WasInformedBy && r2 instanceof WasInformedBy) {
            return sameEdge(u, r1, r2, 2);
        } else if (r1 instanceof Used && r2 instanceof Used) {
            return sameEdge(u, r1, r2, 3);
        } else if (r1 instanceof WasGeneratedBy && r2 instanceof WasGeneratedBy) {
            return sameEdge(u, r1, r2, 3);
        } else if (r1 instanceof WasDerivedFrom && r2 instanceof WasDerivedFrom) {
            return sameEdge(u, r1, r2, 5);
        } else if (r1 instanceof WasAssociatedWith && r2 instanceof WasAssociatedWith) {
            return sameEdge(u, r1, r2, 3);
        } else if (r1 instanceof WasAttributedTo && r2 instanceof WasAttributedTo) {
            return sameEdge(u, r1, r2, 2);
        } else if (r1 instanceof ActedOnBehalfOf && r2 instanceof ActedOnBehalfOf) {
            return sameEdge(u, r1, r2, 3);
        } else if (r1 instanceof WasInvalidatedBy && r2 instanceof WasInvalidatedBy) {
            return sameEdge(u, r1, r2, 3);
        } else if (r1 instanceof SpecializationOf && r2 instanceof SpecializationOf) {
            return sameEdge(u, r1, r2, 2);
        } else if (r1 instanceof AlternateOf && r2 instanceof AlternateOf) {
            return sameEdge(u, r1, r2, 2);
        } else if (r1 instanceof WasInfluencedBy && r2 instanceof WasInfluencedBy) {
            return sameEdge(u, r1, r2, 2);
        } else if (r1 instanceof WasStartedBy && r2 instanceof WasStartedBy) {
            return sameEdge(u, r1, r2, 4);
        } else if (r1 instanceof WasEndedBy && r2 instanceof WasEndedBy) {
            return sameEdge(u, r1, r2, 4);
        } else if (r1 instanceof HadMember && r2 instanceof HadMember) {
            return sameEdge(u, r1, r2, 1);
        }
        return false;
    }

    private static boolean sameEdge(ProvUtilities u, Relation r1, Relation r2, int count) {
        for (int i = 0; i <= count; i++) {
            Object qn1 = u.getter(r1, i);
            Object qn2 = u.getter(r2, i);
            if (!Objects.equals(qn1, qn2)) {
                return false;
            }
        }
        return true;
    }

}
