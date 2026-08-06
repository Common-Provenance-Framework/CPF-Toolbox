# CPF Toolbox — Usage Guide

## Prerequisites

### Requirements

| | Minimum version |
|---|---|
| Java | 23 |
| Maven | 3.9 |

### Installation

The CPF Toolbox is not published to Maven Central. Download the JAR files from the [GitHub Releases](https://github.com/dwwop/cpm/releases) page and install them locally by following the [installation steps in OLD_README](OLD_README.md#installation).

After installation, add these dependencies to your `pom.xml`:

<details>
<summary><b>Click to expand — Maven dependencies (pom.xml)</b></summary>

```xml
<!-- CPF Toolbox (locally installed — use the version matching your JARs) -->
<dependency>
    <groupId>cz.muni.fi.cpm</groupId>
    <artifactId>cpm-core</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>cz.muni.fi.cpm</groupId>
    <artifactId>cpm-template</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- ProvToolBox (available on Maven Central) -->
<dependency>
    <groupId>org.openprovenance.prov</groupId>
    <artifactId>prov-model</artifactId>
    <version>2.2.1</version>
</dependency>
<dependency>
    <groupId>org.openprovenance.prov</groupId>
    <artifactId>prov-nf</artifactId>
    <version>2.2.1</version>
</dependency>
<dependency>
    <groupId>org.openprovenance.prov</groupId>
    <artifactId>prov-interop</artifactId>
    <version>2.2.1</version>
</dependency>
```

</details>

### Java imports

```java
// Step 1 — deserialize traversal backbone
import cz.muni.fi.cpm.template.deserialization.TraversalInformationDeserializer;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Bundle;

// Step 2 — add domain-specific part
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.vanilla.ProvFactory;
import java.util.Arrays;

// Step 3 — serialize
import org.openprovenance.prov.interop.InteropFramework;
```

---

## What This Toolbox Does

The CPF Toolbox generates the **traversal information** part of a CPM bundle. Traversal information is the standardized backbone that links provenance bundles across organizations — it contains the main activity, backward and forward connectors, and sender/receiver agents.

The toolbox does **not** generate the domain-specific part. Domain-specific provenance — the actual samples, devices, protocols, sub-activities, and their relations — is added separately after the traversal backbone is built. How the domain-specific part is structured depends on each use case and is handled case by case.

A complete CPM bundle consists of both parts, connected by `specializationOf` relations:

```
┌─────────────────────────────────────────────────────┐
│                    CPM Bundle                       │
│                                                     │
│  ┌──────────────────────┐  ┌─────────────────────┐  │
│  │ Traversal Information│  │ Domain-Specific Part │  │
│  │                      │  │                      │  │
│  │  Main Activity       │  │  Real samples        │  │
│  │  Backward Connector ◄├──┤► Sample entity       │  │
│  │  Forward Connector  ◄├──┤► Aliquot entity      │  │
│  │  Sender/Receiver Agt │  │  Devices, SOPs       │  │
│  │                      │  │  Sub-activities      │  │
│  └──────────────────────┘  └─────────────────────┘  │
│           ▲                        ▲                │
│           └── specializationOf ────┘                │
└─────────────────────────────────────────────────────┘
```

The arrows between the two parts represent `specializationOf` relations — they attach each domain entity to its corresponding connector in the traversal backbone.

---

## Workflow

Building a CPM bundle is a three-step process:

**Step 1 — Generate the traversal backbone.** Write a JSON template describing the main activity, connectors, and agents. Feed it to `TraversalInformationDeserializer`, which produces a ProvToolBox `Document` containing one bundle with the traversal information.

**Step 2 — Add the domain-specific part.** Retrieve the bundle from the Document and add domain entities (samples, devices, protocols), domain activities (sub-steps), and domain relations (`used`, `wasGeneratedBy`, `wasDerivedFrom`) using the standard ProvToolBox API. Add `specializationOf` relations to connect domain entities to their corresponding connectors.

**Step 3 — Serialize.** Write the complete Document to PROV-JSON (or any other supported format) using `InteropFramework`.

```java
// Step 1
InputStream in = new FileInputStream("template.json");
Document doc = new TraversalInformationDeserializer().deserializeDocument(in);

// Step 2
Bundle bundle = (Bundle) doc.getStatementOrBundle().get(0);
// ... add domain entities, relations, specializationOf links ...

// Step 3
new InteropFramework().writeDocument("output.json", doc);
```

Steps 2 and 3 use the ProvToolBox API directly — the CPF Toolbox is only involved in Step 1.

---

## Building the Input JSON Template

The input template is a JSON file that describes the traversal information for one bundle. It is **not** PROV-JSON — it is a separate format consumed by `TraversalInformationDeserializer`. The full schema is defined in [template_schema.json](cpm-template/src/main/resources/template_schema.json).

### Required fields

| Field | Type | Description |
|---|---|---|
| `prefixes` | object | Namespace declarations. Every prefix used in identifiers must be declared here. |
| `bundleName` | string | Qualified name for the output bundle (e.g. `"gen:bundle_dna_extraction_TS4420"`). |
| `mainActivity.id` | string | Qualified name for the main activity — the single core process of this bundle. |

### Optional fields

| Field | Type | Description |
|---|---|---|
| `mainActivity.startTime` | string | ISO-8601 timestamp (e.g. `"2025-03-10T09:30:00.000Z"`). |
| `mainActivity.endTime` | string | ISO-8601 timestamp. |
| `mainActivity.hasPart` | array of strings | Qualified names of **sub-activities** (not devices or protocols). |
| `mainActivity.used` | array of objects | Each entry has `id` (relation identifier) and `backwardConnectorId` (the backward connector the activity consumes). |
| `mainActivity.generated` | array of strings | Qualified names of forward connectors the activity produces. |
| `backwardConnectors` | array of objects | Each has `id` (required), plus optional `externalId`, `attributedTo`, `derivedFrom`, and hash/reference attributes. |
| `forwardConnectors` | array of objects | Each has `id` (required), plus optional `externalId`, `derivedFrom`, `attributedTo`. |
| `senderAgents` | array of objects | Each has `id` (required). |
| `receiverAgents` | array of objects | Each has `id` (required). |
| `identifierEntities` | array of objects | External identifiers only (e.g. accession numbers). Each has `id`, `externalId`, `externalIdType`. |

### Identifier rules

Every identifier in the template must follow the `prefix:localName` format, where `prefix` is a key in `prefixes`.

The local name part (after the colon) must contain only letters, digits, hyphens, and underscores. Colons, spaces, parentheses, and accented characters in local names cause escaping issues in the output and break interoperability with other PROV tools.

Put human-readable descriptions in `externalId` or `prov:label` attributes — not in the identifier itself.

```
gen:centrifuge_5810R              ← clean identifier
gen:Centrifuge Model 5810R        ← will cause escaping problems
```

### Basic example

One backward connector, one forward connector, one main activity:

**Input** (`template_minimal.json`):
```json
{
  "prefixes": {
    "ex": "https://example.org/"
  },
  "bundleName": "ex:bundle1",
  "mainActivity": {
    "id": "ex:mainActivity1",
    "used": [
      { "id": "ex:use1", "backwardConnectorId": "ex:backwardConnector1" }
    ],
    "generated": ["ex:forwardConnector1"]
  },
  "backwardConnectors": [
    { "id": "ex:backwardConnector1" }
  ],
  "forwardConnectors": [
    { "id": "ex:forwardConnector1", "derivedFrom": ["ex:backwardConnector1"] }
  ]
}
```

**Output** (PROV-JSON):

<details>
<summary><b>Click to expand — Output PROV-JSON</b></summary>

```json
{
  "prefix": {
    "cpm": "https://www.commonprovenancemodel.org/cpm-namespace-v1-0/",
    "ex": "https://example.org/",
    "dct": "http://purl.org/dc/terms/",
    "xsd": "http://www.w3.org/2001/XMLSchema#",
    "prov": "http://www.w3.org/ns/prov#"
  },
  "bundle": {
    "ex:bundle1": {
      "entity": {
        "ex:backwardConnector1": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:backwardConnector"}]
        },
        "ex:forwardConnector1": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:forwardConnector"}]
        }
      },
      "activity": {
        "ex:mainActivity1": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:mainActivity"}]
        }
      },
      "used": {
        "ex:use1": {
          "prov:activity": "ex:mainActivity1",
          "prov:entity": "ex:backwardConnector1"
        }
      },
      "wasDerivedFrom": {
        "_:n1": {
          "prov:generatedEntity": "ex:forwardConnector1",
          "prov:usedEntity": "ex:backwardConnector1"
        }
      },
      "wasGeneratedBy": {
        "_:n0": {
          "prov:entity": "ex:forwardConnector1",
          "prov:activity": "ex:mainActivity1"
        }
      }
    }
  }
}
```

</details>

The toolbox automatically:
- Assigns CPM types (`cpm:mainActivity`, `cpm:backwardConnector`, `cpm:forwardConnector`)
- Generates `wasGeneratedBy` from `mainActivity.generated`
- Generates `wasDerivedFrom` from `forwardConnectors[].derivedFrom`
- Generates `used` from `mainActivity.used`
- Adds `cpm`, `dct`, `xsd`, `prov` namespace prefixes

### Realistic example — biobank processing

A biobank receives a tissue sample and extracts two DNA aliquots. The template describes only the traversal backbone — the equipment, protocol, and actual sample entities are added in Step 2.

**Input** (`template_dna_extraction.json`):

<details>
<summary><b>Click to expand — Input JSON template</b></summary>

```json
{
  "prefixes": {
    "gen": "https://biobank-gen.example.org/",
    "pbm": "https://w3id.org/2023/prov-biobank-model/",
    "cpm": "https://www.commonprovenancemodel.org/cpm-namespace-v1-0/",
    "dct": "http://purl.org/dc/terms/"
  },
  "bundleName": "gen:bundle_dna_extraction_TS4420",
  "mainActivity": {
    "id": "gen:dnaExtraction_TS4420",
    "startTime": "2025-03-10T09:30:00.000Z",
    "used": [
      {
        "id": "gen:use_tissue_TS4420",
        "backwardConnectorId": "gen:tissue_sample_TS4420"
      }
    ],
    "generated": ["gen:dna_aliquot_A1", "gen:dna_aliquot_A2"]
  },
  "backwardConnectors": [
    {
      "id": "gen:tissue_sample_TS4420",
      "externalId": "TS4420"
    }
  ],
  "forwardConnectors": [
    {
      "id": "gen:dna_aliquot_A1",
      "externalId": "A1",
      "derivedFrom": ["gen:tissue_sample_TS4420"]
    },
    {
      "id": "gen:dna_aliquot_A2",
      "externalId": "A2",
      "derivedFrom": ["gen:tissue_sample_TS4420"]
    }
  ]
}
```

</details>

**Output** (PROV-JSON):

<details>
<summary><b>Click to expand — Output PROV-JSON</b></summary>

```json
{
  "prefix": {
    "cpm": "https://www.commonprovenancemodel.org/cpm-namespace-v1-0/",
    "gen": "https://biobank-gen.example.org/",
    "dct": "http://purl.org/dc/terms/",
    "xsd": "http://www.w3.org/2001/XMLSchema#",
    "pbm": "https://w3id.org/2023/prov-biobank-model/",
    "prov": "http://www.w3.org/ns/prov#"
  },
  "bundle": {
    "gen:bundle_dna_extraction_TS4420": {
      "entity": {
        "gen:tissue_sample_TS4420": {
          "cpm:externalId": ["TS4420"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:backwardConnector"}]
        },
        "gen:dna_aliquot_A1": {
          "cpm:externalId": ["A1"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:forwardConnector"}]
        },
        "gen:dna_aliquot_A2": {
          "cpm:externalId": ["A2"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:forwardConnector"}]
        }
      },
      "activity": {
        "gen:dnaExtraction_TS4420": {
          "prov:startTime": "2025-03-10T09:30:00.000Z",
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:mainActivity"}]
        }
      },
      "used": {
        "gen:use_tissue_TS4420": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:tissue_sample_TS4420"
        }
      },
      "wasDerivedFrom": {
        "_:n5": {
          "prov:generatedEntity": "gen:dna_aliquot_A2",
          "prov:usedEntity": "gen:tissue_sample_TS4420"
        },
        "_:n4": {
          "prov:generatedEntity": "gen:dna_aliquot_A1",
          "prov:usedEntity": "gen:tissue_sample_TS4420"
        }
      },
      "wasGeneratedBy": {
        "_:n2": {
          "prov:entity": "gen:dna_aliquot_A1",
          "prov:activity": "gen:dnaExtraction_TS4420"
        },
        "_:n3": {
          "prov:entity": "gen:dna_aliquot_A2",
          "prov:activity": "gen:dnaExtraction_TS4420"
        }
      }
    }
  }
}
```

</details>

This output is the traversal backbone only. The microcentrifuge, thermal cycler, micropipette, SOP protocol, and actual DNA sample entities are not here — they belong in the domain-specific part, added in Step 2.

---

## What Must Be in the Input

For the traversal backbone to be structurally complete, the template needs:

| Element | Field | What happens if missing |
|---|---|---|
| Bundle name | `bundleName` | `IllegalArgumentException: Bundle name cannot be null` |
| Main activity | `mainActivity.id` | Deserialization fails |
| Backward connector(s) | `backwardConnectors[].id` | No input entity — the chain cannot be traced backward |
| Forward connector(s) | `forwardConnectors[].id` | No output entity — the chain cannot be traced forward |
| Forward → backward lineage | `forwardConnectors[].derivedFrom` | **No `wasDerivedFrom` in output — silent data loss** (see "What Must Not Be in the Input") |
| Activity uses input | `mainActivity.used[].backwardConnectorId` | No `used` relation — activity is disconnected from its input |
| Activity generates output | `mainActivity.generated[]` | No `wasGeneratedBy` — output entities are disconnected from the activity |
| Prefix declarations | `prefixes` | Identifiers cannot be resolved |

The toolbox does not validate CPM structural rules. A template with only a `bundleName` and a `mainActivity` (no connectors at all) is accepted without error — the output is a valid PROV-JSON document, but not a valid CPM component.

---

## What Must Not Be in the Input

The traversal information template accepts **only** the CPM navigation backbone. Domain-specific content — the actual science — is added in Step 2 using the ProvToolBox API.

### Common mistakes

| What people put in the template | Why it's wrong | Where it actually belongs |
|---|---|---|
| Devices / equipment | Not traversal information | Step 2: add as entities, link with `used` |
| SOPs / protocols | Not traversal information | Step 2: add as entities |
| Real sample entities (the actual tissue, DNA) | Not traversal information | Step 2: add as entities, link with `specializationOf` to connectors |
| Sub-activities (extraction steps, centrifugation) | Partly correct in `hasPart`, but the activities themselves go in Step 2 | `hasPart` can list their IDs; the activity declarations go in Step 2 |
| Technicians / operators | Not traversal information | Step 2: add as agents with `wasAssociatedWith` |
| Devices as agents | Template only allows sender/receiver agents | Step 2: add as entities with `used`, or as agents via ProvToolBox API |

### What goes wrong

The toolbox does not reject invalid input. It processes whatever it receives and produces output — but the output is structurally wrong.

**Example — devices and SOP placed in the template (wrong):**

<details>
<summary><b>Click to expand — Wrong input JSON (devices in template)</b></summary>

```json
{
  "prefixes": {
    "gen": "https://biobank-gen.example.org/",
    "pbm": "https://w3id.org/2023/prov-biobank-model/",
    "cpm": "https://www.commonprovenancemodel.org/cpm-namespace-v1-0/",
    "dct": "http://purl.org/dc/terms/"
  },
  "bundleName": "gen:bundle_dna_extraction_TS4420",
  "mainActivity": {
    "id": "gen:dnaExtraction_TS4420",
    "startTime": "2025-03-10T09:30:00.000Z",
    "hasPart": ["gen:SOP_DX_12", "gen:microcentrifuge_Z200", "gen:thermal_cycler_T100"],
    "used": [
      { "id": "gen:use_tissue_TS4420", "backwardConnectorId": "gen:tissue_sample_TS4420" },
      { "id": "gen:use_microcentrifuge", "backwardConnectorId": "gen:microcentrifuge_Z200" },
      { "id": "gen:use_thermal_cycler", "backwardConnectorId": "gen:thermal_cycler_T100" }
    ],
    "generated": ["gen:dna_aliquot_A1", "gen:dna_aliquot_A2"]
  },
  "backwardConnectors": [
    { "id": "gen:tissue_sample_TS4420", "externalId": "TS4420" }
  ],
  "forwardConnectors": [
    { "id": "gen:dna_aliquot_A1", "externalId": "A1" },
    { "id": "gen:dna_aliquot_A2", "externalId": "A2" }
  ],
  "identifierEntities": [
    { "id": "gen:microcentrifuge_Z200", "externalId": "Microcentrifuge_Z200", "externalIdType": "pbm:ProcessingDevice" },
    { "id": "gen:thermal_cycler_T100", "externalId": "Thermal_Cycler_T100", "externalIdType": "pbm:ProcessingDevice" },
    { "id": "gen:SOP_DX_12", "externalId": "SOP_DX_12", "externalIdType": "pbm:SOP" }
  ]
}
```

</details>

This template has three problems:
1. Devices and SOP are in `identifierEntities` — they are not external identifiers.
2. `mainActivity.used` has three entries — two of them point at devices, not at backward connectors.
3. `forwardConnectors` have no `derivedFrom` — the lineage from output back to input is missing.

The toolbox produces this output **without any error**:

<details>
<summary><b>Click to expand — Wrong output PROV-JSON</b></summary>

```json
{
  "bundle": {
    "gen:bundle_dna_extraction_TS4420": {
      "entity": {
        "gen:thermal_cycler_T100": {
          "cpm:externalIdType": ["pbm:ProcessingDevice"],
          "cpm:externalId": ["Thermal_Cycler_T100"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:id"}]
        },
        "gen:microcentrifuge_Z200": {
          "cpm:externalIdType": ["pbm:ProcessingDevice"],
          "cpm:externalId": ["Microcentrifuge_Z200"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:id"}]
        },
        "gen:SOP_DX_12": {
          "cpm:externalIdType": ["pbm:SOP"],
          "cpm:externalId": ["SOP_DX_12"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:id"}]
        },
        "gen:tissue_sample_TS4420": { "..." : "..." },
        "gen:dna_aliquot_A1": { "..." : "..." },
        "gen:dna_aliquot_A2": { "..." : "..." }
      },
      "activity": {
        "gen:dnaExtraction_TS4420": {
          "dct:hasPart": [
            {"type": "prov:QUALIFIED_NAME", "$": "gen:SOP_DX_12"},
            {"type": "prov:QUALIFIED_NAME", "$": "gen:thermal_cycler_T100"},
            {"type": "prov:QUALIFIED_NAME", "$": "gen:microcentrifuge_Z200"}
          ],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:mainActivity"}]
        }
      },
      "used": {
        "gen:use_thermal_cycler": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:thermal_cycler_T100"
        },
        "gen:use_microcentrifuge": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:microcentrifuge_Z200"
        },
        "gen:use_tissue_TS4420": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:tissue_sample_TS4420"
        }
      },
      "wasGeneratedBy": {
        "_:n0": { "prov:entity": "gen:dna_aliquot_A1", "prov:activity": "gen:dnaExtraction_TS4420" },
        "_:n1": { "prov:entity": "gen:dna_aliquot_A2", "prov:activity": "gen:dnaExtraction_TS4420" }
      }
    }
  }
}
```

</details>

Four things went wrong in this output:

**1. Devices and SOP are typed `cpm:id` (identifier entity).** They appear as entities with `"prov:type": "cpm:id"` — the type reserved for external identifiers like accession numbers. The toolbox mapped them from `identifierEntities` exactly as instructed. Downstream tools that process `cpm:id` entities will treat these as identifier references, not as physical equipment.

**2. `dct:hasPart` lists devices and a protocol.** `hasPart` is meant to reference sub-activities (e.g. a centrifugation step, a lysis step). Devices and protocols are not activities.

**3. Three `used` relations instead of one.** The activity appears to "use" the microcentrifuge and thermal cycler as if they were backward connectors. Only the tissue sample is a real backward connector — the other two `used` entries are structurally meaningless in the traversal backbone.

**4. No `wasDerivedFrom`.** The forward connectors had no `derivedFrom` in the template, so the output contains no `wasDerivedFrom` relations. The DNA aliquots have no recorded lineage back to the tissue sample. This breaks backward traversal — a tool following the chain will find the aliquots but cannot determine where they came from.

### Side-by-side comparison

| | Wrong template | Correct template |
|---|---|---|
| `identifierEntities` | 3 entries (devices + SOP) | empty / absent |
| `mainActivity.used` | 3 entries (sample + 2 devices) | 1 entry (sample only) |
| `mainActivity.hasPart` | lists devices + SOP | absent (or lists sub-activity IDs only) |
| `forwardConnectors[].derivedFrom` | missing | `["gen:tissue_sample_TS4420"]` |
| Output entities | 6 (3 connectors + 3 `cpm:id`) | 3 (connectors only) |
| Output `used` | 3 relations | 1 relation |
| Output `wasDerivedFrom` | **missing** | 2 relations |
| Output `dct:hasPart` | lists devices | absent |

The correct template from the realistic example above produces a clean backbone with only connectors, one `used`, two `wasDerivedFrom`, and two `wasGeneratedBy`. Devices, the SOP, and real sample entities are added in Step 2.

---

## Adding the Domain-Specific Part (Step 2)

Step 1 produces a traversal backbone — connectors, main activity, and the relations between them. The backbone alone is not a complete CPM bundle. Step 2 adds the actual science: real sample entities, equipment, protocols, and the detailed relations between them, to the same bundle.

This step uses the ProvToolBox API directly. The CPF Toolbox is not involved.

### What to add

| Category | Examples | ProvToolBox method |
|---|---|---|
| Domain entities | Samples, devices, protocols | `pF.newEntity(id, attributes)` |
| Domain relations | Activity used device; activity produced sample; output derived from input | `pF.newUsed()`, `pF.newWasGeneratedBy()`, `pF.newWasDerivedFrom()` |
| `specializationOf` cross-links | Domain sample entity → corresponding connector | `pF.newSpecializationOf(domainEntity, connector)` |

Domain relations connect domain entities to each other and to the main activity. They parallel the traversal-level relations (which connect connectors), but describe what actually happened — which device was used, which sample was produced, which output came from which input.

`specializationOf` is the bridge between the two parts. Each domain entity that corresponds to a connector needs a `specializationOf` relation linking it to that connector. Without these links, the domain-specific part is present in the bundle but disconnected from the traversal backbone.

### Java example

Continuing with the DNA extraction scenario from the realistic example above. After Step 1 produces the traversal backbone, the code below adds five domain entities (tissue sample, two DNA aliquots, two devices), their relations, and the `specializationOf` cross-links.

<details>
<summary><b>Click to expand — Full Java code for Step 2</b></summary>

```java
import org.openprovenance.prov.model.*;
import org.openprovenance.prov.vanilla.ProvFactory;
import org.openprovenance.prov.interop.InteropFramework;
import cz.muni.fi.cpm.template.deserialization.TraversalInformationDeserializer;
import java.util.Arrays;

// Step 1: generate traversal backbone
InputStream in = new FileInputStream("template_dna_extraction.json");
Document doc = new TraversalInformationDeserializer().deserializeDocument(in);

// Step 2: add domain-specific part
Bundle bundle = (Bundle) doc.getStatementOrBundle().get(0);
ProvFactory pF = new ProvFactory();

String GEN = "https://biobank-gen.example.org/";
String PBM = "https://w3id.org/2023/prov-biobank-model/";
String PROV = "http://www.w3.org/ns/prov#";

// --- Domain entities ---

Entity tissue = pF.newEntity(
        pF.newQualifiedName(GEN, "tissue_TS4420", "gen"),
        Arrays.asList(
                pF.newType(pF.newQualifiedName(PBM, "Sample", "pbm"),
                        pF.getName().PROV_QUALIFIED_NAME),
                pF.newOther(pF.newQualifiedName(PROV, "label", "prov"),
                        "Tissue sample TS4420", pF.getName().XSD_STRING)));

Entity dna1 = pF.newEntity(
        pF.newQualifiedName(GEN, "dna_sample_A1", "gen"),
        Arrays.asList(
                pF.newType(pF.newQualifiedName(PBM, "Sample", "pbm"),
                        pF.getName().PROV_QUALIFIED_NAME),
                pF.newOther(pF.newQualifiedName(PROV, "label", "prov"),
                        "DNA aliquot A1, 200 uL", pF.getName().XSD_STRING)));

Entity dna2 = pF.newEntity(
        pF.newQualifiedName(GEN, "dna_sample_A2", "gen"),
        Arrays.asList(
                pF.newType(pF.newQualifiedName(PBM, "Sample", "pbm"),
                        pF.getName().PROV_QUALIFIED_NAME),
                pF.newOther(pF.newQualifiedName(PROV, "label", "prov"),
                        "DNA aliquot A2, 200 uL", pF.getName().XSD_STRING)));

Entity microcentrifuge = pF.newEntity(
        pF.newQualifiedName(GEN, "microcentrifuge_Z200", "gen"),
        Arrays.asList(
                pF.newType(pF.newQualifiedName(PBM, "ProcessingDevice", "pbm"),
                        pF.getName().PROV_QUALIFIED_NAME),
                pF.newOther(pF.newQualifiedName(PROV, "label", "prov"),
                        "Microcentrifuge Z200", pF.getName().XSD_STRING)));

Entity thermalCycler = pF.newEntity(
        pF.newQualifiedName(GEN, "thermal_cycler_T100", "gen"),
        Arrays.asList(
                pF.newType(pF.newQualifiedName(PBM, "ProcessingDevice", "pbm"),
                        pF.getName().PROV_QUALIFIED_NAME),
                pF.newOther(pF.newQualifiedName(PROV, "label", "prov"),
                        "Thermal Cycler T100", pF.getName().XSD_STRING)));

// --- Domain relations ---

QualifiedName activityId = pF.newQualifiedName(GEN, "dnaExtraction_TS4420", "gen");

Used usedMicro     = pF.newUsed(activityId, microcentrifuge.getId());
Used usedThermal   = pF.newUsed(activityId, thermalCycler.getId());
Used usedTissue    = pF.newUsed(activityId, tissue.getId());

WasGeneratedBy genDna1 = pF.newWasGeneratedBy(null, dna1.getId(), activityId);
WasGeneratedBy genDna2 = pF.newWasGeneratedBy(null, dna2.getId(), activityId);

WasDerivedFrom derivDna1 = pF.newWasDerivedFrom(dna1.getId(), tissue.getId());
WasDerivedFrom derivDna2 = pF.newWasDerivedFrom(dna2.getId(), tissue.getId());

// --- specializationOf cross-links ---

QualifiedName bcId  = pF.newQualifiedName(GEN, "tissue_sample_TS4420", "gen");
QualifiedName fc1Id = pF.newQualifiedName(GEN, "dna_aliquot_A1", "gen");
QualifiedName fc2Id = pF.newQualifiedName(GEN, "dna_aliquot_A2", "gen");

SpecializationOf specTissue = pF.newSpecializationOf(tissue.getId(), bcId);
SpecializationOf specDna1   = pF.newSpecializationOf(dna1.getId(), fc1Id);
SpecializationOf specDna2   = pF.newSpecializationOf(dna2.getId(), fc2Id);

// --- Add everything to the bundle ---

bundle.getStatement().addAll(Arrays.asList(
        tissue, dna1, dna2, microcentrifuge, thermalCycler,
        usedMicro, usedThermal, usedTissue,
        genDna1, genDna2,
        derivDna1, derivDna2,
        specTissue, specDna1, specDna2));

// Step 3: serialize
new InteropFramework(pF).writeDocument("output_complete.json", doc);
```

</details>

The main activity (`gen:dnaExtraction_TS4420`) is not redeclared — it already exists in the bundle from Step 1. The domain relations reference it by its qualified name.

### The `specializationOf` cross-links

Each `specializationOf` says: "this domain entity is a more specific version of that traversal connector." The connector carries the cross-bundle navigation role; the domain entity carries the real-world detail.

| Domain entity (specific) | Connector (general) | Meaning |
|---|---|---|
| `gen:tissue_TS4420` (`pbm:Sample`) | `gen:tissue_sample_TS4420` (`cpm:backwardConnector`) | The tissue sample is the real entity behind the backward connector |
| `gen:dna_sample_A1` (`pbm:Sample`) | `gen:dna_aliquot_A1` (`cpm:forwardConnector`) | DNA aliquot A1 is the real entity behind forward connector A1 |
| `gen:dna_sample_A2` (`pbm:Sample`) | `gen:dna_aliquot_A2` (`cpm:forwardConnector`) | DNA aliquot A2 is the real entity behind forward connector A2 |

In the ProvToolBox API, `pF.newSpecializationOf(specificEntity, generalEntity)` — the first argument is the domain entity, the second is the connector.

Devices (`gen:microcentrifuge_Z200`, `gen:thermal_cycler_T100`) do not get `specializationOf` relations. They are not counterparts of any connector — they are standalone domain entities linked to the activity through `used`.

### Complete output

After adding the domain-specific part to the DNA extraction backbone, the serialized bundle contains both parts:

<details>
<summary><b>Click to expand — Complete output PROV-JSON (traversal + domain)</b></summary>

```json
{
  "prefix": {
    "cpm": "https://www.commonprovenancemodel.org/cpm-namespace-v1-0/",
    "gen": "https://biobank-gen.example.org/",
    "dct": "http://purl.org/dc/terms/",
    "xsd": "http://www.w3.org/2001/XMLSchema#",
    "pbm": "https://w3id.org/2023/prov-biobank-model/",
    "prov": "http://www.w3.org/ns/prov#"
  },
  "bundle": {
    "gen:bundle_dna_extraction_TS4420": {
      "entity": {
        "gen:tissue_TS4420": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "pbm:Sample"}],
          "prov:label": ["Tissue sample TS4420"]
        },
        "gen:thermal_cycler_T100": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "pbm:ProcessingDevice"}],
          "prov:label": ["Thermal Cycler T100"]
        },
        "gen:microcentrifuge_Z200": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "pbm:ProcessingDevice"}],
          "prov:label": ["Microcentrifuge Z200"]
        },
        "gen:tissue_sample_TS4420": {
          "cpm:externalId": ["TS4420"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:backwardConnector"}]
        },
        "gen:dna_aliquot_A1": {
          "cpm:externalId": ["A1"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:forwardConnector"}]
        },
        "gen:dna_sample_A1": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "pbm:Sample"}],
          "prov:label": ["DNA aliquot A1, 200 uL"]
        },
        "gen:dna_aliquot_A2": {
          "cpm:externalId": ["A2"],
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:forwardConnector"}]
        },
        "gen:dna_sample_A2": {
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "pbm:Sample"}],
          "prov:label": ["DNA aliquot A2, 200 uL"]
        }
      },
      "activity": {
        "gen:dnaExtraction_TS4420": {
          "prov:startTime": "2025-03-10T09:30:00.000Z",
          "prov:type": [{"type": "prov:QUALIFIED_NAME", "$": "cpm:mainActivity"}]
        }
      },
      "used": {
        "gen:use_tissue_TS4420": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:tissue_sample_TS4420"
        },
        "_:u1": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:microcentrifuge_Z200"
        },
        "_:u2": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:thermal_cycler_T100"
        },
        "_:u3": {
          "prov:activity": "gen:dnaExtraction_TS4420",
          "prov:entity": "gen:tissue_TS4420"
        }
      },
      "specializationOf": {
        "_:s1": {
          "prov:specificEntity": "gen:tissue_TS4420",
          "prov:generalEntity": "gen:tissue_sample_TS4420"
        },
        "_:s2": {
          "prov:specificEntity": "gen:dna_sample_A1",
          "prov:generalEntity": "gen:dna_aliquot_A1"
        },
        "_:s3": {
          "prov:specificEntity": "gen:dna_sample_A2",
          "prov:generalEntity": "gen:dna_aliquot_A2"
        }
      },
      "wasDerivedFrom": {
        "_:d1": {
          "prov:generatedEntity": "gen:dna_aliquot_A1",
          "prov:usedEntity": "gen:tissue_sample_TS4420"
        },
        "_:d2": {
          "prov:generatedEntity": "gen:dna_aliquot_A2",
          "prov:usedEntity": "gen:tissue_sample_TS4420"
        },
        "_:d3": {
          "prov:generatedEntity": "gen:dna_sample_A1",
          "prov:usedEntity": "gen:tissue_TS4420"
        },
        "_:d4": {
          "prov:generatedEntity": "gen:dna_sample_A2",
          "prov:usedEntity": "gen:tissue_TS4420"
        }
      },
      "wasGeneratedBy": {
        "_:g1": {
          "prov:entity": "gen:dna_aliquot_A1",
          "prov:activity": "gen:dnaExtraction_TS4420"
        },
        "_:g2": {
          "prov:entity": "gen:dna_aliquot_A2",
          "prov:activity": "gen:dnaExtraction_TS4420"
        },
        "_:g3": {
          "prov:entity": "gen:dna_sample_A1",
          "prov:activity": "gen:dnaExtraction_TS4420"
        },
        "_:g4": {
          "prov:entity": "gen:dna_sample_A2",
          "prov:activity": "gen:dnaExtraction_TS4420"
        }
      }
    }
  }
}
```

</details>

The bundle now contains both parts:

- **3 traversal entities** (from Step 1): `gen:tissue_sample_TS4420` (backward connector), `gen:dna_aliquot_A1` and `gen:dna_aliquot_A2` (forward connectors)
- **5 domain entities** (from Step 2): `gen:tissue_TS4420` and two `gen:dna_sample_*` (samples), `gen:microcentrifuge_Z200` and `gen:thermal_cycler_T100` (devices)
- **3 `specializationOf`** cross-links connecting each domain sample to its traversal connector
- Separate `used`, `wasGeneratedBy`, and `wasDerivedFrom` relations at both levels — the traversal relations (with named IDs like `gen:use_tissue_TS4420`) connect connectors, the domain relations (with blank-node IDs) connect domain entities

---

## FAQ and Common Errors

The CPF Toolbox generates traversal information only. It does not process, validate, or produce domain-specific content. This is by design.

### Errors

These exceptions stop execution with an error message.

| Error message | Cause | Fix |
|---|---|---|
| `Bundle name cannot be null` | JSON template is missing `bundleName` | Add `"bundleName": "prefix:bundleName"` to the template |
| `Cpm Document must consist of exactly one statement of type PROV_BUNDLE` | Document contains zero or more than one bundle | Use exactly one bundle per Document |
| `JsonParseException` / `MismatchedInputException` | Malformed JSON, wrong value types, or invalid date format | Validate JSON syntax; use ISO-8601 dates (`"2025-03-10T09:30:00.000Z"`); check every id follows `prefix:localName` |
| `Document cannot be null` | `toProvDocument()` returned null because the input template was null | Check that deserialization succeeded before passing the Document downstream |

### Silent issues

These produce output without any error, but the output is structurally wrong. The toolbox does not validate CPM structural rules — it accepts any syntactically valid JSON and maps it to PROV.

| What you did | What happens |
|---|---|
| Put devices/SOPs in `identifierEntities` | They appear as `cpm:id` entities — the type reserved for external identifiers, not equipment |
| Forgot `derivedFrom` on forward connectors | No `wasDerivedFrom` in output — the lineage from output back to input is lost |
| Put a non-connector id in `backwardConnectorId` | A `used` relation is emitted pointing at whatever you referenced, regardless of what it is |
| Omitted connectors, used, or generated entirely | Toolbox accepts it — the output is valid PROV-JSON but not a valid CPM component |

### Common questions

**Q: How do I add devices, protocols, or real sample entities using the toolbox?**
The CPF Toolbox generates traversal information only. Domain-specific content is not supported by design.

**Q: My devices ended up typed as `cpm:id` — why?**
You put them in `identifierEntities`. That field is reserved for external identifiers (accession numbers, sample IDs). The toolbox mapped them exactly as instructed.

**Q: My identifiers contain backslash escaping like `blf:SOP\:Process`.**
The local name part of an identifier must not contain colons, spaces, parentheses, or special characters. Use clean tokens (`gen:SOP_PE_35S`) and put descriptions in `externalId` or `prov:label`.

**Q: Forward connectors have no `wasDerivedFrom` in the output.**
You did not add `"derivedFrom": ["prefix:backwardConnectorId"]` to the forward connectors in the template. Without it, the toolbox has no information to generate `wasDerivedFrom` relations.

**Q: Python's prov library cannot parse the PROV-JSON output.**
ProvToolBox adds an `"@id"` field inside bundles that is not part of the PROV-JSON specification. Remove it from the output JSON before passing it to other tools. Known issue: [ProvToolBox #222](https://github.com/lucmoreau/ProvToolbox/issues/222).

**Q: I tried to add an agent and only `senderAgents` / `receiverAgents` work.**
The template supports sender and receiver agents only. Other agent roles are not implemented in the toolbox.

**Q: My sender and receiver agents have the same identifier — why do I get two separate agents?**
By default, sender and receiver agents with the same ID are kept as distinct entries. To merge them into a single agent with both roles, pass `true` to the mapper constructor:
```java
ITemplateProvMapper mapper = new TemplateProvMapper(new CpmProvFactory(pF), true);
```

**Q: `getMainActivity()` returns null.**
The bundle does not contain an activity with `prov:type = cpm:mainActivity`. The toolbox does not validate CPM structure — it returns null instead of throwing an error.
