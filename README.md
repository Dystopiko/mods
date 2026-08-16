# `Dystopiko/mods`

A Gradle multi-project monorepo containing custom Fabric mods used for Dystopia S4 and succeeding seasons.

## Subprojects

| Project | Description |
|---|---|
| `deathchest` | **DeathChest** — Spawns a protected death chest on player death for players with the `dystopia.deathchest` perk, securely storing inventory and equipment. |
| `fasthoppers` | **FastHoppers** — Waxing a hopper with honeycomb upgrades its transfer speed by setting its cooldown to 1 tick. |
| `itemfilter` | **ItemFilter** — A server-side hopper item filter block built using Polymer with optional Geyser Bedrock support. |
| `instantrestock` | **InstantRestock** — Enables instant villager trade restocking without offer exhaustion, balancing trade XP rewards to 50%. |
| `dev` | **Dev Aggregator** — Launches a single unified development server with all mods loaded and automatically downloads external runtime dependencies (`Floodgate`, `Geyser`, `LuckPerms`, `Eden`). |

## Dependencies

External dependencies such as `edenmc-api` are consumed as Maven artifacts rather than being built directly in this repository:

```toml
edenmc-api = { module = "xyz.memothelemo.edenmc:api", version.ref = "edenmc-api" }
```

All centralized version declarations live in [`libs.versions.toml`](libs.versions.toml).

## Requirements

- **Java 25+**
- **Gradle 9.4.1** (via included wrapper `./gradlew`)
- **Fabric Loader 0.19.3+** / **Minecraft 26.2**

## Build & Run

```bash
# Compile all mod subprojects and copy their JAR files to root build/libs
./gradlew release

# Build all subprojects (including dev aggregator)
./gradlew build

# Build a specific subproject
./gradlew :deathchest:build

# Launch the unified dev server with all mods & downloaded dependencies loaded
./gradlew runServer   # or ./gradlew :dev:runServer

# Download dev environment mod dependencies manually
./gradlew :dev:downloadModDependencies
```

## Updating Dependencies & Minecraft Version

```bash
# Update dependencies for a target Minecraft version (e.g. 26.2 or 26.1.2)
./scripts/update-dependencies.sh 26.2
```

This script automatically fetches the latest compatible versions and updates:
- **`libs.versions.toml`**: `minecraft`, `fabric-loader`, `fabric-api`, `fabric-permissions`, `edenmc-api`, `polymer`, `adventure-api`, `adventure-fabric`.
- **`dev/build.gradle`**: Auto-detects latest download URLs from Modrinth API and GitHub Releases (`Floodgate`, `Geyser`, `LuckPerms`, `Eden`).

## Mod Loader

**Fabric only.** No Forge or NeoForge support.
