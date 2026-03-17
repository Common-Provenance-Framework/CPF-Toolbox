package cz.muni.fi.cpm.template.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle.Kind;

import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.divided.ordered.CpmOrderedFactory;
import cz.muni.fi.cpm.merged.CpmMergedFactory;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.INode;
import cz.muni.fi.cpm.template.schema.BackwardConnector;
import cz.muni.fi.cpm.template.schema.ForwardConnector;
import cz.muni.fi.cpm.template.schema.MainActivity;
import cz.muni.fi.cpm.template.schema.MainActivityUsed;
import cz.muni.fi.cpm.template.schema.ReceiverAgent;
import cz.muni.fi.cpm.template.schema.SenderAgent;
import cz.muni.fi.cpm.template.schema.SpecForwardConnector;
import cz.muni.fi.cpm.template.schema.TraversalInformation;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;

public class TraversalInformationTest {

  private DatatypeFactory datatypeFactory;
  private ProvFactory pF;
  private CpmProvFactory cpmProvFactory;

  private TemplateProvMapper mapper;
  private TemplateProvMapper mergedMapper;

  @BeforeEach
  public void setUp() throws Exception {
    datatypeFactory = DatatypeFactory.newInstance();
    cpmProvFactory = new CpmProvFactory();
    pF = cpmProvFactory.getProvFactory();

    mapper = new TemplateProvMapper(cpmProvFactory);
    mergedMapper = new TemplateProvMapper(cpmProvFactory, true);
  }

  @Test
  public void toDocument_null_returnsNull() {
    assertNull(mapper.toProvDocument((TraversalInformation) null));
  }

  @Test
  public void toDocument_emptyTI_returnsNull() {
    assertThrows(IllegalArgumentException.class,
        () -> mapper.toProvDocument(new TraversalInformation()));
  }

  @Test
  public void toDocument_basicTI_returnsDocument() {
    TraversalInformation ti = new TraversalInformation();

    ti.setPrefixes(Map.of("ex", "www.example.com/"));
    ti.setBundleName(ti.getNamespace().qualifiedName("ex", "bundle1", pF));

    QualifiedName mAID = ti.getNamespace().qualifiedName("ex", "activity1", pF);
    MainActivity mA = new MainActivity(mAID);
    XMLGregorianCalendar startTime = datatypeFactory.newXMLGregorianCalendar("2011-11-16T16:05:00");
    mA.setStartTime(startTime);
    XMLGregorianCalendar endTime = datatypeFactory.newXMLGregorianCalendar("2011-11-16T18:05:00");
    mA.setEndTime(endTime);
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

    QualifiedName specFcID = ti.getNamespace().qualifiedName("ex", "specForwardConnector1", pF);
    SpecForwardConnector specFC = new SpecForwardConnector(specFcID);
    specFC.setSpecializationOf(fcID);

    ti.getSpecForwardConnectors().add(specFC);

    Document doc = mapper.toProvDocument(ti);

    assertNotNull(doc);
    CpmDocument cpmDoc = new CpmDocument(doc, pF, cpmProvFactory, new CpmMergedFactory(pF));
    assertEquals(ti.getBundleName(), cpmDoc.getBundleId());

    cpmDoc.getEdges().forEach(e -> {
      System.out.println(e.getKind());
      System.out.println(e.getEffect().getId() + " -> " + e.getCause().getId());
    });

    INode mANode = cpmDoc.getMainActivity();
    assertNotNull(mANode);
    assertEquals(mAID, mANode.getId());
    assertEquals(Kind.PROV_ACTIVITY, mANode.getKind());
    assertEquals(startTime, ((Activity) mANode.getAnyElement()).getStartTime());
    assertEquals(endTime, ((Activity) mANode.getAnyElement()).getEndTime());

    assertEquals(1, cpmDoc.getBackwardConnectors().size());
    assertEquals(bcID, cpmDoc.getBackwardConnectors().getFirst().getId());

    assertEquals(1, cpmDoc.getForwardConnectors().size());
    assertEquals(fcID, cpmDoc.getForwardConnectors().getFirst().getId());
    assertNotNull(cpmDoc.getEdge(fcID, bcID, Kind.PROV_DERIVATION));

    assertEquals(1, cpmDoc.getSpecForwardConnectors().size());
    assertEquals(specFcID, cpmDoc.getSpecForwardConnectors().getFirst().getId());
    assertNotNull(cpmDoc.getEdge(specFcID, fcID, Kind.PROV_SPECIALIZATION));
  }

  @Test
  public void toDocument_mergeAgents_returnsDocument() {
    TraversalInformation ti = new TraversalInformation();

    ti.setPrefixes(Map.of("ex", "www.example.com/"));
    ti.setBundleName(ti.getNamespace().qualifiedName("ex", "bundle1", pF));

    QualifiedName agentID = ti.getNamespace().qualifiedName("ex", "agent", pF);

    SenderAgent stationSenderAg = new SenderAgent(agentID);
    ti.setSenderAgents(List.of(stationSenderAg));

    ReceiverAgent stationAg = new ReceiverAgent(agentID);
    ti.setReceiverAgents(List.of(stationAg));

    Document doc = mergedMapper.toProvDocument(ti);

    assertNotNull(doc);
    CpmDocument cpmDoc = new CpmDocument(doc, pF, cpmProvFactory, new CpmOrderedFactory(pF));

    INode agentNode = cpmDoc.getNode(agentID, Kind.PROV_AGENT);
    assertNotNull(agentNode);
    assertEquals(1, agentNode.getElements().size());
    assertTrue(CpmUtilities.hasCpmType(agentNode, CpmType.SENDER_AGENT));
    assertTrue(CpmUtilities.hasCpmType(agentNode, CpmType.RECEIVER_AGENT));
  }

  @Test
  public void toDocument_separateAgents_returnsDocument() {
    TraversalInformation ti = new TraversalInformation();

    ti.setPrefixes(Map.of("ex", "www.example.com/"));
    ti.setBundleName(ti.getNamespace().qualifiedName("ex", "bundle1", pF));

    QualifiedName agentID = ti.getNamespace().qualifiedName("ex", "agent", pF);

    SenderAgent stationSenderAg = new SenderAgent(agentID);
    ti.setSenderAgents(List.of(stationSenderAg));

    ReceiverAgent stationAg = new ReceiverAgent(agentID);
    ti.setReceiverAgents(List.of(stationAg));

    Document doc = mapper.toProvDocument(ti);

    assertNotNull(doc);
    CpmDocument cpmDoc = new CpmDocument(doc, pF, cpmProvFactory, new CpmOrderedFactory(pF));

    INode agentNode = cpmDoc.getNode(agentID, Kind.PROV_AGENT);
    assertNotNull(agentNode);
    assertEquals(2, agentNode.getElements().size());
    assertTrue(CpmUtilities.hasCpmType(agentNode, CpmType.SENDER_AGENT));
    assertTrue(CpmUtilities.hasCpmType(agentNode, CpmType.RECEIVER_AGENT));
  }
}
