# Learning Rounds: Progress Log

This project is a hands-on way to learn **Groovy and Grails**, building a file storage app one round at a time.
This file records what has been done, the problems hit along the way, and what comes next.
**Update it at the end of every round** (see "How to update this file" at the bottom).

## Current status

**Next up: Round 3. Postgres, `.env` and the Docker dev setup.**

| # | Round | Status | Commit |
|---|---|---|---|
| 1 | Groovy crash course and IntelliJ setup | ✅ Done | `5319da4`, `5dde12b` |
| 2 | Tour of Grails and your first endpoint | ✅ Done | _(fill in the hash after committing)_ |
| 3 | Postgres, `.env` and the Docker dev setup | ⏭️ Next | |
| 4 | Domain classes and GORM (`User`) | ⬜ | |
| 5 | Registration and BCrypt | ⬜ | |
| 6 | Login, JWT and the auth interceptor | ⬜ | |
| 7 | Upload, list, download and delete files | ⬜ | |
| 8 | Sharing files with other users | ⬜ | |
| 9 | Integration tests (Testcontainers Postgres) | ⬜ | |
| 10 | Production: migrations, prod Dockerfile, compose | ⬜ | |

## The app

A file storage REST API that runs on a server. Users register with a **username and password**, which is hashed with BCrypt, and log in to get a **JWT**.
A user uploads files and can **share** them with other users. The uploader always stays the **owner**.

## Learner profile

- Already knows **Spring Boot** well: Maven, JPA, JWT, Docker Compose. Their reference project is `../CoolWebAppWithAllFeature/backend`, which may not exist on other PCs.
- **Complete beginner** at Groovy and Grails, and wants explanations a beginner can follow.
- Uses **IntelliJ IDEA Ultimate**, with runs delegated to Gradle.

## Stack (from the Grails 8.0.0 BOM)

| Thing | Version / choice |
|---|---|
| Grails | 8.0.0 (Apache Grails), `web` profile |
| Groovy | 5.1.3 |
| Spring Boot | 4.1.1, underneath Grails |
| GORM | Hibernate 5.6 (`grails-data-hibernate5`) |
| Tests | Spock 2.4 on JUnit 5. Mockito is available but Spock mocks are preferred |
| Build | Gradle 9.8 wrapper, `compileJava.options.release = 21` |
| JDK on the dev PC | 25 (the build targets 21; Docker images will use 21) |
| Server port | `9030` (`grails-app/conf/application.yml`) |
| Database | H2 for now, Postgres from Round 3 |
| Base package | `com.example` |

## Decisions made

- **Login code is written by hand, not with a plugin.** BCrypt comes from `spring-security-crypto` (a bean in `resources.groovy`), JWT from `nimbus-jose-jwt` (the Grails BOM already manages its version), and a Grails **Interceptor** checks the token. The official `grails-spring-security-rest` plugin exists for Grails 8 and is an optional extra.
- **A JSON API only, with no UI.** It's tested with curl or Postman. A GSP UI is an optional extra.
- **Files are stored on local disk** (a Docker volume in production) under random UUID names, never the user's filename. Details about each file are stored in Postgres.
- **Sharing is read-only.** Shared users can view and download a file. Only the owner can delete it or share it further.
- **Tests are written every round**, not all at the end.
- **Every route is listed explicitly in `UrlMappings.groovy`.** The generated catch-all `/$controller/$action?/$id?` was removed in Round 2. Use `get`/`post`/`put`/`delete "/api/..."(controller: ..., action: ...)`.
- **Errors are returned as JSON.** `ErrorController` handles the `"404"` and `"500"` mappings. A 500 never reveals exception details to the client.
- **`api.http`** (IntelliJ HTTP Client) holds example requests for trying the API by hand. Add every new endpoint to it.

---

## Done rounds

### Round 1: Groovy crash course and IntelliJ setup ✅

**What was built**
- `src/test/groovy/com/example/learning/`: five Spock specs that teach Groovy, with **38 tests, all passing**. Read them in this order:
  1. `SpockBasicsSpec`: `expect`, `given/when/then`, `where:` tables, `thrown()`, `Stub()`/`Mock()`
  2. `GroovyBasicsSpec`: `def`, string types (GString), `==` vs `.is()`, Groovy truth, `?.`, `?:`, `5 / 2 == 2.5`, dynamic method calls
  3. `CollectionsSpec`: list and map literals, `collect`/`findAll`/`find`/`groupBy`, `*.`, ranges, `in`
  4. `ClosuresSpec`: closures, named arguments as a Map, and how `static constraints = { ... }` works (closure + delegate + `methodMissing`)
  5. `ClassesSpec`: properties, named-argument constructors, default parameter values, `@ToString`/`@EqualsAndHashCode`, traits, `@CompileStatic`
- `.run/Grails App.run.xml` (runs `com.example.Application`, module `storage-grails.main`) and `.run/Unit Tests.run.xml` (the Gradle `test` task). These are shared IntelliJ run configurations.
- `server.port: 9030` in `application.yml`.

