# British Birds Minecraft Mod — Design Specification

**Date:** 2026-03-19
**Target:** Minecraft Java Edition 26.1, Fabric Loader 0.18.4+, Fabric API 0.143.14+26.1
**Toolchain:** Loom 1.15, Gradle 9.4.0, Java 25
**Mod ID:** `britishbirds`
**Package:** `com.birbs.britishbirds`

---

## 1. Overview

A Fabric mod adding 100 British bird species as wild mobs with spawn eggs. Focus on biological accuracy: realistic models/textures at 32x32 "enhanced vanilla" resolution, appropriate biome spawning, species-specific flight behaviour, feeding habits, nesting, and authentic bird calls sourced from xeno-canto (CC BY-SA or CC BY-NC-SA licensed).

The mod starts with a 5-bird pilot (European Robin, Blue Tit, Barn Owl, Peregrine Falcon, Mallard) chosen to stress-test the entity hierarchy across different size categories, flight styles, habitats, and behavioural archetypes.

No external dependencies beyond Fabric API. Models and animations are vanilla entity system (procedural, not GeckoLib) — all model geometry defined in Java code, all animations driven programmatically.

---

## 2. Bird List

The full 100-species list is documented in `docs/british_birds_100.md`, compiled from RSPB Big Garden Birdwatch data, BTO records, and standard ornithological references, grouped into 16 categories.

### 2.1 Pilot Birds

| # | Species | Size | Category | Key Behaviours |
|---|---------|------|----------|----------------|
| 1 | European Robin | Small (14 cm) | Songbird | Territorial, follows digging player, hops, crepuscular song |
| 2 | Blue Tit | Tiny (12 cm) | Songbird | Acrobatic clinging, upside-down hanging, winter flocking |
| 3 | Barn Owl | Medium (34 cm) | Raptor/Owl | Nocturnal, quartering flight, silent, hunts small mobs |
| 4 | Peregrine Falcon | Medium-Large (42 cm) | Raptor | Diurnal, soaring, high-speed stoop dive, hunts birds |
| 5 | Mallard | Large (58 cm) | Waterfowl | Swimming, dabbling, waddling, flocking, sexual dimorphism |

Detailed per-species research documents are in `docs/` (`european_robin_reference.md`, etc.).

---

## 3. Entity Hierarchy

### 3.1 Class Diagram

```
AbstractBritishBird extends AnimalEntity
│   Common: spawn egg, sound system, age (baby/adult), biome spawning,
│   size config, texture variants (male/female/juvenile)
│
├── AbstractFlyingBird extends AbstractBritishBird
│   │   Flight navigation, takeoff/landing state machine, perching
│   │
│   ├── SmallPasserineEntity            [Robin, Blue Tit]
│   │     Ground hopping, short fluttery flights, perching,
│   │     seed/insect eating, territorial song
│   │     Config: flockSize, canHangUpsideDown, followsPlayer,
│   │       territoriality, songPattern
│   │     → All tits, finches, sparrows, warblers, thrushes,
│   │       wren, nuthatch, treecreeper, wagtails, buntings, lark
│   │
│   ├── RaptorEntity                    [Peregrine, Barn Owl]
│   │     Soaring/gliding, hunting AI, high-point perching, territorial
│   │     Config: huntStyle (STOOP/PURSUIT/HOVER/QUARTERING),
│   │       activitySchedule (DIURNAL/NOCTURNAL/CREPUSCULAR),
│   │       silentFlight, canHover
│   │     → All raptors, all owls
│   │
│   ├── CorvidEntity                    [future]
│   │     Ground + flight, intelligent behavior, social, caching food
│   │     → Magpie, Crow, Jay, Raven, Jackdaw, Rook
│   │
│   ├── PigeonEntity                    [future]
│   │     Ground feeding, direct powerful flight, cooing
│   │     → Woodpigeon, Collared Dove, Feral Pigeon, Stock Dove
│   │
│   ├── HirundineEntity                 [future]
│   │     Aerial feeding, fast agile flight, rarely lands on ground
│   │     → Swallow, House Martin, Sand Martin, Swift
│   │
│   └── WoodpeckerEntity               [future]
│         Tree clinging, drumming, undulating flight
│         → Great Spotted, Green, Lesser Spotted Woodpecker
│
├── AbstractWaterBird extends AbstractBritishBird
│   │   Swimming navigation, water detection, water takeoff/landing
│   │
│   ├── WaterfowlEntity                [Mallard]
│   │     Dabbling/upending, walking on land, flocking, quacking
│   │     Config: canDive, gregariousness, isMigratory,
│   │       hasEclipsePlumage, ducklingFollowMother
│   │     → All ducks, geese, swans
│   │
│   ├── WaderEntity                     [future]
│   │     Long legs, probing in shallows, shore walking
│   │     → Lapwing, Curlew, Oystercatcher, Avocet
│   │
│   └── DivingWaterBirdEntity           [future]
│         Dives underwater from surface
│         → Cormorant, Grebes, Coot, Moorhen
│
└── GroundBirdEntity extends AbstractBritishBird
      Ground-dwelling, short explosive burst flights, running
      → Pheasant, Red Grouse, Grey Partridge
```

