# Learning Rounds: Progress Log

This project is a hands-on way to learn **Groovy and Grails**, building a file storage app one round at a time.
This file records what has been done, the problems hit along the way, and what comes next.
**Update it at the end of every round** (see "How to update this file" at the bottom).

## Current status

**Next up: Round 4. Domain classes and GORM (`User`).**

| # | Round | Status | Commit |
|---|---|---|---|
| 1 | Groovy crash course and IntelliJ setup | ✅ Done | `5319da4`, `5dde12b` |
| 2 | Tour of Grails and your first endpoint | ✅ Done | `87993b5` |
| 3 | Postgres, `.env` and the Docker dev setup | ✅ Done | _(fill in the hash after committing)_ |
| 4 | Domain classes and GORM (`User`) | ⏭️ Next | |
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
| Database | Postgres 18 (`postgres:18-alpine`) in development. The `test` environment still uses in-memory H2 until Round 9 |
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
- **Config comes from environment variables, with `.env` for local values.** `application.yml` uses `${DB_HOST:localhost}`-style placeholders. `.env` is gitignored; `.env.example` is committed and must list every variable without a default (`EnvExampleSpec` enforces this). New secrets (e.g. `JWT_SECRET` in Round 6) go in both files, and into `Application.REQUIRED_SETTINGS` if the app can't start without them.
- **The `test` environment stays on H2 until Round 9**, so `./gradlew integrationTest` needs no Docker yet. H2 is a `testRuntimeOnly` dependency.
- **Development uses `dbCreate: update`** (keeps data across restarts). Production is `none`, and Round 10 adds migrations.

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

### Round 3: Postgres, `.env` and the Docker dev setup ✅

**What was explained**
- Spring Boot never reads `.env`. Docker Compose does, in two separate ways: for `${...}` interpolation inside the compose file (automatic), and as container environment variables (only with `env_file:` / `environment:`).
- `spring.config.import: optional:file:.env[.properties]` makes Spring load `.env` as a properties file when running from IntelliJ or `bootRun`. `optional:` means no error if it's missing; `[.properties]` names the format because the file has no extension. The path is relative to the **working directory**.
- Precedence: real environment variables beat values imported from `.env`. That's how `compose.dev.yml` swaps `DB_HOST=localhost` for `DB_HOST=postgres`.
- Inside a container, `localhost` is the container itself, so the app reaches Postgres by service name on the container port (5432), not on the host-mapped port.
- The Spring Bean DSL in `resources.groovy` (`beanName(Class, ref('otherBean'))`) ↔ `@Bean` methods.

**What was built**
- `build.gradle`: `runtimeOnly "org.postgresql:postgresql"`, H2 moved to `testRuntimeOnly`, plus `spring-boot-jdbc` (for the health indicator class).
- `application.yml`: `spring.config.import` for `.env`; the datasource is Postgres, built from `DB_HOST`, `DB_PORT`, `DB_NAME` (with defaults) and `DB_USERNAME`, `DB_PASSWORD` (no defaults); development uses `dbCreate: update`; the `test` environment overrides everything back to H2; `management.endpoint.health.show-details: always`.
- `.env.example` (committed) and `.env` (gitignored, made with `cp .env.example .env`).
- `Dockerfile.dev` (`eclipse-temurin:21-jdk`, runs `./gradlew bootRun` on the mounted source) and `compose.dev.yml` (Postgres 18 with a healthcheck and a named volume; the app with `env_file: .env`, `DB_HOST: postgres`, `depends_on: service_healthy`; host port `${APP_PORT:-9030}`).
- `resources.groovy`: a `dbHealthIndicator` bean, so `/actuator/health` shows `components.db`.
- `Application.groovy` implements `EnvironmentAware` and fails fast with a clear message when a datasource placeholder has no value.
- Tests: `ApplicationSpec` (the startup check), `EnvExampleSpec` (every required `${VAR}` in `application.yml` and `compose.dev.yml` is in `.env.example`; `.env` is gitignored), giving 52 unit tests. `HealthApiIntegrationSpec` (health is UP with a db check), giving 4 integration tests. All pass.
- Checked by hand: `/actuator/health` shows `db: UP, database: PostgreSQL` both from `bootRun` and with the app running inside `compose.dev.yml`.

