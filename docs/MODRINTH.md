# Modrinth publishing

Modrinth and CurseForge publishing are performed by the MinecraftModConventions reusable `reusable-multiloader-release.yml` workflow, which `.github/workflows/release.yml` calls. It publishes the exact Fabric and NeoForge release JARs prepared from a matching `v<mod_version>` tag; see [RELEASE.md](RELEASE.md).

## Required configuration

Configure these repository settings before publishing:

- `MODRINTH_TOKEN` secret: a Modrinth token permitted to publish versions.
- `MODRINTH_PROJECT_ID` variable: the existing Modrinth project ID for BrainageServerUtils.
- `CURSEFORGE_TOKEN` secret and `CURSEFORGE_PROJECT_ID` variable: the CurseForge upload token and the ID of the `brainage-server-utils` project.

A missing token skips only that destination; the GitHub Release still proceeds.

`.modrinth/project.json` holds the Modrinth categories that the publishing tasks read. The NeoForge version declares no extra dependencies, so there is no `.modrinth/neoforge-dependencies.json`.
