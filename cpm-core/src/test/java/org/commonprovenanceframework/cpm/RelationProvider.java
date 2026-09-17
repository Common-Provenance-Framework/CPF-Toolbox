package org.commonprovenanceframework.cpm;

import org.openprovenance.prov.model.QualifiedName;

import java.util.Collections;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class RelationProvider {

    static Stream<Object[]> provideRelations() {
        org.openprovenance.prov.vanilla.ProvFactory pF = new org.openprovenance.prov.vanilla.ProvFactory();
        QualifiedName entityId1 = pF.newQualifiedName("uri", "entity1", "ex");
        QualifiedName entityId2 = pF.newQualifiedName("uri", "entity2", "ex");

        QualifiedName activityId1 = pF.newQualifiedName("uri", "activity1", "ex");
        QualifiedName activityId2 = pF.newQualifiedName("uri", "activity2", "ex");

        QualifiedName agentId1 = pF.newQualifiedName("uri", "agent1", "ex");
        QualifiedName agentId2 = pF.newQualifiedName("uri", "agent2", "ex");


        return Stream.of(
                new Object[]{pF.newUsed(pF.newQualifiedName("uri", "used", "ex"), activityId1, entityId1)},
                new Object[]{pF.newWasStartedBy(pF.newQualifiedName("uri", "wasStartedBy", "ex"), activityId1, entityId2)},
                new Object[]{pF.newWasAssociatedWith(pF.newQualifiedName("uri", "associatedWith", "ex"), activityId1, agentId1)},
                new Object[]{pF.newWasAttributedTo(pF.newQualifiedName("uri", "attributedTo", "ex"), entityId1, agentId1)},
                new Object[]{pF.newWasDerivedFrom(pF.newQualifiedName("uri", "wasDerivedFrom", "ex"), entityId1, entityId2)},
                new Object[]{pF.newActedOnBehalfOf(pF.newQualifiedName("uri", "actedOnBehalfOf", "ex"), agentId1, agentId2)},
                new Object[]{pF.newAlternateOf(entityId1, entityId2)},
                new Object[]{pF.newWasGeneratedBy(pF.newQualifiedName("uri", "wasGeneratedBy", "ex"), entityId1, activityId1)},
                new Object[]{pF.newWasInvalidatedBy(pF.newQualifiedName("uri", "wasInvalidatedBy", "ex"), entityId1, activityId1)},
                new Object[]{pF.newWasEndedBy(pF.newQualifiedName("uri", "wasEndedBy", "ex"), activityId1, entityId1)},
                new Object[]{pF.newWasInformedBy(pF.newQualifiedName("uri", "wasInformedBy", "ex"), activityId1, activityId2)},
                new Object[]{pF.newSpecializationOf(entityId1, entityId2)},
                new Object[]{pF.newQualifiedSpecializationOf(pF.newQualifiedName("uri", "qualifiedSpecializationOf", "ex"), entityId1, entityId2, Collections.emptyList())},
                new Object[]{pF.newQualifiedAlternateOf(pF.newQualifiedName("uri", "qualifiedAlternateOf", "ex"), entityId1, entityId2, Collections.emptyList())}
        );
    }

}