**Problems hit, and how they were solved**
1. **`/actuator/health` had no `db` check, even with Postgres connected.** Two causes. (a) Spring Boot 4 moved the JDBC health indicator into a separate `spring-boot-jdbc` module that Grails doesn't pull in. (b) Even after adding it, Boot's auto-configuration only creates the check if a `DataSource` bean already exists when auto-configuration runs, and GORM registers `dataSource` later. Fixed by registering `DataSourceHealthIndicator` by hand in `resources.groovy` (bean name `dbHealthIndicator` → component `db`). Adding `spring-boot-jdbc` did **not** create a second connection pool (checked: only `HikariPool-1`).
2. **A missing `.env` didn't stop the app; it used the literal text `${DB_USERNAME}` as the username.** Plain Spring Boot fails with "Could not resolve placeholder", but Grails' config loader silently keeps unresolved placeholders. Postgres then rejected a user named `${DB_USERNAME}` and Hibernate failed with a confusing `JdbcEnvironment` error. A check in `BootStrap` was tried first and was useless, because with Postgres the failure happens while the context is built, **before** `BootStrap.init` runs. Fixed in `Application.setEnvironment` (it runs before Hibernate): Spring's `Environment.getProperty` *does* throw `PlaceholderResolutionException`, which is rethrown with a "copy .env.example to .env" message.
3. **Verified:** a real environment variable overrides `.env` (`DB_PASSWORD=wrong ./gradlew bootRun` logged in as `storage` from `.env` with the wrong password).
4. **Running the app container as root would have left root-owned files in `build/` and `.gradle/`**, breaking the next local build with "permission denied". Fixed with anonymous volumes for `/app/build` and `/app/.gradle` in `compose.dev.yml`, and Gradle's downloads in a named `gradle-home` volume. Checked: no root-owned files in the project after a container run.
5. **Postgres 18 images keep data under `/var/lib/postgresql`**, not `/var/lib/postgresql/data` like older versions, so the volume is mounted there.
6. **`EnvExampleSpec` first flagged `${NAME}` from an example in a yml comment.** The spec now ignores `#` comment lines.
7. **The IntelliJ app stopped serving on 9030 while `build.gradle`/config changed under it** (DevTools restart with the old classpath). Restart the run config after reloading Gradle.

**Open items**
- The learner edited `.run/Grails App.run.xml` in IntelliJ (JRE 21, module renamed `storage-grails.main` → `basicStorage.main`, `WORKING_DIRECTORY` line removed). Not committed by Claude. Because `.env` is read relative to the working directory, the run config must start in the project folder; IntelliJ's default for a Gradle module usually is the project folder, but re-add `$PROJECT_DIR$` if the app can't find `.env`.
- The `naem` typo from Round 1 in `ClosuresSpec` is still there.

**Commits**
- `Round 3: switch to Postgres with .env config, add Docker dev setup and a db health check`

---

## Upcoming rounds

### Round 4: Domain classes and GORM
- Create a `User` domain class with `username` (unique, not blank) and `passwordHash`.
- **Gotcha:** `user` is a reserved word in Postgres. Map the table with `static mapping = { table 'users' }` or a similar name.
- Cover constraints, dynamic finders (`User.findByUsername`), `save()`/`validate()`/`errors`, and `dbCreate`.
- Write a `DomainUnitTest`.
- With Postgres running, `dbCreate: update` creates the `users` table. Look at it with `docker compose -f compose.dev.yml exec postgres psql -U storage storage` → `\d users`.

### Round 5: Registration and BCrypt
- Add `UserService`, and explain dependency injection by name.
- Use a command object (a validated DTO) for the request.
- Define a `BCryptPasswordEncoder` bean from `spring-security-crypto` in `resources.groovy`.
- Build `POST /api/auth/register`, returning 201, 400 or 409.
- Write a `ServiceUnitTest` and a `ControllerUnitTest`.

### Round 6: Login, JWT and the auth interceptor
- Add a `JwtService` using `nimbus-jose-jwt`, with the secret coming from the environment (`JWT_SECRET` in `.env`/`.env.example`, added to `Application.REQUIRED_SETTINGS`).
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
- Write `@Integration` tests against Postgres running in Testcontainers (Testcontainers 2.0.5 is managed by the Spring Boot BOM). Remove the H2 `test` environment override and the `testRuntimeOnly` H2 dependency, and make `HealthApiIntegrationSpec` expect `PostgreSQL`.
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