### 3.2 Design Rationale

- **Three locomotion archetypes** (`AbstractFlyingBird`, `AbstractWaterBird`, `GroundBirdEntity`) capture fundamentally different movement modes
- **Behavioral subclasses** within each archetype capture clusters of shared AI (e.g., all small passerines hop and flutter; all raptors soar and hunt)
- **Config params** within subclasses handle species variation without a new class per bird
- The 5 pilot birds hit 3 of 4 top-level categories, building the full foundation immediately

---

## 4. AI & Behavior System

### 4.1 Reusable AI Goal Classes

Behaviors are composed per species from a library of ~15 parameterized goal classes:

**Flight Controllers** (one per species):
- `FlutteringFlightGoal` — short, low, rapid wingbeats between perches
- `SoaringFlightGoal` — circle on thermals, glide with occasional flaps
- `StoopingFlightGoal` — high-altitude dive attack on prey
- `QuarteringFlightGoal` — low, slow, wavering flight; hover on prey detection
- `DirectFlightGoal` — strong, straight, rapid wingbeats

**Ground Movement** (composable):
- `HoppingMovementGoal` — hop-pause-hop with wing/tail flick
- `WaddlingMovementGoal` — side-to-side gait
- `PerchingGoal` — find and sit on fence posts, logs, leaves
- `AcrobaticClingingGoal` — hang upside-down from leaves/branches

**Water Behaviors**:
- `SwimmingGoal` — float on water surface, paddle movement
- `DabblingGoal` — upend animation, surface feeding
- `WaterTakeoffGoal` — vertical launch from water
- `WaterLandingGoal` — feet-forward braking, splash particles

**Feeding**:
- `GroundForagingGoal` — hop around, peck at ground for insects/seeds
- `HuntingGoal` — target small entities; configurable technique (stoop/pursuit/quartering)
- `FollowDiggingPlayerGoal` — follow player using hoe/shovel within range

**Social**:
- `FlockingGoal` — stay near conspecifics, move as loose group
- `TerritorialGoal` — claim area, sing from exposed perch, chase intruders
- `DucklingFollowGoal` — baby follows nearest adult in a line

**Schedule**:
- `ActivityScheduleGoal` — DIURNAL, NOCTURNAL, or CREPUSCULAR activity patterns

### 4.2 Per-Species Goal Composition

| Goal | Robin | Blue Tit | Peregrine | Barn Owl | Mallard |
|------|-------|----------|-----------|----------|---------|
| FlutteringFlight | X | X | | | |
| SoaringFlight | | | X | | |
| StoopingFlight | | | X | | |
| QuarteringFlight | | | | X | |
| DirectFlight | | | | | X |
| Hopping | X | X | | | |
| Waddling | | | | | X |
| Perching | X | X | X | X | |
| AcrobaticClinging | | X | | | |
| Swimming | | | | | X |
| Dabbling | | | | | X |
| WaterTakeoff | | | | | X |
| WaterLanding | | | | | X |
| GroundForaging | X | X | | | X |
| Hunting | | | X | X | |
| FollowDiggingPlayer | X | | | | |
| Flocking | | X | | | X |
| Territorial | X | | X | loosely | |
| DucklingFollow | | | | | X |
| DIURNAL | X | X | X | | X |
| NOCTURNAL | | | | X | |