**Problems hit, and how they were solved**
1. **IntelliJ only showed "Current File" and had no run configuration.** IntelliJ creates one automatically only for classes marked `@SpringBootApplication`. Grails' `Application` extends `GrailsAutoConfiguration` instead, so it isn't detected. Running from the gutter ▶ did work, but only while `Application.groovy` was the open tab. Fixed by adding the `.run/` configs above. If they don't show up, use File → Reload All from Disk.
2. **"10 actionable tasks" but 38 tests?** Gradle *tasks* are build steps, and the single `test` task runs every test. `UP-TO-DATE` means Gradle skipped the step because nothing changed. Gotcha: running `./gradlew test` twice in a terminal skips the tests the second time, so use `./gradlew test --rerun`. IntelliJ always reruns.
3. **Warnings during tests** (fixed in commit `5dde12b`, in `build.gradle`):
   - The `Mockito is currently self-attaching…` and `A Java agent has been loaded dynamically` warnings appeared because Spock's `Stub()`/`Mock()` use Mockito internally. Fixed by adding a `mockitoAgent` configuration and passing `-javaagent:` to the `Test` tasks.
   - The `Sharing is only supported for boot loader classes…` warning is the JVM's startup cache (CDS) reacting to Mockito adding a jar to the core class path. Fixed with `-Xshare:off`, which applies to test JVMs only.
   - The `Deprecated Gradle features… incompatible with Gradle 10` warning comes from the Grails GSP Gradle plugin (`build.gradle:24`, `apply plugin: "org.apache.grails.gradle.grails-gsp"`), not our code. **Ignored on purpose.** Revisit when Grails updates the plugin.

**Open items**
- `ClosuresSpec` lines 54 and 61 spell `naem` instead of `name` (in both places, so the test still passes). The learner may have done it on purpose. Ask before fixing.

**Commits**
- `Round 1: add Groovy/Spock learning specs, IntelliJ run configs, and set server port to 9030`
- `Load Mockito as a test JVM agent to remove self-attach and CDS warnings`

### Round 2: Tour of Grails and your first endpoint ✅

**What was explained**
- Grails folder conventions mapped to Spring Boot:
  - `controllers/*Controller` ↔ `@RestController`
  - `services/` ↔ `@Service`
  - `domain/` ↔ `@Entity` + repository
  - `UrlMappings` ↔ `@RequestMapping`
  - `*Interceptor` ↔ filters
  - `resources.groovy` ↔ `@Bean`
  - `BootStrap` ↔ `CommandLineRunner`
- Grails environments (development, test, production) and how they relate to Spring profiles.
- Controller "actions", rendering JSON with `as JSON`, and what `@Integration` does.

**What was built**
- `PingController`: `GET /api/ping` returns `{"message":"pong","environment":"development"}`.
- `ErrorController` with JSON 404 (`{"error":"Not Found","path":...}`) and JSON 500 (`{"error":"Internal Server Error"}`).
- `UrlMappings.groovy` rewritten: explicit routes only, with 404 and 500 going to `ErrorController`.
- Deleted `views/index.gsp`, `views/error.gsp`, `views/notFound.gsp` and the Geb test `BasicStorageSpec`.
- Tests: `PingControllerSpec`, `ErrorControllerSpec` (`ControllerUnitTest`) and `UrlMappingsSpec` (`UrlMappingsUnitTest`, including negative cases), giving 47 unit tests. Plus `PingApiIntegrationSpec` (`@Integration`, real HTTP calls), with 3 integration tests. All pass.
- `api.http`: requests to send from IntelliJ's HTTP Client.

**Problems hit, and how they were solved**
1. **The generated catch-all route exposed everything.** `"/$controller/$action?/$id?"` made every action of every controller reachable at `/controller/action`, with any HTTP method. Spring developers don't expect that. Removed it, and `UrlMappingsSpec` now checks that `/ping/index` doesn't route.
2. **The wrong HTTP method gives 404, not 405.** `POST /api/ping` returns 404 in Grails, where Spring would return 405. This was confirmed with curl and is pinned down in the integration test.
3. **Removing the welcome page would have broken `BasicStorageSpec`.** That generated Geb browser test checked the welcome page title, and it needs Docker for a browser container. It was replaced with `PingApiIntegrationSpec`, which uses plain `java.net.http.HttpClient` and `JsonSlurper`.
4. **Finding the server port in an `@Integration` test.** Grails' `@Integration` is `@SpringBootTest(RANDOM_PORT)` underneath. Get the port with `@Value('${local.server.port}') Integer serverPort`.
5. **Verified:** the Grails environment is also activated as a Spring profile. The log shows `The following 1 profile is active: "development"`, so both `environments:` blocks and `application-<env>.yml` files work.
6. **Verified:** a real exception produces a JSON 500 with no details sent to the client. The full stack trace is logged at ERROR by Grails' `StackTrace` logger. This was checked with a temporary throwing controller, which was then removed.

