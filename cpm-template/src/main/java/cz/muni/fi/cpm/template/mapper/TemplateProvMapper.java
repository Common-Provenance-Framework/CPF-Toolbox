package cz.muni.fi.cpm.template.mapper;

import static cz.muni.fi.cpm.template.constants.CpmTemplateExceptionConstants.NULL_BUNDLE_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;

import cz.muni.fi.cpm.model.ICpmProvFactory;
import cz.muni.fi.cpm.template.schema.BackwardConnector;
import cz.muni.fi.cpm.template.schema.CpmAgent;
import cz.muni.fi.cpm.template.schema.ForwardConnector;
import cz.muni.fi.cpm.template.schema.HashAlgorithms;
import cz.muni.fi.cpm.template.schema.IdentifierEntity;
import cz.muni.fi.cpm.template.schema.MainActivity;
import cz.muni.fi.cpm.template.schema.MergedAgent;
import cz.muni.fi.cpm.template.schema.SpecForwardConnector;
import cz.muni.fi.cpm.template.schema.TraversalInformation;

public class TemplateProvMapper implements ITemplateProvMapper {
  private final ICpmProvFactory cPF;
  private boolean mergeAgents;

  public TemplateProvMapper(ICpmProvFactory cPF) {
    this.cPF = cPF;
    this.mergeAgents = false;
  }

  public TemplateProvMapper(ICpmProvFactory cPF, boolean mergeAgents) {
    this.cPF = cPF;
    this.mergeAgents = mergeAgents;
  }

  public boolean isMergeAgents() {
    return mergeAgents;
  }

  public void setMergeAgents(boolean mergeAgents) {
    this.mergeAgents = mergeAgents;
  }

