# CLAUDE.md

This repo is a **learning project**. The user is learning Groovy and Grails by building a file storage app in rounds.

## Before doing anything

Read **`ROUNDS.md`**. It records which round is next, what each finished round built, the problems already solved, and the decisions already made. Don't redo finished rounds and don't reopen settled decisions unless the user asks.

## How to run a round

- Explain every Grails or Groovy concept **for a beginner**, mapped to the Spring Boot equivalent the user already knows.
- **Write the code yourself.** Comment wherever Groovy or Grails does something surprising.
- Add **Spock tests** for whatever the round builds, and run them (`./gradlew test --rerun`) before saying the round is done.
- Finish with the commands the user should run to see it working, plus a **one-line commit message** in a code block. Don't commit unless the user asks.

## Keep ROUNDS.md up to date (required)

At the end of **every** round, and whenever a problem gets solved or the plan changes, update `ROUNDS.md` following the "How to update this file" section at its bottom. That file is how the next session, possibly on another PC, knows where things stand.

## Commands

- Run the app: the IntelliJ run config "Grails App", or `./gradlew bootRun` (port 9030)
- Unit tests: `./gradlew test --rerun` (or the IntelliJ run config "Unit Tests")
- Integration tests: `./gradlew integrationTest`
