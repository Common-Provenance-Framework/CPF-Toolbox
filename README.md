# CPF Toolbox

The reference implementation of the Common Provenance Model (CPM) from the ISO 23494 standard. Originally developed as part of a master's thesis ([available here](https://is.muni.cz/auth/th/sv0z0/)), it reflects the model's state as of Spring 2025 ([reference](https://zenodo.org/records/14526108)) and is implemented as an extension of the [ProvToolbox](https://github.com/lucmoreau/ProvToolbox) library.

[![Pipeline](https://github.com/Common-Provenance-Framework/CPF-Toolbox/actions/workflows/maven.yml/badge.svg)](https://github.com/Common-Provenance-Framework/CPF-Toolbox/actions/workflows/maven.yml)
![Coverage](https://img.shields.io/badge/coverage-88%25-brightgreen)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

## Prerequisites

### Requirements

| | Minimum version |
|---|---|
| Java | 23 |
| Maven | 3.9 |

### Installation

The CPF Toolbox is not published to Maven Central. Build it from source to install it into your local Maven repository:

```sh
git clone https://github.com/Common-Provenance-Framework/CPF-Toolbox.git
cd CPF-Toolbox
mvn install -DskipTests
```

This installs `cpm-core` and `cpm-template` version **2.2.0**.

After installation, add these dependencies to your `pom.xml`:

<blockquote>

<details>
<summary><b>📦 Maven dependencies (pom.xml)</b></summary>

```xml
<!-- CPF Toolbox (installed locally by `mvn install`) -->
<dependency>
    <groupId>cz.muni.fi.cpm</groupId>
    <artifactId>cpm-core</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>cz.muni.fi.cpm</groupId>
    <artifactId>cpm-template</artifactId>
    <version>2.2.0</version>
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

</blockquote>

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

The CPF Toolbox generates the **traversal information** part of a a finalized provenance component (FPC), which is technically realised as a PROV bundle with a restricted content. Traversal information is the standardized backbone that links FPCs across organizations — it contains the main activity, backward and forward connectors, and sender/receiver agents. 

The toolbox does **not** generate the domain-specific part. Domain-specific provenance covering the details of the main activity is added separately after the traversal information is built. 

An FPC consists of the both parts, connected by `specializationOf` relations:

```
┌─────────────────────────────────────────────────────┐
│                    CPM Bundle                       │
│                                                     │
│  ┌──────────────────────┐  ┌─────────────────────┐  │
│  │ Traversal Information│  │ Domain-Specific Part │  │
│  │                      │  │                      │  │
│  │  Main Activity       │  │  Domain entity       │  │
│  │  Backward Connector ◄├──┤► Domain entity       │  │
│  │  Forward Connector  ◄├──┤► Domain entity       │  │
│  │  Sender/Receiver Agt │  │  Devices, SW, SOPs   │  │
│  │                      │  │  Sub-activities      │  │
│  └──────────────────────┘  └─────────────────────┘  │
│           ▲                        ▲                │
│           └── specializationOf ────┘                │
└─────────────────────────────────────────────────────┘
```

The arrows between the two parts represent `specializationOf` relations — they attach a domain entity to its corresponding connector in the traversal information.

---

## Workflow

Building an FPC is a three-step process. Steps 2 and 3 use the ProvToolBox API directly — the CPF Toolbox is only involved in Step 1.

**Step 1 — Generate the traversal information.** Write a JSON template describing the main activity, connectors, and agents. Feed it to `TraversalInformationDeserializer`, which produces a ProvToolBox `Document` containing a PROV bundle with the traversal information.

**Step 2 — Add the domain-specific part.** Retrieve the bundle from the Document and add domain entities (e.g., samples, devices, protocols), domain activities (e.g., sub-steps of the main activity), and domain relations (e.g., `used`, `wasGeneratedBy`, `wasDerivedFrom`) using the standard ProvToolBox. Add `specializationOf` relations to connect relevant domain entities to the corresponding connectors.

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

---

## Building the Input JSON Template

The input template is a JSON file that describes the traversal information for one bundle. It is **not** a PROV-JSON file — it is a separate format consumed by `TraversalInformationDeserializer`. 

### Field reference

Every field the template accepts, grouped by the object it belongs to. Anything not
listed here is rejected — see [Unknown fields](#unknown-fields) below.

Where a field maps to an attribute in the output, the attribute is shown as `→ cpm:…`.

#### Top level

| Field | Required | Description |
|---|---|---|
| `prefixes` | yes | Namespace declarations. Every prefix used in an identifier must be declared here. |
| `bundleName` | yes | Qualified name of the output bundle. |
| `mainActivity` | yes | The single core process of this bundle. |
| `backwardConnectors` | no | Entities this bundle consumed from an upstream bundle. |
| `forwardConnectors` | no | Entities this bundle passes to a downstream bundle. |
| `specForwardConnectors` | no | Forward connectors that also record *where* the data went. See below. |
| `senderAgents` | no | Agents that sent the incoming data. |
| `receiverAgents` | no | Agents that received the outgoing data. |
| `identifierEntities` | no | External identifiers — accession numbers, sample IDs. Not equipment. |

#### `mainActivity`

| Field | Required | Description |
|---|---|---|
| `id` | yes | Qualified name of the activity. |
| `startTime` | no | ISO-8601, e.g. `"2025-03-10T09:30:00.000Z"`. |
| `endTime` | no | ISO-8601. |
| `referencedMetaBundleId` | no | Meta-bundle describing this bundle. → `cpm:referencedMetaBundleId` |
| `hasPart` | no | Qualified names of **sub-activities**, declared in the domain-specific part. Not devices or protocols. → `dct:hasPart` |
| `used` | no | Array of `{ "id": <optional>, "backwardConnectorId": <required> }`. → `used` |
| `generated` | no | Qualified names of forward connectors this activity produces. → `wasGeneratedBy` |

#### `backwardConnectors[]`

| Field | Required | Description |
|---|---|---|
| `id` | yes | Qualified name of the connector. |
| `externalId` | no | → `cpm:externalId` |
| `derivedFrom` | no | Array of qualified names this connector derives from. → `wasDerivedFrom` |
| `referencedBundleId` | no | The upstream bundle. → `cpm:referencedBundleId` |
| `referencedMetaBundleId` | no | The upstream meta-bundle. → `cpm:referencedMetaBundleId` |
| `referencedBundleSpecV` | no | Content version of the referenced bundle. → `cpm:referencedBundleSpecV` |
| `referencedMetaBundleSpecV` | no | Content version of the referenced meta-bundle. → `cpm:referencedMetaBundleSpecV` |
| `hashAlg` | no | One of `MD5`, `SHA1`, `SHA256`, `SHA512`. → `cpm:hashAlg` |
| `referencedBundleHashValue` | no | Hash of the referenced bundle. Pass a plain string — a JSON object is not converted correctly. |
| `attributedTo` | no | `{ "id": <optional>, "agentId": <required> }`. → `wasAttributedTo` |

#### `forwardConnectors[]`

| Field | Required | Description |
|---|---|---|
| `id` | yes | Qualified name of the connector. |
| `externalId` | no | → `cpm:externalId` |
| `derivedFrom` | no | Backward connectors this one derives from. → `wasDerivedFrom` |

Forward connectors accept **nothing else**. There is no `attributedTo` and no
`referencedBundleId` here — for those, use a spec-forward connector.

#### `specForwardConnectors[]`

A plain forward connector says "this bundle produced something and passed it on". A spec-forward connector also records *where it went* — use one when the receiving bundle already exists and you want the chain verifiable in both directions. Its `specializationOf` field points at the plain forward connector it refines, so both can live in the same bundle.

Everything `forwardConnectors` accepts, plus:

| Field | Required | Description |
|---|---|---|
| `specializationOf` | no | The forward connector this one refines. → `specializationOf` |
| `referencedBundleId` | no | The downstream bundle. → `cpm:referencedBundleId` |
| `referencedMetaBundleId` | no | → `cpm:referencedMetaBundleId` |
| `referencedBundleSpecV` | no | → `cpm:referencedBundleSpecV` |
| `referencedMetaBundleSpecV` | no | → `cpm:referencedMetaBundleSpecV` |
| `provenanceServiceUri` | no | Provenance service holding the referenced bundle. → `cpm:provenanceServiceUri` |
| `hashAlg` | no | `MD5`, `SHA1`, `SHA256` or `SHA512`. → `cpm:hashAlg` |
| `referencedBundleHashValue` | no | Hash of the referenced bundle. Pass a plain string — a JSON object is not converted correctly. |
| `attributedTo` | no | `{ "id": <optional>, "agentId": <required> }`. → `wasAttributedTo` |

Entities get `prov:type = cpm:specForwardConnector`.

#### `senderAgents[]` / `receiverAgents[]`

| Field | Required | Description |
|---|---|---|
| `id` | yes | Qualified name of the agent. |
| `contactIdPid` | no | Persistent identifier for the contact point. → `cpm:contactIdPid` |

#### `identifierEntities[]`

| Field | Required | Description |
|---|---|---|
| `id` | yes | Qualified name of the entity. |
| `externalId` | no | The identifier value. → `cpm:externalId` |
| `externalIdType` | no | What kind of identifier it is. → `cpm:externalIdType` |
| `comment` | no | Free text. → `cpm:comment` |

<a id="unknown-fields"></a>

#### Unknown fields are considered as an error

A misspelled or unsupported key anywhere in the template aborts deserialization
with `UnrecognizedPropertyException`, and no output is produced. The message names
the offending field and lists what is accepted at that position.

This cuts both ways: the parser is strict, so if it succeeds, every key you wrote
was understood.

### Identifier rules

Every identifier in the template must follow the `prefix:localName` format, where `prefix` is a key in `prefixes`.

The local name part (after the colon) must contain only letters, digits, hyphens, and underscores. Colons, spaces, parentheses, and accented characters in local names cause escaping issues in the output and break interoperability with other PROV tools.

Put human-readable descriptions in `cpm:comment` or `prov:label` attributes — not in the identifier itself.

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

<blockquote>

<details>
<summary><b>📄 Output PROV-JSON</b></summary>

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

</blockquote>

The toolbox automatically:
- Assigns CPM types (`cpm:mainActivity`, `cpm:backwardConnector`, `cpm:forwardConnector`)
- Generates `wasGeneratedBy` from `mainActivity.generated`
- Generates `wasDerivedFrom` from `forwardConnectors[].derivedFrom`
- Generates `used` from `mainActivity.used`
- Adds `cpm`, `dct`, `xsd`, `prov` namespace prefixes

### Realistic example — biobank processing

A biobank receives a tissue sample and extracts two DNA aliquots. The template describes only the traversal backbone — the equipment, protocol, and actual sample entities are added in Step 2.

**Input** (`template_dna_extraction.json`):

<blockquote>

<details>
<summary><b>📥 Input JSON template</b></summary>

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

</blockquote>

**Output** (PROV-JSON):

<blockquote>

<details>
<summary><b>📄 Output PROV-JSON</b></summary>

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

</blockquote>

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

## What SHOULD Not Be in the Input

The traversal information template is used to generate the **traversal information only**. Domain-specific content — the actual details about the process — is added in Step 2 using the ProvToolBox.

### Common mistakes

| What people put in the template | Why it's wrong | Where it actually belongs |
|---|---|---|
| Devices / equipment / SW | Not traversal information | Step 2: add as entities/agents, link with `used/wasAssociatedWith` |
| SOPs / protocols | Not traversal information | Step 2: add as entities |
| Sub-activities (extraction steps, centrifugation) | Partly correct in `hasPart`, but the activities themselves go in Step 2 | `hasPart` lists their IDs; the activity declarations go in Step 2 |
| Technicians / operators | Not traversal information | Step 2: add as agents with `wasAssociatedWith` |
| Devices as agents | Template only allows sender/receiver agents | Step 2: add as entities with `used`, or as agents via ProvToolBox |

### What goes wrong

**Example — devices and SOP placed in the template (wrong):**

<blockquote>

<details>
<summary><b>❌ Wrong input JSON (devices in template)</b></summary>

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

</blockquote>

This template has three problems:
1. Devices and SOP are in `identifierEntities` — they are not external identifiers.
2. `mainActivity.used` has three entries — two of them point at devices, not at backward connectors.
3. `forwardConnectors` have no `derivedFrom` — the lineage from output back to input is missing.

The toolbox produces this output **without any error**:

<blockquote>

<details>
<summary><b>❌ Wrong output PROV-JSON</b></summary>

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

</blockquote>

Four things went wrong in this output:

**1. Devices and SOP are typed `cpm:id` (identifier entity).** They appear as entities with `"prov:type": "cpm:id"` — the type reserved for external identifiers like accession numbers. The toolbox mapped them from `identifierEntities` exactly as instructed. Downstream tools that process `cpm:id` entities will treat these as identifier references, not as physical equipment.

**2. `dct:hasPart` lists devices and a protocol.** `hasPart` is meant to reference sub-activities of the main activity (e.g. a centrifugation step or a data preparation step). Devices and protocols are not activities.

**3. Three `used` relations instead of one.** The activity appears to "use" the microcentrifuge and thermal cycler as if they were backward connectors. Only the tissue sample is a proper backward connector in this use case — the other two `used` entries are structurally meaningless in the traversal backbone.

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

Step 1 produces a traversal information — connectors, main activity, agents, and the relations between them. Step 2 adds the details of the main activity and how the outputs of the activity were produced.

This step uses the ProvToolBox directly. The CPF Toolbox is not used.

### What to add

| Category | Examples | ProvToolBox method |
|---|---|---|
| Domain entities | Samples, files, datasets, protocols, configuration files | `pF.newEntity(id, attributes)` |
| Domain relations | Activity used a file; activity produced sample; output derived from input | `pF.newUsed()`, `pF.newWasGeneratedBy()`, `pF.newWasDerivedFrom()` |
| `specializationOf` cross-links | Domain entity → corresponding connector | `pF.newSpecializationOf(domainEntity, connector)` |

Relations present in the domain-specific part describe the main activity in the traversal information with finer granularity, for instance, which device and sample was used, which file or data was produced, which output came from which input.

`specializationOf` is the bridge between the two parts. Each entity present in a domain-specific part that corresponds to a connector needs a `specializationOf` relation linking it to that connector. Without these links, the domain-specific part is present in the FPC but disconnected from the traversal information.

### Java example

After Step 1 produces the traversal backbone, the code below adds five domain entities (tissue sample, two DNA aliquots, two devices), their relations, and the `specializationOf` cross-links.

<blockquote>

<details>
<summary><b>🔧 Full Java code for Step 2</b></summary>

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

</blockquote>

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

<blockquote>

<details>
<summary><b>📄 Complete output PROV-JSON (traversal + domain)</b></summary>

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

</blockquote>

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

---

## Going further

- [docs/cpm-core-api.md](docs/cpm-core-api.md) — read, query and manipulate CPM bundles as graphs with `CpmDocument`
- [docs/java-api.md](docs/java-api.md) — build traversal information in Java instead of from JSON
- [docs/datasets.md](docs/datasets.md) — the MMCI and EMBRC example datasets
- [docs/contributing.md](docs/contributing.md) — setup, commit conventions, pull requests

## License

Apache 2.0. See [LICENSE](LICENSE).