  public Stream<Statement> toStatementsStream(BackwardConnector backwardConnector) {
    return scalarToStreamSafe(backwardConnector)
        .flatMap(connector -> {
          Entity entity = cPF.newCpmBackwardConnector(connector.getId());

          Optional.ofNullable(connector.getExternalId())
              .map(cPF::newCpmAttributeExternalId)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getHashAlg())
              .map(HashAlgorithms::toString)
              .map(cPF::newCpmAttributeHashAlg)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedBundleHashValue())
              .map(cPF::newCpmAttributeReferencedBundleHashValue)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedBundleId())
              .map(cPF::newCpmAttributeReferencedBundleId)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedBundleSpecV())
              .map(cPF::newCpmAttributeReferencedBundleSpecV)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedMetaBundleSpecV())
              .map(cPF::newCpmAttributeReferencedMetaBundleSpecV)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedMetaBundleId())
              .map(cPF::newCpmAttributeReferencedMetaBundleId)
              .ifPresent(entity.getOther()::add);

          Stream<Statement> relations = scalarToStreamSafe(connector.getAttributedTo())
              .map(attributedTo -> cPF.getProvFactory().newWasAttributedTo(
                  attributedTo.getId(),
                  connector.getId(),
                  attributedTo.getAgentId()));

          relations = Stream.concat(
              relations,
              listToStreamSafe(connector.getDerivedFrom())
                  .map(derivedFrom -> cPF.getProvFactory()
                      .newWasDerivedFrom(connector.getId(), derivedFrom)));

          return Stream.concat(relations, Stream.of(entity));
        });
  }

  public Stream<Statement> toStatementsStream(ForwardConnector forwardConnector) {
    return scalarToStreamSafe(forwardConnector)
        .flatMap(connector -> {
          Entity entity = cPF.newCpmForwardConnector(connector.getId());

          Optional.ofNullable(connector.getExternalId())
              .map(cPF::newCpmAttributeExternalId)
              .ifPresent(entity.getOther()::add);

          Stream<Statement> relations = listToStreamSafe(connector.getDerivedFrom())
              .map(derivedFrom -> cPF.getProvFactory()
                  .newWasDerivedFrom(connector.getId(), derivedFrom));

          return Stream.concat(relations, Stream.of(entity));
        });
  }

  public Stream<Statement> toStatementsStream(SpecForwardConnector specForwardConnector) {
    return scalarToStreamSafe(specForwardConnector)
        .flatMap(connector -> {
          Entity entity = cPF.newCpmSpecForwardConnector(connector.getId());

          Optional.ofNullable(connector.getExternalId())
              .map(cPF::newCpmAttributeExternalId)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getHashAlg())
              .map(HashAlgorithms::toString)
              .map(cPF::newCpmAttributeHashAlg)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getProvenanceServiceUri())
              .map(cPF::newCpmAttributeProvenanceServiceUri)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedBundleHashValue())
              .map(cPF::newCpmAttributeReferencedBundleHashValue)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedBundleId())
              .map(cPF::newCpmAttributeReferencedBundleId)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedMetaBundleId())
              .map(cPF::newCpmAttributeReferencedMetaBundleId)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedBundleSpecV())
              .map(cPF::newCpmAttributeReferencedBundleSpecV)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(connector.getReferencedMetaBundleSpecV())
              .map(cPF::newCpmAttributeReferencedMetaBundleSpecV)
              .ifPresent(entity.getOther()::add);

          Stream<Statement> relations = scalarToStreamSafe(connector.getAttributedTo())
              .map(attributedTo -> cPF.getProvFactory().newWasAttributedTo(
                  attributedTo.getId(),
                  connector.getId(),
                  attributedTo.getAgentId()));

          relations = Stream.concat(
              relations,
              scalarToStreamSafe(connector.getSpecializationOf())
                  .map(specializationOf -> cPF.getProvFactory().newSpecializationOf(
                      connector.getId(),
                      specializationOf)));

          relations = Stream.concat(
              relations,
              listToStreamSafe(connector.getDerivedFrom())
                  .map(derivedFrom -> cPF.getProvFactory()
                      .newWasDerivedFrom(connector.getId(), derivedFrom)));
          return Stream.concat(relations, Stream.of(entity));
        });
  }

  private UnaryOperator<Agent> setContactPid(CpmAgent cpmAgent) {
    return (Agent agent) -> Optional.ofNullable(cpmAgent.getContactIdPid())
        .map(cPF::newCpmAttributeContactIdPid)
        .map(o -> {
          agent.getOther().add(o);
          return agent;
        })
        .orElse(agent);
  }

  private Agent buildAgent(CpmAgent cpmAgent) {
    return cPF.newCpmAgent(cpmAgent.getId(), cpmAgent.getType(), new ArrayList<Attribute>());
  }

  public Stream<Statement> toStatementsStream(CpmAgent cpmAgent) {
    return scalarToStreamSafe(cpmAgent)
        .map(this::buildAgent)
        .map(this.setContactPid(cpmAgent));
  }

  public Stream<Statement> toStatementsStream(IdentifierEntity identifierEntity) {
    return scalarToStreamSafe(identifierEntity)
        .map(iE -> {
          Entity entity = cPF.newCpmIdentifierEntity(iE.getId());

          Optional.ofNullable(iE.getExternalId())
              .map(cPF::newCpmAttributeExternalId)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(iE.getExternalIdType())
              .map(cPF::newCpmAttributeExternalIdType)
              .ifPresent(entity.getOther()::add);

          Optional.ofNullable(iE.getComment())
              .map(cPF::newCpmAttributeComment)
              .ifPresent(entity.getOther()::add);

          return entity;
        });
  }

  public Stream<Statement> toStatementsStream(MainActivity mainActivity) {
    return scalarToStreamSafe(mainActivity)
        .flatMap(mA -> {
          Activity activity = cPF.newCpmMainActivity(mA.getId(), mA.getStartTime(), mA.getEndTime());

          Optional.ofNullable(mA.getReferencedMetaBundleId())
              .map(cPF::newCpmAttributeReferencedMetaBundleId)
              .ifPresent(activity.getOther()::add);

          listToStreamSafe(mA.getHasPart())
              .map(cPF::newDctAttribute)
              .forEach(activity.getOther()::add);

          Stream<Statement> relations = listToStreamSafe(mA.getUsed())
              .map(mainActivityUsed -> cPF.getProvFactory().newUsed(
                  mainActivityUsed.getId(),
                  mA.getId(),
                  mainActivityUsed.getBackwardConnectorId()));

          relations = Stream.concat(
              relations,
              listToStreamSafe(mA.getGenerated())
                  .map(forwardConnector -> cPF.getProvFactory().newWasGeneratedBy(null, forwardConnector, mA.getId())));

          relations = Stream.concat(
              relations,
              scalarToStreamSafe(mA.getAssociatedWith())
                  .map(associatedWith -> cPF.getProvFactory().newWasAssociatedWith(
                      associatedWith.getId(),
                      mA.getId(),
                      associatedWith.getAgentId())));

          return Stream.concat(relations, Stream.of(activity));
        });
  }

  private <T> Stream<T> listToStreamSafe(List<T> list) {
    return Optional.ofNullable(list).stream().flatMap(List::stream);
  }

  private <T> Stream<T> scalarToStreamSafe(T scalar) {
    return Optional.ofNullable(scalar).stream();
  }

  /**
   * Every agent of the traversal information, in emission order: receivers,
   * then senders, then the current agent.
   */
  private List<CpmAgent> allAgents(TraversalInformation ti) {
    return Stream.concat(
        Stream.concat(
            listToStreamSafe(ti.getReceiverAgents()),
            listToStreamSafe(ti.getSenderAgents())),
        scalarToStreamSafe(ti.getCurrentAgent()))
        .toList();
  }

  /**
   * Folds agents sharing an identifier into a single agent carrying every role
   * they hold, keeping the order of first appearance.
   */
  private List<CpmAgent> mergeById(List<CpmAgent> agents) {
    Map<QualifiedName, MergedAgent> byId = new LinkedHashMap<>();

    for (CpmAgent agent : agents) {
      byId.computeIfAbsent(agent.getId(), id -> new MergedAgent(id, agent.getContactIdPid()))
          .merge(agent);
    }

    return new ArrayList<>(byId.values());
  }

  @Override
  public Document toProvDocument(TraversalInformation ti) {
    // TODO: Should be Optional!
    if (ti == null) {
      return null;
    }

    if (ti.getBundleName() == null) {
      throw new IllegalArgumentException(NULL_BUNDLE_NAME);
    }

    Namespace bundleNamespace = new Namespace();
    bundleNamespace.setParent(ti.getNamespace());
    Bundle tiBundle = cPF.getProvFactory().newNamedBundle(
        ti.getBundleName(),
        bundleNamespace,
        new ArrayList<Statement>());

    scalarToStreamSafe(ti.getMainActivity())
        .flatMap(this::toStatementsStream)
        .forEach(tiBundle.getStatement()::add);

    listToStreamSafe(ti.getBackwardConnectors())
        .flatMap(this::toStatementsStream)
        .forEach(tiBundle.getStatement()::add);

    listToStreamSafe(ti.getForwardConnectors())
        .flatMap(this::toStatementsStream)
        .forEach(tiBundle.getStatement()::add);

    listToStreamSafe(ti.getSpecForwardConnectors())
        .flatMap(this::toStatementsStream)
        .forEach(tiBundle.getStatement()::add);

    listToStreamSafe(ti.getIdentifierEntities())
        .flatMap(this::toStatementsStream)
        .forEach(tiBundle.getStatement()::add);

    List<CpmAgent> agents = mergeAgents ? mergeById(allAgents(ti)) : allAgents(ti);

    agents.stream()
        .flatMap(this::toStatementsStream)
        .forEach(tiBundle.getStatement()::add);

    ti.getNamespace().extendWith(cPF.newCpmNamespace());

    return cPF.getProvFactory().newDocument(ti.getNamespace(), Collections.singletonList(tiBundle));
  }
}