**Open items**
- The `naem` typo from Round 1 in `ClosuresSpec` is still there.
- GSP leftovers are now unused but kept for the optional GSP UI extra: `views/layouts/main.gsp`, `grails-app/assets/`, the webjars, the Geb dependency, and the `grails-gsp`/asset-pipeline Gradle plugins. They could be removed later if the UI extra is skipped.

**Commits**
- `Round 2: add /api/ping, JSON error responses and explicit URL mappings; remove welcome page`

---

## Upcoming rounds

### Round 3: Postgres, `.env` and the Docker dev setup
- Replace H2 with Postgres. Add the `org.postgresql:postgresql` runtime dependency (its version is managed by the BOM).
- Read the datasource settings and secrets from environment variables.
- Create a `.env` file and **add it to `.gitignore`**, and commit a `.env.example` alongside it.
- Explain that Spring Boot does not read `.env` by itself: Docker Compose does. Running from IntelliJ needs another way in, for example `spring.config.import: optional:file:.env[.properties]`. Verify that this works in Grails before teaching it.
- Create `Dockerfile.dev` and `compose.dev.yml` (Postgres plus the app running from source).
- Check that it works through `/actuator/health`.

### Round 4: Domain classes and GORM
- Create a `User` domain class with `username` (unique, not blank) and `passwordHash`.
- **Gotcha:** `user` is a reserved word in Postgres. Map the table with `static mapping = { table 'users' }` or a similar name.
- Cover constraints, dynamic finders (`User.findByUsername`), `save()`/`validate()`/`errors`, and `dbCreate`.
- Write a `DomainUnitTest`.

### Round 5: Registration and BCrypt
- Add `UserService`, and explain dependency injection by name.
- Use a command object (a validated DTO) for the request.
- Define a `BCryptPasswordEncoder` bean from `spring-security-crypto` in `resources.groovy`.
- Build `POST /api/auth/register`, returning 201, 400 or 409.
- Write a `ServiceUnitTest` and a `ControllerUnitTest`.

### Round 6: Login, JWT and the auth interceptor
- Add a `JwtService` using `nimbus-jose-jwt`, with the secret coming from the environment.
- Build `POST /api/auth/login`, which returns a token.
- Add an `AuthInterceptor` that protects `/api/**` except the auth endpoints. Compare it to the Spring Security filter chain.
- Build `GET /api/me`, which returns the logged-in user.

### Round 7: Upload, list, download and delete files
- Create a `StoredFile` domain class: owner, original name, content type, size, storage key (a UUID) and date created.
- Add a `StorageService` that writes files to a configured folder (`STORAGE_DIR`). Guard against path traversal.
- Configure upload size limits (`grails.controllers.upload.maxFileSize` / `maxRequestSize`).
- Build `POST /api/files`, `GET /api/files`, `GET /api/files/{id}/download` (streamed, with `Content-Disposition`) and `DELETE /api/files/{id}`.

### Round 8: Sharing files with other users
- Create a `FileShare` domain class (file, shared with, date created), unique on the file + user pair.
- Build endpoints to share and unshare a file by username, list "files shared with me", and list who a file is shared with.
- Enforce the rules: only the owner can delete or reshare a file, and shared users can only view and download it.

### Round 9: Integration tests
- Write `@Integration` tests against Postgres running in Testcontainers (Testcontainers 2.0.5 is managed by the Spring Boot BOM).
- Test the whole story: A and B register, A uploads a file and shares it with B, B downloads it, and B gets refused when deleting it.

### Round 10: Production
- Add database migrations: the Liquibase plugin `org.apache.grails:grails-data-hibernate5-dbmigration`, with `dbCreate: none` in production.
- Write a multi-stage `Dockerfile`: a JDK 21 build stage, then a JRE 21 runtime that runs as a non-root user.
- Write `compose.prod.yml` with named volumes for the database and the files, health checks (actuator) and restart policies.
- Compare it line by line with the learner's Spring `Dockerfile`. One teaching point: there, `ARG PORT` exists only while the image is built, and the `ENTRYPOINT` never uses `$PORT`. The port actually comes from the app's own config.

### Optional extras
A GSP web UI, storage limits per user, public share links, refresh tokens, and replacing the hand-written login code with `grails-spring-security-rest`.

---

## How to update this file

At the end of every round:
1. Update the **Current status** line and the status table: ✅ for done rounds, ⏭️ for the next one, and the commit hashes.
2. Move the round from **Upcoming** to **Done**, using this layout: **What was built** / **Problems hit, and how they were solved** / **Open items** / **Commits**.
3. Write down **every problem** you hit, even small ones: what the symptom looked like, what caused it, and how it was fixed or why it was ignored.
4. If the plan changed (a new decision, a reordered round), update **Decisions made** and **Upcoming rounds**.
