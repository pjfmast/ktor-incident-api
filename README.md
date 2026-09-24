# Ktor Incidents API

This project is a demo for a modern Ktor backend application.
(Intended as a first step for a full-stack Android or Kotlin Multiplatform app using this Ktor API).

For the complete architecture, case description, security design, and developer instructions:
👉 **[Ontwikkeldocument](docs/ontwikkeldocument.md)** *(in Dutch)*

For an educational introduction to Ktor theory based on this project:
👉 **[Ktor Theorie- en Introductiegids](docs/theorie-ktor-introductie.md)** *(in Dutch)*

---

### Highlights & Modern Tech Stack

- **Build System**: Built with the modern **Kotlin Toolchain** (powered by Amper) using the clean, flattened standard project layout and JDK 25.
- **Ktor 3.6**: Utilizes modern Ktor 3.6 features, including the new **Typed Authentication API with declarative roles** (`withRoles` and `authenticateWith`).
- **Security & Authorization**: Role-Based Access Control (RBAC) combined with Resource Ownership checks (ABAC) and JWT authentication.
- **Persistence**: **JetBrains Exposed** ORM with automated demo data seeding on H2 (in-memory or file-based).
- **Serialization**: Native JSON handling via `kotlinx.serialization`.
- **Interactive API Testing**: Ready-to-use HTTP requests in `test/http-requests/` for direct execution via IntelliJ IDEA's HTTP Client.

---

### Quick Start

This project uses the official Kotlin Toolchain wrapper (no Gradle installation required):

```bash
# Run automated tests
./kotlin test          # Linux / macOS
.\kotlin.bat test      # Windows

# Run the backend server (starts on http://localhost:8080)
./kotlin run           # Linux / macOS
.\kotlin.bat run       # Windows
```

---

### Ktor application modules

The Ktor application structure is grouped by the following features:

| module                                                        | description                                                                                                                                                                                                                                                                                       |
|---------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [UsersModule](src/avans/avd/users/UsersModule.kt)             | A user may register and keep informed about report incidents<br/> qualified users may change the status of an incident                                                                                                                                                                            |
| [AuthModule](src/avans/avd/auth/AuthModule.kt)                | a registered user can login to report and manage incidents                                                                                                                                                                                                                                        |
| [IncidentsModule](src/avans/avd/incidents/IncidentsModule.kt) | Incidents can be reported anonymously or by a registered user<br/>An incident has location, a status and can be documented with images of the incident<br/>reported incidents can be managed and when an incident report is deleted the related uploaded images of the incident are also deleted. |

