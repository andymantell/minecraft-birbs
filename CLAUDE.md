# British Birds Minecraft Mod

## What This Is

A Fabric mod for Minecraft Java Edition 26.1 adding 100 British bird species as wild mobs with spawn eggs. Focus on biological accuracy: realistic models, appropriate biome spawning, species-specific flight and feeding behaviour, and authentic bird calls.

## Tech Stack

- **Minecraft:** Java Edition 26.1 (new year-based versioning, unobfuscated)
- **Mod Loader:** Fabric (Loader 0.18.4+, API 0.143.14+26.1)
- **Build:** Loom 1.15, Gradle 9.4.0, Java 25
- **No external dependencies** beyond Fabric API (no GeckoLib)
- **Models:** Vanilla entity system, procedural animations in Java
- **Textures:** 32x32 enhanced vanilla style
- **Sounds:** .ogg format, sourced from xeno-canto (CC BY-SA or CC BY-NC-SA only)

## Project Structure

- `src/main/java/com/birbs/britishbirds/` — mod source code (server-safe)
- `src/main/java/com/birbs/britishbirds/client/` — models & renderers (CLIENT-SIDE ONLY)
- `src/main/resources/assets/britishbirds/` — textures, sounds, lang, sounds.json
- `src/main/resources/data/britishbirds/` — data tags
- `docs/` — species research references, asset provenance, design spec
- `SOUND_CREDITS.md` — per-recording xeno-canto attribution
- `LICENSE-SOUNDS` — CC licence texts for sound assets

## Entity Hierarchy

```
AbstractBritishBird (root: age, sounds, spawn, textures)
├── AbstractFlyingBird (flight nav, perching)
│   ├── SmallPasserineEntity (hopping, short flights, song)
│   ├── RaptorEntity (soaring, hunting, high perches)
│   ├── CorvidEntity, PigeonEntity, HirundineEntity, WoodpeckerEntity [future]
├── AbstractWaterBird (swimming, water landing)
│   ├── WaterfowlEntity (dabbling, waddling, flocking)
│   ├── WaderEntity, DivingWaterBirdEntity [future]
└── GroundBirdEntity (ground-dwelling, burst flights) [future]
```

## Pilot Birds (Phase 1-7)

1. European Robin → SmallPasserineEntity
2. Blue Tit → SmallPasserineEntity
3. Barn Owl → RaptorEntity
4. Peregrine Falcon → RaptorEntity
5. Mallard → WaterfowlEntity

## Key Conventions

- **Mod ID:** `britishbirds`
- **Package:** `com.birbs.britishbirds`
- **One EntityType per species** — sex/age variants use texture switching, not separate entity types
- **AI goals are reusable** — composed per species from shared goal classes in `ai/`
- **All external assets must be logged** in `docs/ASSET_PROVENANCE.md` with source, author, licence, date
- **Sound attribution** in `SOUND_CREDITS.md`: recordist, XC number, licence, URL, modifications
- **No git commits by Claude** — pause after each phase for user to review and commit
- **Build incrementally** — each phase must produce a working, testable state

## Implementation Phases

1. Scaffold & base classes → mod loads, spawn egg works
2. Flight & movement foundation → robin hops and flies
3. Robin complete → full behaviour, model, textures, sounds
4. Blue Tit → validates songbird code reuse
5. Barn Owl → nocturnal hunter, activity schedule
6. Peregrine Falcon → stoop dive, soaring flight
7. Mallard → water birds, swimming, dabbling, dimorphism
8. Polish → dawn chorus, spawn balancing, inter-species interactions

## Research Documents

Detailed per-species research in `docs/`:
- `british_birds_100.md` — full 100-species list with families, sizes, habitats
- `european_robin_reference.md` — Robin biology, plumage, behaviour, sounds
- `blue_tit_reference.md` — Blue Tit biology
- `barn_owl_reference.md` — Barn Owl biology
- `peregrine_falcon_reference.md` — Peregrine biology
- `mallard_reference.md` — Mallard biology

## Design Spec

Full design specification: `docs/superpowers/specs/2026-03-19-british-birds-mod-design.md`
