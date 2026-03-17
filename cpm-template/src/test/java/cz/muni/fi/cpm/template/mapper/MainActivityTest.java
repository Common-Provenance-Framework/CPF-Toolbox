package cz.muni.fi.cpm.template.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.StatementOrBundle.Kind;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasGeneratedBy;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.constants.DctAttributeConstants;
import cz.muni.fi.cpm.template.schema.MainActivity;
import cz.muni.fi.cpm.template.schema.MainActivityUsed;
import cz.muni.fi.cpm.vanilla.CpmProvFactory;

public class MainActivityTest {
  private TemplateProvMapper mapper;
  private CpmProvFactory cpmProvFactory;
  private ProvFactory provFactory;

  @BeforeEach
  public void setUp() {
    this.cpmProvFactory = new CpmProvFactory();
    this.provFactory = this.cpmProvFactory.getProvFactory();
    this.mapper = new TemplateProvMapper(this.cpmProvFactory);
  }

  @Test
  public void toStatements_basicIdSet_returnsOneStatement() {
    MainActivity mainActivity = new MainActivity();
    QualifiedName id = provFactory.newQualifiedName("uri", "activityExample", "ex");
    mainActivity.setId(id);

    List<Statement> statements = mapper.toStatementsStream(mainActivity).toList();

    assertNotNull(statements);
    assertEquals(1, statements.size());

    Statement statement = statements.getFirst();
    assertInstanceOf(Activity.class, statement);

    Activity activity = (Activity) statement;
    assertEquals(id, activity.getId());

    assertNotNull(activity.getType());
    assertEquals(1, activity.getType().size());
    Type type = activity.getType().getFirst();
    assertEquals(CpmType.MAIN_ACTIVITY.toString(), ((QualifiedName) type.getValue()).getLocalPart());
  }

  @Test
  public void toStatements_withReferencedMetaBundleId_returnsMetaBundleId() {
    MainActivity mainActivity = new MainActivity();
    mainActivity.setId(provFactory.newQualifiedName("uri", "activityExample", "ex"));
    QualifiedName referencedMetaBundleId = provFactory.newQualifiedName("uri", "metaBundleId", "ex");
    mainActivity.setReferencedMetaBundleId(referencedMetaBundleId);

    List<Statement> statements = mapper.toStatementsStream(mainActivity).toList();
    Activity activity = (Activity) statements.getFirst();

    assertNotNull(activity.getOther());
    assertEquals(1, activity.getOther().size());
    assertEquals(CpmAttribute.REFERENCED_META_BUNDLE_ID.toString(),
        activity.getOther().getFirst().getElementName().getLocalPart());
    assertInstanceOf(QualifiedName.class, activity.getOther().getFirst().getValue());
    assertEquals(referencedMetaBundleId, activity.getOther().getFirst().getValue());
  }

  @Test
  public void toStatements_withHasPart_returnsCorrectHasPart() {
    MainActivity mainActivity = new MainActivity();
    mainActivity.setId(provFactory.newQualifiedName("uri", "activityExample", "ex"));
    QualifiedName hasPart = provFactory.newQualifiedName("uri", "hasPart", "ex");
    mainActivity.setHasPart(List.of(hasPart));

    List<Statement> statements = mapper.toStatementsStream(mainActivity).toList();
    Activity activity = (Activity) statements.getFirst();

    assertNotNull(activity.getOther());
    assertEquals(1, activity.getOther().size());
    assertEquals(DctAttributeConstants.HAS_PART, activity.getOther().getFirst().getElementName().getLocalPart());
    assertInstanceOf(QualifiedName.class, activity.getOther().getFirst().getValue());
    assertEquals(hasPart, activity.getOther().getFirst().getValue());
  }

