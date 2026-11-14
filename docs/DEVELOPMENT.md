# Development

```shell
./gradlew --no-daemon build runAllGameTests
```

Production GameTests validate entrypoint initialization, gameplay mixins, and commands on each loader. The shared GameTest bodies live in `common/src/gametest` and are compiled into each loader's GameTest source set.

On Thomas's hosts, run Gradle through `heavy` as described in the workspace `AGENTS.md`. The client-options property above prevents copying personal Prism settings into a client test run.

## Rankings-file recovery

`config/brainageserverutils.json` loads through BrainageLib's `JsonFile`, whose default `KEEP_ORIGINAL` policy refuses to save after a failed load. The file is only written to create the defaults when it is missing, and that write goes through `AtomicFiles`, so a reader sees either the old file or the complete new one. Malformed JSON, a wrong shape and duplicate enchantment IDs are rejected rather than repaired; an unreadable file stays as it is. Commands that need the rankings fail with the reason, and a corrected file is picked up on the next use, since the file is read on every use.

GameTests cover the previous version's file format, the exact bytes of the recreated defaults, and an unreadable file surviving a command and a restart unchanged.

## Messages

Command replies and scoreboard names are translation keys in `common/src/main/resources/assets/brainageserverutils/lang/en_us.json`, sent through `Translations.text`. Clients do not need the mod, so each message also carries its English text from that file as the fallback an unmodified client shows. `TranslationsTest` (Fabric unit tests) fails when the code uses a key the file lacks, when the file has a key no code uses, or when a reply is built from a hard-coded string.