---

## 5. Models & Rendering

### 5.1 Approach

All models defined in Java as `EntityModel` subclasses using `ModelPart` cuboids built via `ModelData`/`ModelPartData`/`ModelPartBuilder` in a static `getTexturedModelData()` method. Animations are procedural (driven in the renderer), not keyframed. This gives precise control — e.g., wing flap frequency responds to flight speed, birds tuck wings during dives, spread tail for braking.

**Client-side only:** All model classes (`EntityModel` subclasses), renderer classes (`EntityRenderer`/`MobEntityRenderer` subclasses), and `EntityModelLayer` registrations are client-side only. They live in a `client/` sub-package and are registered in `BritishBirdsClient` via `EntityRendererRegistry` and `EntityModelLayerRegistry`. Never reference these classes from server-side code or the mod will crash on dedicated servers.

### 5.1b Entity Data Tracking

Species with visual variants (sex, age, plumage) store their state via `TrackedData` (synced to client for rendering) and persist via NBT:

- **Sex** (`TrackedData<Boolean> IS_MALE`): Used by Mallard, Barn Owl, Peregrine. Randomly assigned at spawn. Determines which texture the renderer selects.
- **Age**: Vanilla baby/adult system via `AnimalEntity`. Juvenile textures shown when `isBaby()` returns true.
- **Plumage variant** (future): Could support eclipse plumage, seasonal changes via additional tracked data.

### 5.2 Size Categories

| Category | Real Size | MC Model Height | Hitbox (W x H blocks) | Examples |
|----------|----------|-----------------|----------------------|---------|
| Tiny | 9-12 cm | 4-5 pixels (~0.25 blocks) | 0.3 x 0.3 | Blue Tit, Goldcrest, Wren |
| Small | 12-18 cm | 5-7 pixels (~0.35 blocks) | 0.4 x 0.4 | Robin, Sparrow, Finches |
| Medium | 18-40 cm | 7-16 pixels (~0.7 blocks) | 0.5 x 0.5 | Barn Owl, Peregrine, Blackbird, Pigeon |
| Large | 40-65 cm | 16-24 pixels (~1.2 blocks) | 0.6 x 0.6 | Mallard, Buzzard, Heron |
| Very Large | 65-150 cm | 24-48 pixels (~2+ blocks) | 0.8 x 1.0 | Swan, Eagle |

### 5.3 Model Part Hierarchy

Shared across all species with per-species dimensions:

```
root
├── body              (main torso cuboid)
├── head
│   ├── beak          (upper + lower mandible for open/close)
│   └── eyes          (overlay layer for blink)
├── left_wing
│   ├── inner_wing
│   └── outer_wing    (folds/extends for flight phases)
├── right_wing
│   ├── inner_wing
│   └── outer_wing
├── tail              (fans/bobs, can have multiple feather parts)
├── left_leg
│   └── left_foot
└── right_leg
    └── right_foot
```

Species-specific additions:
- **Mallard:** webbed feet, drake tail curl
- **Barn Owl:** facial disc plate, longer legs
- **Peregrine:** larger talons
- **Blue Tit:** proportionally larger head

### 5.4 Procedural Animations

| Animation | Technique | Species |
|-----------|-----------|---------|
| Wing flap | Sinusoidal rotation, frequency tied to speed | All |
| Wing fold | Wings tuck against body when grounded/perched | All |
| Wing stoop | Wings pressed flat, narrow profile | Peregrine |
| Head tracking | Y-rotation toward nearest entity/player | All |
| Head tilt | Z-roll rotation, curious pose | Robin, Blue Tit |
| Tail bob | Small pitch oscillation | Robin (frequent), all (occasional) |
| Tail fan | X-scale increase for braking/display | Peregrine, Barn Owl |
| Hop cycle | Leg compress/extend, body bob | Robin, Blue Tit |
| Waddle | Alternating body roll with leg movement | Mallard (land) |
| Swim paddle | Leg alternation below waterline | Mallard (water) |
| Dabble/upend | Body pitches 90 degrees forward, tail up | Mallard |
| Upside-down hang | Body inverted, feet grip block above | Blue Tit |
| Beak open | Lower mandible rotates during calls | All (synced to sounds) |
| Puff up | Body scale increase for cold/resting | Robin |