  @Test
  public void toStatements_withUsed_returnsCorrectUsed() {
    MainActivity mainActivity = new MainActivity();
    QualifiedName activity = provFactory.newQualifiedName("uri", "activityExample", "ex");
    mainActivity.setId(activity);

    QualifiedName uId = provFactory.newQualifiedName("uri", "usedId1", "ex");
    QualifiedName uBc = provFactory.newQualifiedName("uri", "used1", "ex");
    MainActivityUsed mainActivityUsed = new MainActivityUsed();
    mainActivityUsed.setId(uId);
    mainActivityUsed.setBackwardConnectorId(uBc);

    QualifiedName uBc2 = provFactory.newQualifiedName("uri", "used2", "ex");
    MainActivityUsed mainActivityUsed2 = new MainActivityUsed();
    mainActivityUsed2.setBackwardConnectorId(uBc2);

    List<MainActivityUsed> usedList = Arrays.asList(mainActivityUsed, mainActivityUsed2);
    mainActivity.setUsed(usedList);

    List<Statement> statements = mapper.toStatementsStream(mainActivity).toList();

    assertEquals(3, statements.size());

    List<Used> usedRelations = statements.stream()
        .filter(Used.class::isInstance)
        .map(Used.class::cast)
        .toList();

    assertEquals(2, usedRelations.size());

    Optional<Used> relation1 = usedRelations.stream()
        .filter(relation -> Optional.ofNullable(relation.getId()).isPresent())
        .findFirst();

    assertTrue(relation1.isPresent());
    assertEquals(activity, relation1.get().getActivity());
    assertEquals(uBc, relation1.get().getEntity());

    Optional<Used> relation2 = usedRelations.stream()
        .filter(relation -> Optional.ofNullable(relation.getId()).isEmpty())
        .findFirst();

    assertTrue(relation2.isPresent());
    assertEquals(activity, relation2.get().getActivity());
    assertEquals(uBc2, relation2.get().getEntity());
  }

  @Test
  public void toStatements_withGenerated_returnsCorrectGenerated() {
    MainActivity mainActivity = new MainActivity();
    QualifiedName activity = provFactory.newQualifiedName("uri", "activityExample", "ex");
    mainActivity.setId(activity);

    List<QualifiedName> generatedList = new ArrayList<>();
    QualifiedName gId1 = provFactory.newQualifiedName("uri", "generated1", "ex");
    generatedList.add(gId1);
    QualifiedName gId2 = provFactory.newQualifiedName("uri", "generated2", "ex");
    generatedList.add(gId2);
    mainActivity.setGenerated(generatedList);

    List<Statement> statements = mapper.toStatementsStream(mainActivity).toList();

    assertEquals(3, statements.size());

    List<WasGeneratedBy> relations = statements.stream()
        .filter(WasGeneratedBy.class::isInstance)
        .map(WasGeneratedBy.class::cast)
        .toList();

    assertEquals(2, relations.size());

    Optional<WasGeneratedBy> relation1 = relations.stream()
        .filter(rel -> rel.getEntity().equals(gId1))
        .findFirst();

    assertTrue(relation1.isPresent());
    assertEquals(activity, relation1.get().getActivity());
    assertNull(relation1.get().getId());
    assertEquals(Kind.PROV_GENERATION, relation1.get().getKind());
    assertTrue(relation1.get().getLabel().isEmpty());
    assertTrue(relation1.get().getLocation().isEmpty());
    assertTrue(relation1.get().getOther().isEmpty());
    assertTrue(relation1.get().getRole().isEmpty());
    assertNull(relation1.get().getTime());
    assertTrue(relation1.get().getType().isEmpty());

    Optional<WasGeneratedBy> relation2 = relations.stream()
        .filter(rel -> rel.getEntity().equals(gId1))
        .findFirst();
    assertTrue(relation2.isPresent());
    assertEquals(activity, relation2.get().getActivity());
    assertNull(relation2.get().getId());
    assertEquals(Kind.PROV_GENERATION, relation2.get().getKind());
  }

  @Test
  public void toStatements_withTimes_returnsCorrectTimes() throws Exception {
    MainActivity mainActivity = new MainActivity();
    mainActivity.setId(provFactory.newQualifiedName("uri", "activityExample", "ex"));

    DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
    XMLGregorianCalendar startTime = datatypeFactory.newXMLGregorianCalendar("2024-11-13T10:00:00");
    XMLGregorianCalendar endTime = datatypeFactory.newXMLGregorianCalendar("2024-11-13T12:00:00");
    mainActivity.setStartTime(startTime);
    mainActivity.setEndTime(endTime);

    List<Statement> statements = mapper.toStatementsStream(mainActivity).toList();

    Activity activity = (Activity) statements.get(0);
    assertEquals(startTime, activity.getStartTime());
    assertEquals(endTime, activity.getEndTime());
  }

  @Test
  public void toStatements_nullActivity_returnsNull() {
    assertTrue(mapper.toStatementsStream((MainActivity) null).toList().isEmpty());
  }
}
