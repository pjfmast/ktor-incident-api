# Tips voor het gebruik van PlantUML en Mermaid

Dit document biedt praktische tips, uitleg en best practices voor het gebruik van **PlantUML** en **Mermaid** binnen softwareontwerpdocumentatie en Markdown-bestanden.

---

## 1. Kan PlantUML Markdown genereren?

> **Kort antwoord:** Nee, PlantUML genereert zelf geen Markdown (`.md`) bestanden. PlantUML is een diagram-rendering engine die platte tekstuele diagramspecificaties (`.puml`) omzet naar grafische bestandsformaten zoals **PNG**, **SVG**, **PDF** of ASCII-art.

Markdown is een opmaaktaal voor tekstdocumenten, terwijl PlantUML zich richt op de visuele representatie van modellen en diagrammen. Wel kunnen PlantUML en Markdown op verschillende manieren effectief met elkaar gecombineerd worden.

---

## 2. PlantUML integreren met Markdown

Er zijn drie gangbare methoden om PlantUML binnen een project en bijbehorende documentatie te gebruiken:

### Methode A: Standalone bronbestanden (`.puml`) met compilatie naar afbeeldingen
* **Werkwijze:** Bewaar diagramdefinities in een aparte map, bijvoorbeeld `docs/puml/mijn-diagram.puml`.
* **Rendertooling:** Compileer het diagram met behulp van de PlantUML CLI, een pre-commit hook of een CI/CD-pipeline (zoals een GitHub Action of GitLab CI) naar een `.svg` of `.png`.
* **Insluiten in Markdown:**
  ```markdown
  ![Architectuur Diagram](images/mijn-diagram.svg)
  [Bekijk PUML-broncode](puml/mijn-diagram.puml)
  ```
* **Voordelen:**
  - Schone scheiding tussen broncode en gegenereerde artefacten.
  - SVG levert haarscherpe afbeeldingen op elk schermformaat.
  - Direct te bekijken op elk platform (GitHub, GitLab, documentatiewebsites).

### Methode B: Ingesloten PlantUML codeblokken (` ```plantuml `)
* **Werkwijze:** Plaats de PlantUML-code direct in het `.md` bestand:
  ````markdown
  ```plantuml
  @startuml
  Alice -> Bob: Authentication Request
  Bob --> Alice: Authentication Response
  @enduml
  ```
  ````
* **Wanneer gebruiken:**
  - Uitstekend bruikbaar in tools met native PlantUML-ondersteuning, zoals **GitLab**, **Azure DevOps** en IDE-plugins (bijvoorbeeld in IntelliJ IDEA met de *PlantUML Integration* plugin).
* **Aandachtspunt:** GitHub ondersteunt standaard geen PlantUML-rendering in Markdown previews; daar wordt het blok getoond als platte broncode.

---

## 3. PlantUML vs. Mermaid: Wanneer kies je wat?

| Eigenschap | PlantUML (`.puml`) | Mermaid (` ```mermaid `) |
| :--- | :--- | :--- |
| **Native GitHub Markdown rendering** | ❌ Nee (toont platte code) | ✅ Ja (direct visueel gerenderd in GitHub/GitLab web UI) |
| **Volledigheid UML-standaard** | Zeer uitgebreid (Use Cases, Componenten, Deployment, State, etc.) | Basis/gemiddeld (Flowcharts, Sequence, Class, State, ERD) |
| **IDE integratie** | Uitstekende plugins voor IntelliJ IDEA, VS Code | Brede ondersteuning in moderne Markdown viewers |
| **Styling & Theming** | Veel vrijheid (`skinparam`, CSS-achtige stijlen) | Styling via thema's en klassendefinities in Mermaid |
| **Opslag & Onderhoud** | Aanbevolen als los `.puml` bestand | Direct in `.md` bestand |

---

## 4. Best Practices voor Diagrammen in Ktor/Kotlin Projecten

1. **Gebruik Mermaid voor snelle documentatie op GitHub:**
   - Als documentatie direct leesbaar moet zijn in webportalen zonder extra build-stappen, heeft een ` ```mermaid ` codeblok de voorkeur.
2. **Gebruik PlantUML voor gedetailleerde, formele UML-diagrammen:**
   - Complexe use-case diagrammen of gedetailleerde sequence diagrammen (zoals authenticatie- en autorisatieflows) laten zich in PlantUML vaak overzichtelijker en formeler modelleren.
3. **Beheer PUML-bestanden in versiebeheer:**
   - Plaats bronbestanden consistent in `docs/puml/`. Hierdoor blijft de geschiedenis van diagramwijzigingen in Git behouden.
4. **Verwijs vanuit het ontwikkeldocument naar het bronbestand:**
   - Voorkom dubbele onderhoudslasten: verwijs in het Markdown-ontwikkeldocument naar het `.puml` bestand in plaats van lange lappen code op te nemen die niet direct renderen in standaard Markdown previews.