### 5.5 Textures

- 32x32 pixel resolution (enhanced vanilla aesthetic)
- UV-mapped to model part hierarchy
- Colours from research documents
- Separate files per variant (male/female/juvenile), loaded by entity state
- One EntityType per species, not per variant

---

## 6. Sound System

### 6.1 Source

All bird calls sourced from xeno-canto.org. Recordings filtered by licence:
- **Preferred:** CC BY-SA (allows commercial use + derivatives, must share-alike)
- **Acceptable:** CC BY-NC-SA (allows derivatives, non-commercial, share-alike)
- **Excluded:** CC BY-NC-ND (no derivatives — cannot trim/convert)

### 6.2 Format

Downloaded as .mp3 from xeno-canto, trimmed and converted to .ogg (Minecraft's audio format). Each species has 2-3 variants per call type for natural variety (Minecraft picks randomly).

### 6.3 Attribution

Every recording is logged in:
- `SOUND_CREDITS.md` — per-recording table: recordist, XC catalogue number, licence, URL, species, modifications
- `docs/ASSET_PROVENANCE.md` — unified asset provenance log
- `LICENSE-SOUNDS` — CC licence texts and explanation

### 6.4 Call Types Per Species

| Species | Ambient Song | Contact Call | Alarm Call | Special |
|---------|-------------|-------------|------------|---------|
| Robin | Warbling melody (year-round, dawn/dusk) | "seep" | "tic-tic-tic" | — |
| Blue Tit | "tsee-tsee-chu-chu" | Thin "tsee" | Churring rattle | — |
| Barn Owl | — | Chirrups | Hiss + bill snap | Screech (territorial) |
| Peregrine | — | "ee-chip" | "kak-kak-kak" | Chitter (excitement) |
| Mallard | — | Soft cluck | Sharp quack | Female quack, male "raehb", wing whistle |

### 6.5 Dawn Chorus

`BirdSoundManager` schedules ambient bird songs based on Minecraft time-of-day. Songbirds sing more at dawn (MC ticks 0-2000) and dusk (MC ticks 11000-13000). Robin sings first (earliest dawn singer), followed by other species. Non-songbirds (owl, falcon, duck) use different scheduling.

---

## 7. Spawning & Biomes

### 7.1 Biome Mapping

| Species | Primary Biomes | Secondary Biomes | Exclusions |
|---------|---------------|-----------------|------------|
| Robin | Forest, Flower Forest, Birch Forest, Dark Forest | Cherry Grove, Taiga, Villages | Desert, Badlands, Ocean, Nether, End |
| Blue Tit | Forest, Birch Forest, Flower Forest | Plains (with trees), Villages, Cherry Grove | Dense Taiga, Treeless biomes |
| Barn Owl | Plains, Sunflower Plains | River edges, Swamp edges, Villages | Forest, Mountains, Taiga |
| Peregrine | Stony Peaks, Windswept Hills | Plains, River, Beach, near structures | Dense forest, Swamp |
| Mallard | River, Swamp, Mangrove Swamp | Plains/Forest near water, Villages near water | Desert, Badlands, Mountains, Ocean |

### 7.2 Spawn Weights

Common garden birds (Robin, Blue Tit) have high spawn weights. Scarce species (Peregrine) have low weights. Configured per biome.

### 7.3 Spawn Rules

`BirdSpawnRules` uses Fabric's `SpawnRestriction.register()` API to enforce:
- Light level requirements (Barn Owl spawns at low light)
- Surface block type (Mallard requires water within N blocks)
- Maximum per-species density per chunk
- Biome tag matching

---

## 8. Project File Structure

### 8.1 Build Configuration

**`gradle.properties`:**
```properties
# Fabric Properties
minecraft_version=26.1
yarn_mappings=26.1+build.1
loader_version=0.18.4
fabric_version=0.143.14+26.1

# Mod Properties
mod_version=0.1.0
maven_group=com.birbs
archives_base_name=britishbirds

# Java
java_version=25
```

**`fabric.mod.json` skeleton:**
```json
{
  "schemaVersion": 1,
  "id": "britishbirds",
  "version": "${version}",
  "name": "British Birds",
  "description": "100 British bird species with realistic behaviour",
  "authors": ["birbs"],
  "license": "MIT",
  "environment": "*",
  "entrypoints": {
    "main": ["com.birbs.britishbirds.BritishBirdsMod"],
    "client": ["com.birbs.britishbirds.BritishBirdsClient"]
  },
  "depends": {
    "fabricloader": ">=0.18.4",
    "fabric": ">=0.143.14",
    "minecraft": ">=26.1",
    "java": ">=25"
  }
}
```

**Key Fabric API modules needed:**
- `fabric-entity-events-v1` — entity lifecycle events
- `fabric-object-builder-api-v1` — EntityType building
- `fabric-biome-api-v1` — spawn biome configuration
- `fabric-rendering-v1` — client renderer registration
- `fabric-sound-api-v1` — sound event registration
- `fabric-item-group-api-v1` — creative tab for spawn eggs

These are included transitively via the `fabric-api` dependency.

### 8.2 Directory Layout

```
birbs/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── CLAUDE.md
├── LICENSE
├── LICENSE-SOUNDS
├── SOUND_CREDITS.md
├── docs/
│   ├── ASSET_PROVENANCE.md
│   ├── british_birds_100.md
│   ├── european_robin_reference.md
│   ├── barn_owl_reference.md
│   ├── peregrine_falcon_reference.md
│   ├── mallard_reference.md
│   └── blue_tit_reference.md
│
└── src/main/
    ├── java/com/birbs/britishbirds/
    │   ├── BritishBirdsMod.java          # Main entrypoint (server-safe)
    │   ├── BritishBirdsClient.java       # Client entrypoint (renderers, models)
    │   │
    │   ├── registry/
    │   │   ├── ModEntities.java          # EntityType registration
    │   │   ├── ModItems.java             # Spawn eggs
    │   │   └── ModSounds.java            # SoundEvent registration
    │   │
    │   ├── entity/
    │   │   ├── base/
    │   │   │   ├── AbstractBritishBird.java
    │   │   │   ├── AbstractFlyingBird.java
    │   │   │   ├── AbstractWaterBird.java
    │   │   │   └── GroundBirdEntity.java
    │   │   ├── songbird/
    │   │   │   ├── SmallPasserineEntity.java
    │   │   │   ├── RobinEntity.java
    │   │   │   └── BlueTitEntity.java
    │   │   ├── raptor/
    │   │   │   ├── RaptorEntity.java
    │   │   │   ├── PeregrineFalconEntity.java
    │   │   │   └── BarnOwlEntity.java
    │   │   └── waterfowl/
    │   │       ├── WaterfowlEntity.java
    │   │       └── MallardEntity.java
    │   │
    │   ├── client/                       # CLIENT-SIDE ONLY — never import from server code
    │   │   ├── model/
    │   │   │   ├── BirdModelLayers.java  # EntityModelLayer constants
    │   │   │   ├── RobinModel.java
    │   │   │   ├── BlueTitModel.java
    │   │   │   ├── BarnOwlModel.java
    │   │   │   ├── PeregrineFalconModel.java
    │   │   │   └── MallardModel.java
    │   │   └── renderer/
    │   │       └── BirdRenderer.java     # Generic parameterized renderer
    │   │
    │   ├── ai/
    │   │   ├── flight/
    │   │   ├── movement/
    │   │   ├── feeding/
    │   │   ├── social/
    │   │   └── schedule/
    │   │
    │   ├── sound/
    │   │   └── BirdSoundManager.java
    │   │
    │   └── world/
    │       └── BirdSpawnRules.java
    │
    └── resources/
        ├── fabric.mod.json
        ├── assets/britishbirds/
        │   ├── textures/entity/{species}/ # 32x32 .png per variant
        │   ├── sounds/{species}/          # .ogg files, 2-3 per call type
        │   ├── lang/en_us.json
        │   └── sounds.json               # Sound event definitions
        └── data/britishbirds/
            └── tags/entity_type/
```

### 8.3 sounds.json Format

Example entry for Robin:
```json
{
  "entity.britishbirds.robin.song": {
    "subtitle": "subtitles.britishbirds.entity.robin.song",
    "sounds": [
      "britishbirds:robin/song1",
      "britishbirds:robin/song2"
    ]
  },
  "entity.britishbirds.robin.call": {
    "sounds": ["britishbirds:robin/call"]
  },
  "entity.britishbirds.robin.alarm": {
    "sounds": ["britishbirds:robin/alarm"]
  }
}
```

### 8.4 Texture Naming Convention

Per-species directory with variant suffixes:
- `{species}.png` — default adult texture (or male if dimorphic)
- `{species}_female.png` — female variant (only if visually distinct)
- `{species}_juvenile.png` — juvenile variant (only if visually distinct)

Examples: `robin/robin.png`, `robin/robin_juvenile.png`, `mallard/mallard.png` (male), `mallard/mallard_female.png`, `mallard/mallard_duckling.png`

---

## 9. Implementation Order

### Phase 1: Scaffold & Base Classes
Initialize Fabric project. Build `AbstractBritishBird`, registration infrastructure (`ModEntities`, `ModItems`, `ModSounds`), mod entrypoints. Create a bare-bones Robin entity (no AI, placeholder texture) to verify the pipeline.

**Milestone:** Mod loads, Robin spawn egg works, featureless Robin stands in world.

### Phase 2: Flight & Movement Foundation
Build `AbstractFlyingBird` with flight navigation and takeoff/landing state machine. Implement `FlutteringFlightGoal`, `HoppingMovementGoal`, `PerchingGoal`. Wire into Robin.

**Milestone:** Robin hops on ground, flies between trees, perches on fences.

### Phase 3: Robin Complete
Build `SmallPasserineEntity`, `RobinEntity`, `FollowDiggingPlayerGoal`, `GroundForagingGoal`, `TerritorialGoal`. Create Robin model, renderer with idle animations, textures (adult + juvenile), sounds from xeno-canto, spawn rules, `BirdSoundManager`.

**Milestone:** Fully functional Robin with accurate appearance, behaviour, and sound.

### Phase 4: Blue Tit
Build `BlueTitEntity`, `AcrobaticClingingGoal`, `FlockingGoal`. Create model, texture, sounds, spawn rules.

**Milestone:** Two songbirds sharing SmallPasserine infrastructure; validates code reuse.

### Phase 5: Barn Owl
Build `RaptorEntity`, `BarnOwlEntity`, `QuarteringFlightGoal`, `HuntingGoal`, `ActivityScheduleGoal`. Create model (heart-shaped face), male + female textures, sounds (screech, hiss), spawn rules.

**Milestone:** First nocturnal hunter; tests activity schedule and hunting systems.

### Phase 6: Peregrine Falcon
Build `PeregrineFalconEntity`, `SoaringFlightGoal`, `StoopingFlightGoal`. Create model, textures (adult + juvenile), sounds, spawn rules.

**Milestone:** Raptor system validated with two different hunters (owl vs falcon).

### Phase 7: Mallard
Build `AbstractWaterBird`, `WaterfowlEntity`, `MallardEntity`, `SwimmingGoal`, `DabblingGoal`, `WaterTakeoffGoal`, `WaddlingMovementGoal`, `DucklingFollowGoal`. Create model (heavy body, flat bill), three textures (drake, hen, duckling), sounds (quack, raeb, wing whistle), spawn rules.

**Milestone:** All 5 pilot birds complete; all three locomotion archetypes proven.

### Phase 8: Polish & Integration
Dawn chorus system. Spawn weight balancing. Inter-species interactions (peregrine targets mod birds). Sound variety (2-3 variants per call). Cross-biome testing.

**Milestone:** Polished pilot ready for iteration toward remaining 95 birds.

---

## 10. Conventions

- **No git commits by Claude** — pause after each phase for user to commit
- **Asset provenance** — every external asset logged in `docs/ASSET_PROVENANCE.md` and `SOUND_CREDITS.md`
- **Sound licensing** — xeno-canto BY-SA preferred, BY-NC-SA acceptable, BY-NC-ND excluded
- **Textures** — 32x32, enhanced vanilla style
- **One EntityType per species** — variants (sex, age) handled by texture switching, not separate types
- **Test incrementally** — each phase must produce a working, testable state
