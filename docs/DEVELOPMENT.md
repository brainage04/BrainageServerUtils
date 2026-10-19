# Development

```shell
./gradlew --no-daemon build runAllGameTests -Pfabricmoddingconventions.devClientOptions=/nonexistent
```

Production GameTests validate entrypoint initialization, gameplay mixins, and commands on each loader. The shared GameTest bodies live in `common/src/gametest` and are compiled into each loader's GameTest source set.

On Thomas's hosts, run Gradle through `heavy` as described in the workspace `AGENTS.md`. The client-options property above prevents copying personal Prism settings into a client test run.
