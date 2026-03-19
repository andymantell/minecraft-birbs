# British Birds Mod Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Fabric mod for Minecraft 26.1 adding 5 pilot British bird species (Robin, Blue Tit, Barn Owl, Peregrine Falcon, Mallard) with realistic models, behaviour, and sounds.

**Architecture:** Vanilla entity system with procedural animations. Entity hierarchy: `AbstractBritishBird` → `AbstractFlyingBird`/`AbstractWaterBird` → species-specific subclasses. AI composed from reusable goal classes. 32x32 textures, .ogg sounds from xeno-canto.

**Tech Stack:** Minecraft 26.1, Fabric Loader 0.18.4, Fabric API 0.143.14+26.1, Loom 1.15, Gradle 9.4.0, Java 25. No external dependencies beyond Fabric API.

**Important context:**
- MC 26.1 is **unobfuscated** — uses Mojang names, not Yarn mappings (e.g., `Animal` not `AnimalEntity`, `PathfinderMob` not `PathAwareEntity`, `MobCategory` not `SpawnGroup`)
- Loom plugin is `net.fabric.fabric-loom` (not the old `net.fabricmc.fabric-loom-remap`)
- Use `implementation` not `modImplementation` in build.gradle
- No `mappings` line needed — MC is unobfuscated
- Depend on `fabric-api` mod ID, not old `fabric`
- **No git commits** — pause after each phase for the user to commit themselves

**Design spec:** `docs/superpowers/specs/2026-03-19-british-birds-mod-design.md`
**Species research:** `docs/european_robin_reference.md`, `docs/barn_owl_reference.md`, `docs/peregrine_falcon_reference.md`, `docs/mallard_reference.md`, `docs/blue_tit_reference.md`

---

## Phase 1: Scaffold & Base Classes

**Goal:** Mod loads in Minecraft 26.1. Robin spawn egg exists. A featureless Robin entity spawns and stands in the world.

### Task 1.1: Initialize Gradle Project

**Files:**
- Create: `settings.gradle`
- Create: `gradle.properties`
- Create: `build.gradle`

- [ ] **Step 1: Create `settings.gradle`**

```groovy
pluginManagement {
    repositories {
        maven {
            name = 'Fabric'
            url = 'https://maven.fabricmc.net/'
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

- [ ] **Step 2: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

# Fabric Properties
minecraft_version=26.1
loader_version=0.18.4
fabric_version=0.143.14+26.1
loom_version=1.15.5

# Mod Properties
mod_version=0.1.0
maven_group=com.birbs
archives_base_name=britishbirds

# Java
java_version=25
```

- [ ] **Step 3: Create `build.gradle`**

```groovy
plugins {
    id 'net.fabric.fabric-loom' version "${loom_version}"
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

loom {
    splitEnvironmentSourceSets()

    mods {
        "britishbirds" {
            sourceSet sourceSets.main
            sourceSet sourceSets.client
        }
    }
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    implementation "net.fabricmc:fabric-loader:${project.loader_version}"
    implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
}

processResources {
    inputs.property "version", project.version
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = Integer.parseInt(project.java_version)
}

java {
    sourceCompatibility = JavaVersion.toVersion(project.java_version)
    targetCompatibility = JavaVersion.toVersion(project.java_version)
}
```

- [ ] **Step 4: Create directory structure**

```
src/main/java/com/birbs/britishbirds/
src/main/resources/
src/client/java/com/birbs/britishbirds/client/
src/client/resources/
```

Note: Fabric Loom's `splitEnvironmentSourceSets()` expects `src/client/` for client-side code, not `src/main/.../client/`.

- [ ] **Step 5: Verify Gradle syncs**

Run: `./gradlew --refresh-dependencies`
Expected: Successful dependency resolution. If the fabric_version is wrong, check https://fabricmc.net/develop/ for the exact string and update `gradle.properties`.

### Task 1.2: Mod Entrypoints & fabric.mod.json

**Files:**
- Create: `src/main/resources/fabric.mod.json`
- Create: `src/main/java/com/birbs/britishbirds/BritishBirdsMod.java`
- Create: `src/client/java/com/birbs/britishbirds/client/BritishBirdsClient.java`

- [ ] **Step 1: Create `fabric.mod.json`**

```json
{
    "schemaVersion": 1,
    "id": "britishbirds",
    "version": "${version}",
    "name": "British Birds",
    "description": "Adds 100 British bird species with realistic behaviour to Minecraft",
    "authors": ["birbs"],
    "license": "MIT",
    "icon": "assets/britishbirds/icon.png",
    "environment": "*",
    "entrypoints": {
        "main": ["com.birbs.britishbirds.BritishBirdsMod"],
        "client": ["com.birbs.britishbirds.client.BritishBirdsClient"]
    },
    "depends": {
        "fabricloader": ">=0.18.4",
        "minecraft": "~26.1",
        "java": ">=25",
        "fabric-api": "*"
    }
}
```

- [ ] **Step 2: Create main entrypoint**

File: `src/main/java/com/birbs/britishbirds/BritishBirdsMod.java`

```java
package com.birbs.britishbirds;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BritishBirdsMod implements ModInitializer {
    public static final String MOD_ID = "britishbirds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("British Birds mod initializing...");
    }
}
```

- [ ] **Step 3: Create client entrypoint**

File: `src/client/java/com/birbs/britishbirds/client/BritishBirdsClient.java`

```java
package com.birbs.britishbirds.client;

import net.fabricmc.api.ClientModInitializer;

public class BritishBirdsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Renderer and model layer registration will go here
    }
}
```

- [ ] **Step 4: Verify mod loads**

Run: `./gradlew runClient`
Expected: Minecraft launches. Check logs for "British Birds mod initializing..." message. No crashes.

### Task 1.3: Registration Infrastructure

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/registry/ModEntities.java`
- Create: `src/main/java/com/birbs/britishbirds/registry/ModItems.java`
- Create: `src/main/java/com/birbs/britishbirds/registry/ModSounds.java`

- [ ] **Step 1: Create `ModSounds.java`**

```java
package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    // Robin sounds
    public static final SoundEvent ROBIN_SONG = registerSound("entity.robin.song");
    public static final SoundEvent ROBIN_CALL = registerSound("entity.robin.call");
    public static final SoundEvent ROBIN_ALARM = registerSound("entity.robin.alarm");
    public static final SoundEvent ROBIN_HURT = registerSound("entity.robin.hurt");
    public static final SoundEvent ROBIN_DEATH = registerSound("entity.robin.death");

    private static SoundEvent registerSound(String id) {
        ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier,
                SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds sounds...");
    }
}
```

Note: MC 26.1 may use `ResourceLocation` or `Identifier` — Fabric provides `Identifier` as an alias. If `ResourceLocation` doesn't resolve, try `net.minecraft.resources.Identifier` or `net.minecraft.util.Identifier`. Check imports at compile time and adjust.

- [ ] **Step 2: Create `ModEntities.java`** (placeholder — Robin entity doesn't exist yet)

```java
package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;

public class ModEntities {

    // Entity types will be registered here as species are implemented

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds entities...");
    }
}
```

- [ ] **Step 3: Create `ModItems.java`** (placeholder)

```java
package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;

public class ModItems {

    // Spawn eggs will be registered here as species are implemented

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds items...");
    }
}
```

- [ ] **Step 4: Wire registration into mod entrypoint**

Update `BritishBirdsMod.java`:

```java
@Override
public void onInitialize() {
    LOGGER.info("British Birds mod initializing...");
    ModSounds.initialize();
    ModEntities.initialize();
    ModItems.initialize();
}
```

- [ ] **Step 5: Verify compiles and loads**

Run: `./gradlew runClient`
Expected: No crashes. Log shows all three registration messages.

### Task 1.4: AbstractBritishBird Base Entity

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/base/AbstractBritishBird.java`

- [ ] **Step 1: Create the base entity class**

```java
package com.birbs.britishbirds.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Root entity class for all British bird species.
 * Provides: sex tracking (for dimorphic species), basic attributes, sound hooks.
 */
public abstract class AbstractBritishBird extends Animal {

    // Tracked data for sexual dimorphism — synced to client for texture selection
    private static final EntityDataAccessor<Boolean> IS_MALE =
            SynchedEntityData.defineId(AbstractBritishBird.class, EntityDataSerializers.BOOLEAN);

    protected AbstractBritishBird(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    // --- Attributes ---

    public static AttributeSupplier.Builder createBirdAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    // --- Sex tracking ---

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_MALE, true);
    }

    public boolean isMale() {
        return this.entityData.get(IS_MALE);
    }

    public void setMale(boolean male) {
        this.entityData.set(IS_MALE, male);
    }

    /**
     * Whether this species has visible sexual dimorphism.
     * Override to return true for species like Mallard, Barn Owl, etc.
     */
    public boolean hasSexualDimorphism() {
        return false;
    }

    // --- NBT persistence ---

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsMale", this.isMale());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("IsMale")) {
            this.setMale(tag.getBoolean("IsMale"));
        }
    }

    // --- Spawning ---

    @Nullable
    @Override
    public Animal getBreedOffspring(ServerLevel level, Animal otherParent) {
        // Default: no breeding yet. Override per species if needed.
        return null;
    }

    /**
     * Called when entity first spawns. Randomize sex.
     */
    @Override
    public void finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.EntitySpawnReason spawnReason,
            @Nullable net.minecraft.world.entity.SpawnGroupData groupData) {
        // Note: Method signature may differ in 26.1. Adjust if needed.
        this.setMale(this.random.nextBoolean());
        return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
    }

    // --- Sound hooks (override per species) ---

    @Nullable
    @Override
    protected abstract SoundEvent getAmbientSound();

    @Override
    protected abstract SoundEvent getHurtSound(DamageSource source);

    @Override
    protected abstract SoundEvent getDeathSound();

    /**
     * Species-specific song sound, separate from ambient.
     * Used by BirdSoundManager for dawn chorus scheduling.
     */
    @Nullable
    public SoundEvent getSongSound() {
        return null;
    }

    /**
     * Species-specific alarm call.
     */
    @Nullable
    public SoundEvent getAlarmSound() {
        return null;
    }
}
```

Note: The `finalizeSpawn` method signature changed across MC versions. In 26.1 (unobfuscated Mojang names), it may use `MobSpawnType` instead of `EntitySpawnReason`, and may return `SpawnGroupData` instead of being void. Adjust at compile time. The key logic (randomize sex) stays the same.

- [ ] **Step 2: Verify compiles**

Run: `./gradlew build`
Expected: Compiles successfully. If any MC class names are wrong (due to 26.1 unobfuscation), fix the imports based on compiler errors. Common adjustments:
- `EntitySpawnReason` might be `MobSpawnType`
- `SpawnGroupData` might be `SpawnGroupData` or `SpawnData`
- `SynchedEntityData.defineId` signature may differ

### Task 1.5: Robin Entity (Minimal)

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/songbird/RobinEntity.java`
- Modify: `src/main/java/com/birbs/britishbirds/registry/ModEntities.java`
- Modify: `src/main/java/com/birbs/britishbirds/registry/ModItems.java`
- Modify: `src/main/java/com/birbs/britishbirds/BritishBirdsMod.java`

- [ ] **Step 1: Create minimal RobinEntity**

```java
package com.birbs.britishbirds.entity.songbird;

import com.birbs.britishbirds.entity.base.AbstractBritishBird;
import com.birbs.britishbirds.registry.ModSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

/**
 * European Robin (Erithacus rubecula).
 * Phase 1: Minimal entity that stands in the world.
 * Full behaviour added in Phase 3.
 */
public class RobinEntity extends AbstractBritishBird {

    public RobinEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractBritishBird.createBirdAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 4.0)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        // Minimal goals for Phase 1 — just wander and look around
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    // --- Sounds ---

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ROBIN_SONG;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ROBIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBIN_DEATH;
    }

    @Override
    public SoundEvent getSongSound() {
        return ModSounds.ROBIN_SONG;
    }

    @Override
    public SoundEvent getAlarmSound() {
        return ModSounds.ROBIN_ALARM;
    }
}
```

- [ ] **Step 2: Register Robin entity type in `ModEntities.java`**

```java
package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.entity.songbird.RobinEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<RobinEntity> ROBIN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin"),
            EntityType.Builder.<RobinEntity>of(RobinEntity::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f)
                    .clientTrackingRange(10)
                    .build("robin")
    );

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds entities...");
    }
}
```

Note: The `EntityType.Builder` API may use `.of()` or `.create()` in 26.1. If `.of()` doesn't compile, try `.create()`. The `.<RobinEntity>of()` generic syntax ensures type safety.

- [ ] **Step 3: Register Robin spawn egg in `ModItems.java`**

```java
package com.birbs.britishbirds.registry;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {

    // Robin: orange-red breast (0xD4602A) + olive-brown back (0x6B6B3A)
    public static final Item ROBIN_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin_spawn_egg"),
            new SpawnEggItem(ModEntities.ROBIN, 0xD4602A, 0x6B6B3A, new Item.Properties())
    );

    public static void initialize() {
        BritishBirdsMod.LOGGER.info("Registering British Birds items...");
    }
}
```

Note: If `SpawnEggItem` constructor signature has changed in 26.1 (e.g., requires `RegistryKey`), adjust accordingly. The two int args are the primary and secondary egg colours.

- [ ] **Step 4: Register Robin attributes in `BritishBirdsMod.java`**

```java
@Override
public void onInitialize() {
    LOGGER.info("British Birds mod initializing...");
    ModSounds.initialize();
    ModEntities.initialize();
    ModItems.initialize();

    // Register entity attributes
    FabricDefaultAttributeRegistry.register(ModEntities.ROBIN, RobinEntity.createAttributes());
}
```

Add imports:
```java
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import com.birbs.britishbirds.entity.songbird.RobinEntity;
```

- [ ] **Step 5: Create placeholder sound files and sounds.json**

Create empty .ogg placeholder files (1 second of silence) so the game doesn't crash when trying to play Robin sounds. These will be replaced with real xeno-canto recordings in Phase 3.

File: `src/main/resources/assets/britishbirds/sounds.json`
```json
{
    "entity.robin.song": {
        "subtitle": "subtitles.britishbirds.entity.robin.song",
        "sounds": [
            "britishbirds:robin/song1"
        ]
    },
    "entity.robin.call": {
        "subtitle": "subtitles.britishbirds.entity.robin.call",
        "sounds": [
            "britishbirds:robin/call"
        ]
    },
    "entity.robin.alarm": {
        "subtitle": "subtitles.britishbirds.entity.robin.alarm",
        "sounds": [
            "britishbirds:robin/alarm"
        ]
    },
    "entity.robin.hurt": {
        "sounds": [
            "britishbirds:robin/hurt"
        ]
    },
    "entity.robin.death": {
        "sounds": [
            "britishbirds:robin/death"
        ]
    }
}
```

Generate silence .ogg files:
```bash
# Requires ffmpeg installed
for name in song1 call alarm hurt death; do
    ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 0.5 -c:a libvorbis "src/main/resources/assets/britishbirds/sounds/robin/${name}.ogg"
done
```

- [ ] **Step 6: Create lang file**

File: `src/main/resources/assets/britishbirds/lang/en_us.json`
```json
{
    "entity.britishbirds.robin": "European Robin",
    "item.britishbirds.robin_spawn_egg": "Robin Spawn Egg",
    "subtitles.britishbirds.entity.robin.song": "Robin sings",
    "subtitles.britishbirds.entity.robin.call": "Robin calls",
    "subtitles.britishbirds.entity.robin.alarm": "Robin alarm call"
}
```

### Task 1.6: Robin Model & Renderer (Placeholder)

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/model/BirdModelLayers.java`
- Create: `src/client/java/com/birbs/britishbirds/client/model/RobinModel.java`
- Create: `src/client/java/com/birbs/britishbirds/client/renderer/RobinRenderer.java`
- Create: `src/client/java/com/birbs/britishbirds/client/renderer/RobinRenderState.java`
- Modify: `src/client/java/com/birbs/britishbirds/client/BritishBirdsClient.java`
- Create: `src/main/resources/assets/britishbirds/textures/entity/robin/robin.png` (placeholder)

- [ ] **Step 1: Create `BirdModelLayers.java`**

```java
package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.BritishBirdsMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * Central registry of all bird model layer locations.
 */
public class BirdModelLayers {
    public static final ModelLayerLocation ROBIN =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(BritishBirdsMod.MOD_ID, "robin"),
                    "main"
            );
}
```

- [ ] **Step 2: Create `RobinRenderState.java`**

```java
package com.birbs.britishbirds.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state for Robin — carries data from entity to renderer each frame.
 * Will be expanded with animation state in later phases.
 */
public class RobinRenderState extends LivingEntityRenderState {
    public boolean isMale = true;
    public boolean isBaby = false;
}
```

- [ ] **Step 3: Create placeholder `RobinModel.java`**

```java
package com.birbs.britishbirds.client.model;

import com.birbs.britishbirds.client.renderer.RobinRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * European Robin model.
 * Phase 1: Simple placeholder geometry (body + head + beak + wings + tail + legs).
 * Dimensions based on real robin: ~14cm body, round and plump.
 * Scale: roughly 5-6 pixels tall at 32x32 texture.
 */
public class RobinModel extends EntityModel<RobinRenderState> {

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public RobinModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.tail = root.getChild("tail");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDef = new MeshDefinition();
        PartDefinition root = meshDef.getRoot();

        // Body — round, plump (the robin's distinctive shape)
        // 4x4x5 pixels, positioned so feet touch ground at y=24
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0f, -2.5f, -2.5f, 4.0f, 5.0f, 5.0f),
                PartPose.offset(0.0f, 19.0f, 0.0f));

        // Head — large, round, sits atop body
        // 3x3x3 pixels
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-1.5f, -3.0f, -1.5f, 3.0f, 3.0f, 3.0f),
                PartPose.offset(0.0f, 16.5f, -1.0f));

        // Beak — small, thin, insectivore-style
        root.getChild("head").addOrReplaceChild("beak",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-0.5f, -0.5f, -2.0f, 1.0f, 0.5f, 2.0f),
                PartPose.offset(0.0f, -1.5f, -1.5f));

        // Left wing
        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(18, 0)
                        .addBox(0.0f, -1.0f, -1.0f, 1.0f, 3.0f, 4.0f),
                PartPose.offset(2.0f, 18.0f, 0.0f));

        // Right wing (mirrored)
        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(18, 0).mirror()
                        .addBox(-1.0f, -1.0f, -1.0f, 1.0f, 3.0f, 4.0f),
                PartPose.offset(-2.0f, 18.0f, 0.0f));

        // Tail — short, slightly cocked upward
        root.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(18, 7)
                        .addBox(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 18.5f, 2.5f, -0.2618f, 0.0f, 0.0f));
                // -15 degrees pitch = slightly cocked upward

        // Left leg — thin, pinkish
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(1.0f, 21.0f, 0.0f));

        // Right leg
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(-1.0f, 21.0f, 0.0f));

        return LayerDefinition.create(meshDef, 32, 32);
    }

    @Override
    public void setupAnim(RobinRenderState state) {
        super.setupAnim(state);
        // Phase 1: No animations yet. Will add in Phase 2-3.
    }
}
```

- [ ] **Step 4: Create `RobinRenderer.java`**

```java
package com.birbs.britishbirds.client.renderer;

import com.birbs.britishbirds.BritishBirdsMod;
import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.RobinModel;
import com.birbs.britishbirds.entity.songbird.RobinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RobinRenderer extends MobRenderer<RobinEntity, RobinRenderState, RobinModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BritishBirdsMod.MOD_ID,
                    "textures/entity/robin/robin.png");
    private static final ResourceLocation TEXTURE_JUVENILE =
            ResourceLocation.fromNamespaceAndPath(BritishBirdsMod.MOD_ID,
                    "textures/entity/robin/robin_juvenile.png");

    public RobinRenderer(EntityRendererProvider.Context context) {
        super(context, new RobinModel(context.bakeLayer(BirdModelLayers.ROBIN)), 0.3f);
    }

    @Override
    public RobinRenderState createRenderState() {
        return new RobinRenderState();
    }

    @Override
    public void extractRenderState(RobinEntity entity, RobinRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isMale = entity.isMale();
        state.isBaby = entity.isBaby();
    }

    @Override
    public ResourceLocation getTextureLocation(RobinRenderState state) {
        // Robin has no sexual dimorphism — same texture for both sexes
        // Juvenile texture when baby
        return state.isBaby ? TEXTURE_JUVENILE : TEXTURE;
    }
}
```

Note: The `MobRenderer` generic signature in 26.1 may be `MobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<S>>`. If the compiler complains about generics, adjust the type parameters. The `extractRenderState` method replaces the old `getTextureLocation(Entity)` pattern — it copies entity data into the render state each frame.

- [ ] **Step 5: Register renderer and model layer in `BritishBirdsClient.java`**

```java
package com.birbs.britishbirds.client;

import com.birbs.britishbirds.client.model.BirdModelLayers;
import com.birbs.britishbirds.client.model.RobinModel;
import com.birbs.britishbirds.client.renderer.RobinRenderer;
import com.birbs.britishbirds.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class BritishBirdsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register model layers
        EntityModelLayerRegistry.registerModelLayer(
                BirdModelLayers.ROBIN,
                RobinModel::getTexturedModelData
        );

        // Register entity renderers
        EntityRenderers.register(ModEntities.ROBIN, RobinRenderer::new);
    }
}
```

- [ ] **Step 6: Create placeholder Robin texture**

Create a 32x32 pixel PNG at `src/main/resources/assets/britishbirds/textures/entity/robin/robin.png`.

For the placeholder, create a simple coloured texture:
- Body area (0,0 to 18,10): olive-brown (#6B6B3A)
- Head area (0,10 to 9,16): olive-brown upper, orange-red (#D4602A) lower face area
- Wing area (18,0 to 28,7): dark brown (#4A3B2A)
- Tail area (18,7 to 28,10): dark brown
- Leg area (0,19 to 4,23): pinkish-brown (#C4A882)

Also create `robin_juvenile.png` — same layout but mottled brown tones (#8B7355) without the orange breast.

This can be a very rough placeholder — it just needs to be 32x32 and have the UV regions mapped correctly. We'll refine textures in Phase 3.

- [ ] **Step 7: Verify Robin spawns in-game**

Run: `./gradlew runClient`

1. Create a new creative mode world
2. Open inventory, search for "Robin"
3. Find and use the Robin Spawn Egg
4. Robin entity should appear in the world — small bird shape, coloured texture, wanders around, looks at player

Expected: Entity spawns without crashing. Model is visible (even if rough). Entity wanders using basic AI.

**--- PHASE 1 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## Phase 2: Flight & Movement Foundation

**Goal:** Robin hops on the ground, flies between nearby perches, and sits on fence posts/logs.

### Task 2.1: AbstractFlyingBird

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/base/AbstractFlyingBird.java`

- [ ] **Step 1: Create `AbstractFlyingBird`**

Extends `AbstractBritishBird`. Adds flight navigation using Minecraft's `FlyingPathNavigation`, a state machine for grounded vs flying, and basic takeoff/landing logic.

```java
package com.birbs.britishbirds.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for birds that can fly.
 * Provides flight navigation, takeoff/landing state management.
 */
public abstract class AbstractFlyingBird extends AbstractBritishBird {

    private boolean isFlying = false;

    protected AbstractFlyingBird(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createFlyingBirdAttributes() {
        return AbstractBritishBird.createBirdAttributes()
                .add(Attributes.FLYING_SPEED, 0.4);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    // --- Flight state ---

    public boolean isFlying() {
        return this.isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    @Override
    public void tick() {
        super.tick();
        // Auto-detect flying state: if not on ground, we're flying
        if (!this.onGround() && !this.isInWater()) {
            if (!this.isFlying) {
                this.isFlying = true;
            }
        } else {
            if (this.isFlying) {
                this.isFlying = false;
            }
        }
    }

    // Flying birds don't take fall damage
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier,
                                    net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround,
                                    BlockState state, BlockPos pos) {
        // No fall damage for flying birds
    }

    // Apply gravity reduction when flying — birds glide, not plummet
    @Override
    public void travel(Vec3 movementInput) {
        if (this.isFlying() && !this.onGround()) {
            // Reduced gravity while flying
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.01, 0));
        }
        super.travel(movementInput);
    }
}
```

- [ ] **Step 2: Make RobinEntity extend AbstractFlyingBird instead of AbstractBritishBird**

Update `RobinEntity.java` — change `extends AbstractBritishBird` to `extends AbstractFlyingBird` (it will compile through `AbstractFlyingBird` → `AbstractBritishBird` → `Animal`). Update `createAttributes()` to use `createFlyingBirdAttributes()`:

```java
public static AttributeSupplier.Builder createAttributes() {
    return AbstractFlyingBird.createFlyingBirdAttributes()
            .add(Attributes.MAX_HEALTH, 4.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FLYING_SPEED, 0.35);
}
```

- [ ] **Step 3: Verify Robin can navigate in air**

Run: `./gradlew runClient`
Spawn Robin on a hill — it should be able to navigate off edges without fall damage and glide/flutter down.

### Task 2.2: Hopping Movement Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/movement/HoppingMovementGoal.java`

- [ ] **Step 1: Create `HoppingMovementGoal`**

Small birds hop rather than walk. This goal replaces smooth walking with a hop-pause-hop pattern.

```java
package com.birbs.britishbirds.ai.movement;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Makes a bird hop along the ground instead of walking smoothly.
 * Hop-pause-hop with brief pauses between hops. Characteristic of small passerines.
 */
public class HoppingMovementGoal extends Goal {

    private final AbstractFlyingBird bird;
    private final double hopSpeed;
    private final double hopHeight;
    private int hopCooldown = 0;
    private int pauseTicks = 0;
    private static final int MIN_PAUSE = 5;   // ticks between hops
    private static final int MAX_PAUSE = 20;

    public HoppingMovementGoal(AbstractFlyingBird bird, double hopSpeed, double hopHeight) {
        this.bird = bird;
        this.hopSpeed = hopSpeed;
        this.hopHeight = hopHeight;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // Active when bird is on the ground, not flying, and wants to move
        return this.bird.onGround() && !this.bird.isFlying()
                && this.bird.getNavigation().isInProgress();
    }

    @Override
    public void tick() {
        if (this.hopCooldown > 0) {
            this.hopCooldown--;
            // During pause, bird stands still (the characteristic pause)
            this.bird.setDeltaMovement(this.bird.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            return;
        }

        if (this.bird.onGround() && this.bird.getNavigation().isInProgress()) {
            // Execute hop: small upward + forward boost
            Vec3 forward = this.bird.getForward().normalize().scale(this.hopSpeed);
            this.bird.setDeltaMovement(forward.x, this.hopHeight, forward.z);
            this.bird.hasImpulse = true;

            // Set cooldown for next hop
            this.hopCooldown = MIN_PAUSE + this.bird.getRandom().nextInt(MAX_PAUSE - MIN_PAUSE);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
}
```

- [ ] **Step 2: Add to RobinEntity's `registerGoals()`**

```java
this.goalSelector.addGoal(5, new HoppingMovementGoal(this, 0.15, 0.3));
```

- [ ] **Step 3: Verify hopping in-game**

Robin should now hop in short bursts rather than glide-walking. It should look like hop-pause-hop.

### Task 2.3: Perching Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/movement/PerchingGoal.java`

- [ ] **Step 1: Create `PerchingGoal`**

Bird finds a suitable perch block (fence, fence gate, wall, log) and flies/hops to sit on it. Once perched, stays for a while with idle behaviour.

```java
package com.birbs.britishbirds.ai.movement;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;

import java.util.EnumSet;

/**
 * Bird finds a fence post, wall, log, or similar block and perches on top.
 * Stays perched for a configurable duration before resuming normal behaviour.
 */
public class PerchingGoal extends Goal {

    private final AbstractFlyingBird bird;
    private final int searchRadius;
    private final int minPerchTicks;
    private final int maxPerchTicks;
    private BlockPos targetPerch = null;
    private int perchTimer = 0;
    private int cooldown = 0;

    public PerchingGoal(AbstractFlyingBird bird, int searchRadius, int minPerchTicks, int maxPerchTicks) {
        this.bird = bird;
        this.searchRadius = searchRadius;
        this.minPerchTicks = minPerchTicks;
        this.maxPerchTicks = maxPerchTicks;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // 5% chance per tick to look for a perch (don't search every tick)
        if (this.bird.getRandom().nextFloat() > 0.05f) {
            return false;
        }
        this.targetPerch = findNearbyPerch();
        return this.targetPerch != null;
    }

    @Override
    public void start() {
        if (this.targetPerch != null) {
            // Navigate to the top of the perch block
            this.bird.getNavigation().moveTo(
                    this.targetPerch.getX() + 0.5,
                    this.targetPerch.getY() + 1.0,
                    this.targetPerch.getZ() + 0.5,
                    1.0);
        }
        this.perchTimer = this.minPerchTicks
                + this.bird.getRandom().nextInt(this.maxPerchTicks - this.minPerchTicks);
    }

    @Override
    public void tick() {
        if (this.targetPerch == null) return;

        // Check if we've arrived at the perch
        double dist = this.bird.distanceToSqr(
                this.targetPerch.getX() + 0.5,
                this.targetPerch.getY() + 1.0,
                this.targetPerch.getZ() + 0.5);

        if (dist < 1.0) {
            // We're on the perch — stay still
            this.bird.getNavigation().stop();
            this.bird.setDeltaMovement(0, this.bird.getDeltaMovement().y, 0);
            this.perchTimer--;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPerch != null && this.perchTimer > 0;
    }

    @Override
    public void stop() {
        this.targetPerch = null;
        this.cooldown = 100 + this.bird.getRandom().nextInt(200); // 5-15 seconds before perching again
    }

    private BlockPos findNearbyPerch() {
        BlockPos birdPos = this.bird.blockPosition();
        BlockPos bestPerch = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -2; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = birdPos.offset(x, y, z);
                    if (isPerchable(pos)) {
                        double dist = birdPos.distSqr(pos);
                        if (dist < bestDist) {
                            bestDist = dist;
                            bestPerch = pos;
                        }
                    }
                }
            }
        }
        return bestPerch;
    }

    private boolean isPerchable(BlockPos pos) {
        BlockState state = this.bird.level().getBlockState(pos);
        BlockState above = this.bird.level().getBlockState(pos.above());

        // Block must be a fence, wall, or log-type, with air above
        boolean isSuitableBlock = state.getBlock() instanceof FenceBlock
                || state.getBlock() instanceof FenceGateBlock
                || state.getBlock() instanceof WallBlock
                || state.is(BlockTags.LOGS);

        return isSuitableBlock && above.isAir();
    }
}
```

- [ ] **Step 2: Add to RobinEntity's `registerGoals()`**

```java
// Priority 3 — between panic and wandering
this.goalSelector.addGoal(3, new PerchingGoal(this, 8, 60, 200));
```

- [ ] **Step 3: Verify perching in-game**

Place fence posts near a Robin. It should occasionally fly/hop up onto a fence post and sit there for a few seconds before resuming activity.

### Task 2.4: Fluttering Flight Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/flight/FlutteringFlightGoal.java`

- [ ] **Step 1: Create `FlutteringFlightGoal`**

Short, low flights between nearby positions. The bird picks a spot 3-10 blocks away, flies there in a slightly undulating path, and lands.

```java
package com.birbs.britishbirds.ai.flight;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Short, low, fluttery flights between nearby points.
 * Characteristic of small passerines (robin, tits, sparrows).
 * Flight is slightly undulating — rapid wingbeats then brief partial fold.
 */
public class FlutteringFlightGoal extends Goal {

    private final AbstractFlyingBird bird;
    private final double speed;
    private final int maxFlightDistance;
    private final int minFlightDistance;
    private int cooldown = 0;
    private Vec3 targetPos = null;

    public FlutteringFlightGoal(AbstractFlyingBird bird, double speed,
                                 int minFlightDistance, int maxFlightDistance) {
        this.bird = bird;
        this.speed = speed;
        this.minFlightDistance = minFlightDistance;
        this.maxFlightDistance = maxFlightDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // 3% chance per tick to initiate a short flight
        if (this.bird.getRandom().nextFloat() > 0.03f) {
            return false;
        }
        // Only fly when on ground or perched
        if (!this.bird.onGround()) {
            return false;
        }

        this.targetPos = findFlightTarget();
        return this.targetPos != null;
    }

    @Override
    public void start() {
        if (this.targetPos != null) {
            this.bird.setFlying(true);
            this.bird.getNavigation().moveTo(
                    this.targetPos.x, this.targetPos.y, this.targetPos.z, this.speed);
        }
    }

    @Override
    public void tick() {
        // Add slight undulation to flight (sinusoidal y-offset)
        if (this.bird.isFlying() && !this.bird.onGround()) {
            Vec3 motion = this.bird.getDeltaMovement();
            double undulation = Math.sin(this.bird.tickCount * 0.5) * 0.02;
            this.bird.setDeltaMovement(motion.x, motion.y + undulation, motion.z);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.isFlying() && !this.bird.onGround()
                && this.bird.getNavigation().isInProgress();
    }

    @Override
    public void stop() {
        this.bird.setFlying(false);
        this.targetPos = null;
        this.cooldown = 40 + this.bird.getRandom().nextInt(80); // 2-6 seconds
    }

    private Vec3 findFlightTarget() {
        // Find a random position nearby at roughly the same height or slightly higher
        Vec3 pos = LandRandomPos.getPos(this.bird, this.maxFlightDistance, 3);
        if (pos == null) return null;

        double dist = this.bird.position().distanceTo(pos);
        if (dist < this.minFlightDistance) return null;

        return pos;
    }
}
```

- [ ] **Step 2: Add to RobinEntity**

```java
this.goalSelector.addGoal(4, new FlutteringFlightGoal(this, 1.0, 3, 10));
```

- [ ] **Step 3: Verify flight in-game**

Robin should occasionally take short flights between nearby positions — low, quick, slightly bouncy. It should land on the ground or on blocks and resume hopping.

### Task 2.5: Add Flight Animation to Model

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/renderer/RobinRenderState.java`
- Modify: `src/client/java/com/birbs/britishbirds/client/model/RobinModel.java`
- Modify: `src/client/java/com/birbs/britishbirds/client/renderer/RobinRenderer.java`

- [ ] **Step 1: Add animation state to `RobinRenderState`**

```java
public class RobinRenderState extends LivingEntityRenderState {
    public boolean isMale = true;
    public boolean isBaby = false;
    public boolean isFlying = false;
    public float flapAngle = 0.0f;  // Current wing flap angle
    public float headPitch = 0.0f;
    public float headYaw = 0.0f;
}
```

- [ ] **Step 2: Update `RobinRenderer.extractRenderState`**

```java
@Override
public void extractRenderState(RobinEntity entity, RobinRenderState state, float partialTick) {
    super.extractRenderState(entity, state, partialTick);
    state.isMale = entity.isMale();
    state.isBaby = entity.isBaby();
    state.isFlying = entity instanceof AbstractFlyingBird fb && fb.isFlying();

    // Wing flap: sinusoidal based on tick + partial tick
    float time = entity.tickCount + partialTick;
    if (state.isFlying) {
        // Fast flapping when flying (~14Hz, the real robin wingbeat frequency)
        state.flapAngle = (float) Math.sin(time * 1.4) * 1.2f;
    } else {
        // Wings folded when on ground
        state.flapAngle = 0.0f;
    }
}
```

Add import: `import com.birbs.britishbirds.entity.base.AbstractFlyingBird;`

- [ ] **Step 3: Add wing animation to `RobinModel.setupAnim`**

```java
@Override
public void setupAnim(RobinRenderState state) {
    super.setupAnim(state);

    if (state.isFlying) {
        // Wings flap — rotate around Z axis
        this.leftWing.zRot = -state.flapAngle;
        this.rightWing.zRot = state.flapAngle;
        // Slight body bob during flight
        this.body.y = 19.0f + (float) Math.sin(state.flapAngle) * 0.3f;
    } else {
        // Wings folded against body
        this.leftWing.zRot = 0.0f;
        this.rightWing.zRot = 0.0f;
        this.body.y = 19.0f;
    }
}
```

- [ ] **Step 4: Verify wing animation in-game**

Spawn Robin, watch it take a short flight. Wings should flap rapidly during flight and fold when it lands.

**--- PHASE 2 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## Phase 3: Robin Complete

**Goal:** Fully functional Robin with all species-specific behaviours, refined model, real textures, real sounds.

### Task 3.1: SmallPasserineEntity Base Class

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/songbird/SmallPasserineEntity.java`
- Modify: `src/main/java/com/birbs/britishbirds/entity/songbird/RobinEntity.java`

- [ ] **Step 1: Create `SmallPasserineEntity`**

Common base for all small songbirds. Composes hopping, fluttering flight, perching, and ground foraging. Takes config params for species variation.

```java
package com.birbs.britishbirds.entity.songbird;

import com.birbs.britishbirds.ai.flight.FlutteringFlightGoal;
import com.birbs.britishbirds.ai.movement.HoppingMovementGoal;
import com.birbs.britishbirds.ai.movement.PerchingGoal;
import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base class for small passerines (songbirds).
 * Shared behaviour: hopping, short fluttery flights, perching, ground foraging, song.
 */
public abstract class SmallPasserineEntity extends AbstractFlyingBird {

    protected SmallPasserineEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createSmallPasserineAttributes() {
        return AbstractFlyingBird.createFlyingBirdAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.35);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(3, new PerchingGoal(this, 8, 60, 200));
        this.goalSelector.addGoal(4, new FlutteringFlightGoal(this, 1.0, 3, 10));
        this.goalSelector.addGoal(5, new HoppingMovementGoal(this, 0.15, 0.3));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Subclasses add species-specific goals
        registerSpeciesGoals();
    }

    /**
     * Override to add species-specific goals (territorial, follow player, flocking, etc.)
     */
    protected abstract void registerSpeciesGoals();
}
```

- [ ] **Step 2: Refactor RobinEntity to extend SmallPasserineEntity**

```java
public class RobinEntity extends SmallPasserineEntity {

    public RobinEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SmallPasserineEntity.createSmallPasserineAttributes();
        // Robin uses default small passerine attributes
    }

    @Override
    protected void registerSpeciesGoals() {
        // Robin-specific goals added in subsequent tasks
    }

    // ... sound methods unchanged
}
```

### Task 3.2: Ground Foraging Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/feeding/GroundForagingGoal.java`

- [ ] **Step 1: Create `GroundForagingGoal`**

Bird hops to a spot, looks down, pecks at the ground. Purely visual/behavioural — no actual item pickup (yet).

```java
package com.birbs.britishbirds.ai.feeding;

import com.birbs.britishbirds.entity.base.AbstractBritishBird;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.tags.BlockTags;

import java.util.EnumSet;

/**
 * Bird hops around pecking at the ground searching for food.
 * Visually: move to spot, pause, peck animation, move on.
 */
public class GroundForagingGoal extends Goal {

    private final AbstractBritishBird bird;
    private int forageTicks = 0;
    private int peckTimer = 0;
    private boolean isPecking = false;

    public GroundForagingGoal(AbstractBritishBird bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 2% chance per tick, only on ground during daytime
        return this.bird.onGround()
                && this.bird.getRandom().nextFloat() < 0.02f
                && this.bird.level().isDay();
    }

    @Override
    public void start() {
        this.forageTicks = 60 + this.bird.getRandom().nextInt(120); // 3-9 seconds
        this.peckTimer = 10 + this.bird.getRandom().nextInt(20);
    }

    @Override
    public void tick() {
        this.forageTicks--;
        this.peckTimer--;

        if (this.peckTimer <= 0) {
            // Peck at the ground (head bob down)
            this.isPecking = true;
            this.peckTimer = 15 + this.bird.getRandom().nextInt(25);
            // Stop moving briefly during peck
            this.bird.getNavigation().stop();
        } else {
            this.isPecking = false;
        }
    }

    public boolean isPecking() {
        return this.isPecking;
    }

    @Override
    public boolean canContinueToUse() {
        return this.forageTicks > 0 && this.bird.onGround();
    }

    @Override
    public void stop() {
        this.isPecking = false;
    }
}
```

- [ ] **Step 2: Add to `SmallPasserineEntity.registerGoals()`**

Add before the wander goal:
```java
this.goalSelector.addGoal(5, new GroundForagingGoal(this));
```

Adjust wander goal priority to 7 (shift others down).

### Task 3.3: Follow Digging Player Goal (Robin Special)

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/feeding/FollowDiggingPlayerGoal.java`

- [ ] **Step 1: Create `FollowDiggingPlayerGoal`**

Robin follows a player who is using a hoe or shovel — mimicking the real behaviour of following gardeners.

```java
package com.birbs.britishbirds.ai.feeding;

import com.birbs.britishbirds.entity.base.AbstractFlyingBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

/**
 * Robin follows a player who is holding or recently used a hoe or shovel.
 * Mimics the real robin behaviour of following gardeners to catch exposed worms.
 */
public class FollowDiggingPlayerGoal extends Goal {

    private final AbstractFlyingBird bird;
    private final double followDistance;
    private final double speed;
    private Player targetPlayer = null;
    private int lostInterestTimer = 0;

    public FollowDiggingPlayerGoal(AbstractFlyingBird bird, double followDistance, double speed) {
        this.bird = bird;
        this.followDistance = followDistance;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        List<Player> nearbyPlayers = this.bird.level().getEntitiesOfClass(
                Player.class, this.bird.getBoundingBox().inflate(this.followDistance));

        for (Player player : nearbyPlayers) {
            if (isHoldingDiggingTool(player)) {
                this.targetPlayer = player;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.lostInterestTimer = 0;
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        // Look at the player
        this.bird.getLookControl().setLookAt(this.targetPlayer, 30.0f, 30.0f);

        // Follow at 2-4 block distance
        double dist = this.bird.distanceTo(this.targetPlayer);
        if (dist > 4.0) {
            this.bird.getNavigation().moveTo(this.targetPlayer, this.speed);
        } else if (dist < 2.0) {
            this.bird.getNavigation().stop();
        }

        // Lose interest if player stops holding tool
        if (!isHoldingDiggingTool(this.targetPlayer)) {
            this.lostInterestTimer++;
        } else {
            this.lostInterestTimer = 0;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPlayer != null
                && this.targetPlayer.isAlive()
                && this.bird.distanceTo(this.targetPlayer) < this.followDistance * 1.5
                && this.lostInterestTimer < 100; // 5 seconds grace period
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
    }

    private boolean isHoldingDiggingTool(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return isDiggingTool(mainHand) || isDiggingTool(offHand);
    }

    private boolean isDiggingTool(ItemStack stack) {
        return stack.getItem() instanceof HoeItem
                || stack.getItem() instanceof ShovelItem;
    }
}
```

- [ ] **Step 2: Add to Robin's `registerSpeciesGoals()`**

```java
@Override
protected void registerSpeciesGoals() {
    this.goalSelector.addGoal(2, new FollowDiggingPlayerGoal(this, 12.0, 1.2));
}
```

- [ ] **Step 3: Verify — hold a hoe near a Robin**

Robin should approach and follow the player, staying 2-4 blocks away. When you put the hoe away, it should lose interest after ~5 seconds.

### Task 3.4: Territorial Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/social/TerritorialGoal.java`

- [ ] **Step 1: Create `TerritorialGoal`**

Robin claims territory, chases other robins away, and sings from exposed perches.

```java
package com.birbs.britishbirds.ai.social;

import com.birbs.britishbirds.entity.base.AbstractBritishBird;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Territorial behaviour — chase away others of the same species within territory radius.
 * Based on real robin behaviour: 10% of adult mortality from territorial fights.
 */
public class TerritorialGoal extends Goal {

    private final AbstractBritishBird bird;
    private final double territoryRadius;
    private final double chaseSpeed;
    private AbstractBritishBird intruder = null;
    private int chaseTicks = 0;

    public TerritorialGoal(AbstractBritishBird bird, double territoryRadius, double chaseSpeed) {
        this.bird = bird;
        this.territoryRadius = territoryRadius;
        this.chaseSpeed = chaseSpeed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.bird.isBaby()) return false;

        // Look for same-species intruders
        AABB territory = this.bird.getBoundingBox().inflate(this.territoryRadius);
        List<? extends AbstractBritishBird> nearby = this.bird.level().getEntitiesOfClass(
                this.bird.getClass(), territory,
                e -> e != this.bird && !e.isBaby());

        if (!nearby.isEmpty()) {
            this.intruder = nearby.getFirst();
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        this.chaseTicks = 100 + this.bird.getRandom().nextInt(100); // 5-10 seconds
    }

    @Override
    public void tick() {
        if (this.intruder == null) return;

        this.chaseTicks--;
        this.bird.getLookControl().setLookAt(this.intruder, 30.0f, 30.0f);
        this.bird.getNavigation().moveTo(this.intruder, this.chaseSpeed);

        // If close enough, the intruder gets scared and runs
        if (this.bird.distanceTo(this.intruder) < 2.0) {
            // Push intruder away
            double dx = this.intruder.getX() - this.bird.getX();
            double dz = this.intruder.getZ() - this.bird.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) {
                this.intruder.push(dx / len * 0.3, 0.1, dz / len * 0.3);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.intruder != null
                && this.intruder.isAlive()
                && this.chaseTicks > 0
                && this.bird.distanceTo(this.intruder) < this.territoryRadius * 2;
    }

    @Override
    public void stop() {
        this.intruder = null;
        this.bird.getNavigation().stop();
    }
}
```

- [ ] **Step 2: Add to Robin's `registerSpeciesGoals()`**

```java
this.goalSelector.addGoal(2, new TerritorialGoal(this, 16.0, 1.3));
```

### Task 3.5: Refine Robin Model

**Files:**
- Modify: `src/client/java/com/birbs/britishbirds/client/model/RobinModel.java`

- [ ] **Step 1: Add idle animations to `setupAnim`**

Expand `setupAnim` with tail bob, head tilt, and pecking (foraging) animations:

```java
@Override
public void setupAnim(RobinRenderState state) {
    super.setupAnim(state);

    float time = state.ageInTicks;

    if (state.isFlying) {
        // Wing flap
        this.leftWing.zRot = -state.flapAngle;
        this.rightWing.zRot = state.flapAngle;
        // Legs tucked up in flight
        this.leftLeg.xRot = 0.5f;
        this.rightLeg.xRot = 0.5f;
        // Body tilts forward slightly in flight
        this.body.xRot = -0.2f;
    } else {
        // Wings folded
        this.leftWing.zRot = 0.0f;
        this.rightWing.zRot = 0.0f;
        this.leftLeg.xRot = 0.0f;
        this.rightLeg.xRot = 0.0f;
        this.body.xRot = 0.0f;

        // Tail bob — gentle oscillation (characteristic robin behaviour)
        this.tail.xRot = -0.2618f + (float) Math.sin(time * 0.3) * 0.1f;

        // Occasional head tilt (curiosity)
        if ((int) time % 80 < 20) {
            this.head.zRot = 0.15f; // Tilt right
        } else if ((int) time % 80 < 40) {
            this.head.zRot = -0.15f; // Tilt left
        } else {
            this.head.zRot = 0.0f;
        }

        // Walking/hopping leg animation
        if (state.walkAnimationSpeed > 0.01f) {
            float legSwing = (float) Math.sin(time * 0.8) * 0.4f * state.walkAnimationSpeed;
            this.leftLeg.xRot = legSwing;
            this.rightLeg.xRot = -legSwing;
        }
    }
}
```

### Task 3.6: Robin Textures

**Files:**
- Create/replace: `src/main/resources/assets/britishbirds/textures/entity/robin/robin.png`
- Create/replace: `src/main/resources/assets/britishbirds/textures/entity/robin/robin_juvenile.png`

- [ ] **Step 1: Create detailed Robin adult texture**

32x32 PNG. UV map matches the model parts defined in `RobinModel.getTexturedModelData()`. Key colours from research:

- Crown/nape/back: olive-brown (#6B6B3A)
- Forehead/face/breast: vivid orange-red (#D4602A)
- Blue-grey border around breast: (#8B9DAF)
- Belly: off-white (#F0EDE0)
- Flanks: warm buff (#C4A882)
- Wings: dark brown with olive fringes (#4A3B2A)
- Tail: dark brown (#4A3B2A)
- Bill: dark brown (#332211)
- Legs: pinkish-brown (#C4A882)
- Eye: large black bead (#111111)

Generate programmatically or paint pixel by pixel. The texture must be mapped to the UV coordinates defined in the model.

- [ ] **Step 2: Create juvenile texture**

Same UV layout. Colours: mottled speckled brown throughout (#8B7355 base with #6B5B3A and #A89070 spots). No orange breast. Golden-buff spotting on chest and back.

- [ ] **Step 3: Verify textures render correctly in-game**

Spawn adult and baby robins. Adult should show orange breast. Baby should show speckled brown. Check that UV mapping aligns — colours should appear on the correct body parts.

### Task 3.7: Robin Sounds (from xeno-canto)

**Files:**
- Replace: `src/main/resources/assets/britishbirds/sounds/robin/song1.ogg`
- Create: `src/main/resources/assets/britishbirds/sounds/robin/song2.ogg`
- Replace: `src/main/resources/assets/britishbirds/sounds/robin/call.ogg`
- Replace: `src/main/resources/assets/britishbirds/sounds/robin/alarm.ogg`
- Create: `src/main/resources/assets/britishbirds/sounds/robin/hurt.ogg`
- Create: `src/main/resources/assets/britishbirds/sounds/robin/death.ogg`
- Update: `SOUND_CREDITS.md`
- Update: `docs/ASSET_PROVENANCE.md`

- [ ] **Step 1: Find and download BY-SA licensed Robin recordings from xeno-canto**

Visit https://xeno-canto.org/species/Erithacus-rubecula and filter by licence. Also check specific recordings noted in the reference doc:
- Song: XC27554, XC599064, XC138375, XC631248
- Call: XC239703, XC380053

For each recording:
1. Check licence is CC BY-SA (or CC BY-NC-SA as fallback)
2. Download the .mp3
3. Note recordist name, XC number, licence

- [ ] **Step 2: Process audio files**

For each downloaded recording:
```bash
# Trim to 3-5 seconds of clean audio, convert to mono .ogg
ffmpeg -i input.mp3 -ss START_TIME -t DURATION -ac 1 -ar 44100 -c:a libvorbis -q:a 4 output.ogg
```

Create:
- `song1.ogg` — 3-5 second clip of robin song
- `song2.ogg` — different song clip for variety
- `call.ogg` — "seep" contact call, ~1 second
- `alarm.ogg` — "tic-tic-tic" alarm call, ~2 seconds
- `hurt.ogg` — sharp alarm snippet, ~0.5 seconds
- `death.ogg` — fading call snippet, ~1 second

- [ ] **Step 3: Update attribution files**

Add entries to `SOUND_CREDITS.md`:
```markdown
| robin/song1.ogg | https://xeno-canto.org/XXXXX | [Recordist Name] | CC BY-SA 4.0 | 2026-03-19 | Trimmed to 4s, converted to mono .ogg |
```

Add matching entries to `docs/ASSET_PROVENANCE.md`.

- [ ] **Step 4: Verify sounds play in-game**

Spawn Robin, listen for ambient song. Hit Robin, listen for hurt sound.

### Task 3.8: Spawn Rules

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/world/BirdSpawnRules.java`
- Modify: `src/main/java/com/birbs/britishbirds/BritishBirdsMod.java`

- [ ] **Step 1: Create `BirdSpawnRules`**

```java
package com.birbs.britishbirds.world;

import com.birbs.britishbirds.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Registers biome-based spawn rules for all bird species.
 */
public class BirdSpawnRules {

    public static void initialize() {
        registerRobinSpawns();
    }

    private static void registerRobinSpawns() {
        // Robin spawns in forest biomes and near villages
        // Weight 10, min group 1, max group 2 (robins are solitary)
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST,
                        Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST,
                        Biomes.CHERRY_GROVE, Biomes.TAIGA, Biomes.PLAINS, Biomes.MEADOW
                ),
                MobCategory.CREATURE,
                ModEntities.ROBIN,
                10, 1, 2
        );

        // Register spawn placement restrictions
        SpawnPlacements.register(
                ModEntities.ROBIN,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
        );
    }
}
```

Note: The `BiomeModifications` and `BiomeSelectors` API is Fabric-specific. The `Biomes` registry keys may need adjusting for 26.1 — if `Biomes.FOREST` doesn't resolve, it may be `net.minecraft.world.level.biome.Biomes` with slightly different field names in the unobfuscated API.

- [ ] **Step 2: Wire into `BritishBirdsMod.onInitialize()`**

```java
BirdSpawnRules.initialize();
```

- [ ] **Step 3: Test natural spawning**

Create a new survival world. Explore forest biomes. Robins should spawn naturally in small numbers. Check they appear in Forest, Flower Forest, Birch Forest, and Plains biomes.

### Task 3.9: Bird Sound Manager (Dawn Chorus)

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/sound/BirdSoundManager.java`

- [ ] **Step 1: Create basic dawn/dusk song scheduling**

The Robin sings most at dawn (MC time 0-2000) and dusk (11000-13000). Override the ambient sound timing.

Add to `RobinEntity`:
```java
@Override
public int getAmbientSoundInterval() {
    // Sing more frequently at dawn and dusk
    long dayTime = this.level().getDayTime() % 24000;
    boolean isDawn = dayTime >= 0 && dayTime <= 2000;
    boolean isDusk = dayTime >= 11000 && dayTime <= 13000;

    if (isDawn || isDusk) {
        return 80; // Every 4 seconds at dawn/dusk
    }
    return 400; // Every 20 seconds otherwise (robins sing year-round, just less often)
}
```

- [ ] **Step 2: Verify dawn chorus timing**

Use `/time set 0` (dawn) — Robin should sing frequently. Use `/time set 6000` (midday) — singing should be less frequent.

**--- PHASE 3 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## Phase 4: Blue Tit

**Goal:** Second songbird validates SmallPasserine code reuse. Adds acrobatic clinging and flocking behaviours.

### Task 4.1: Blue Tit Entity

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/songbird/BlueTitEntity.java`
- Modify: `src/main/java/com/birbs/britishbirds/registry/ModEntities.java` — add BLUE_TIT EntityType
- Modify: `src/main/java/com/birbs/britishbirds/registry/ModItems.java` — add BLUE_TIT_SPAWN_EGG (colours: #4A90D9 blue crown, #F5D442 yellow breast)
- Modify: `src/main/java/com/birbs/britishbirds/registry/ModSounds.java` — add BLUE_TIT_SONG, BLUE_TIT_CALL, BLUE_TIT_ALARM, BLUE_TIT_HURT, BLUE_TIT_DEATH
- Modify: `src/main/java/com/birbs/britishbirds/BritishBirdsMod.java` — register attributes

- [ ] **Step 1:** Create `BlueTitEntity` extending `SmallPasserineEntity`. Override `registerSpeciesGoals()` to add `AcrobaticClingingGoal` and `FlockingGoal`. Override `createAttributes()` with slightly lower health (3.0) and faster speed (0.35). Size: 0.3f x 0.3f (smaller than Robin).

- [ ] **Step 2:** Register entity type, spawn egg, sounds, attributes following the Robin pattern.

### Task 4.2: Acrobatic Clinging Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/movement/AcrobaticClingingGoal.java`

- [ ] **Step 1:** Create `AcrobaticClingingGoal` — bird finds a leaf or log block, flies to it, and hangs from the underside. Uses a custom pose state that the model reads to render the bird upside-down. Similar structure to `PerchingGoal` but targets leaf/log blocks and sets a `isHangingUpsideDown` flag on the entity.

### Task 4.3: Flocking Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/social/FlockingGoal.java`

- [ ] **Step 1:** Create `FlockingGoal` — bird stays within configurable distance of other nearby birds of the same or compatible species. Uses a cohesion + alignment + separation model (simplified boids). When no flock-mates are nearby, reverts to solo behaviour.

### Task 4.4: Blue Tit Model, Texture, Sounds

**Files:**
- Create: `src/client/java/com/birbs/britishbirds/client/model/BlueTitModel.java`
- Create: `src/client/java/com/birbs/britishbirds/client/renderer/BlueTitRenderer.java`
- Create: `src/client/java/com/birbs/britishbirds/client/renderer/BlueTitRenderState.java`
- Modify: `src/client/java/com/birbs/britishbirds/client/model/BirdModelLayers.java` — add BLUE_TIT
- Modify: `src/client/java/com/birbs/britishbirds/client/BritishBirdsClient.java` — register
- Create: `src/main/resources/assets/britishbirds/textures/entity/blue_tit/blue_tit.png`
- Create: sound files from xeno-canto (BY-SA, species page: https://xeno-canto.org/species/Cyanistes-caeruleus)
- Update: `sounds.json`, `en_us.json`, `SOUND_CREDITS.md`, `ASSET_PROVENANCE.md`

- [ ] **Step 1:** Create `BlueTitModel` — smaller than Robin (0.25 blocks), proportionally larger head, same part hierarchy. Key colour references: crown #4A90D9, wings #5B9BD5, back #7BA23F, breast #F5D442, face #FFFFFF, eye stripe #1A1A2E.
- [ ] **Step 2:** Create `BlueTitRenderer` and `BlueTitRenderState` following Robin pattern. Add `isHangingUpsideDown` state for the acrobatic animation.
- [ ] **Step 3:** Model's `setupAnim` should include upside-down pose when `isHangingUpsideDown` is true (rotate body 180 degrees on X axis).
- [ ] **Step 4:** Source, trim, and convert Blue Tit sounds from xeno-canto. Update attribution.
- [ ] **Step 5:** Add spawn rules — Forest, Birch Forest, Flower Forest, Plains, Villages. Weight 12, group 2-4 (Blue Tits are more social than Robins).
- [ ] **Step 6:** Verify in-game — Blue Tit should appear, be noticeably smaller than Robin, flock loosely with other Blue Tits, occasionally hang upside-down from leaves.

**--- PHASE 4 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## Phase 5: Barn Owl

**Goal:** First raptor. Tests nocturnal activity schedule, hunting AI, and quartering flight.

### Task 5.1: RaptorEntity Base Class

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/raptor/RaptorEntity.java`

- [ ] **Step 1:** Create `RaptorEntity` extending `AbstractFlyingBird`. Attributes: higher health (8.0), attack damage (2.0), faster flying speed (0.5). Goals: hunting small entities, perching on high points. `registerGoals()` includes `FloatGoal`, `MeleeAttackGoal` (for striking prey), `PerchingGoal` (configured for high points, search radius 16), basic wandering flight, plus abstract `registerSpeciesGoals()`. Note: `SoaringFlightGoal` is not created until Phase 6 — RaptorEntity uses basic `FlutteringFlightGoal` (at high altitude) as a temporary placeholder, replaced by soaring when the Peregrine is implemented.

### Task 5.2: Activity Schedule Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/schedule/ActivityScheduleGoal.java`

- [ ] **Step 1:** Create `ActivityScheduleGoal` — controls whether the entity is active or roosting based on MC time. Three modes: DIURNAL (active 0-12000), NOCTURNAL (active 12000-24000), CREPUSCULAR (active 11000-13000 and 23000-1000). When inactive, the bird seeks a dark/sheltered spot and stays still. When active, normal AI resumes.

### Task 5.3: Quartering Flight Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/flight/QuarteringFlightGoal.java`

- [ ] **Step 1:** Create `QuarteringFlightGoal` — slow, low, wavering back-and-forth flight 1-3 blocks above ground over open areas. When prey is detected, hover briefly then dive. Speed: very slow (0.15). Flight height: 2-4 blocks above ground. Wavering: sinusoidal lateral drift during flight.

### Task 5.4: Hunting Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/feeding/HuntingGoal.java`

- [ ] **Step 1:** Create `HuntingGoal` — configurable hunting AI. Target selector based on entity type (rabbits, chickens, or other small mod birds). Hunting technique enum: QUARTERING (Barn Owl), STOOP (Peregrine — added in Phase 6), PURSUIT (generic). For QUARTERING: fly low, detect prey within range, hover, strike from above.

### Task 5.5: Barn Owl Entity

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/raptor/BarnOwlEntity.java`
- Modify: `ModEntities.java`, `ModItems.java`, `ModSounds.java`, `BritishBirdsMod.java`

- [ ] **Step 1:** Create `BarnOwlEntity` extending `RaptorEntity`. Config: nocturnal schedule, quartering hunt style, silent flight, can hover, sexual dimorphism (true — males are whiter underneath, females more spotted). Attributes: health 8.0, attack 2.0, flying speed 0.3 (slow). Spawn egg colours: #F5F0E0 (white breast), #C4A055 (golden-buff back). Size: 0.5f x 0.5f.

- [ ] **Step 2:** Override `hasSexualDimorphism()` to return true.

### Task 5.6: Barn Owl Model, Texture, Sounds

**Files:**
- Create: `BarnOwlModel.java`, `BarnOwlRenderer.java`, `BarnOwlRenderState.java`
- Modify: `BirdModelLayers.java`, `BritishBirdsClient.java`
- Create: textures (`barn_owl/barn_owl.png` male, `barn_owl/barn_owl_female.png`)
- Create: sounds from xeno-canto (https://xeno-canto.org/species/Tyto-alba)

- [ ] **Step 1:** Create `BarnOwlModel` — distinctive heart-shaped facial disc (flat face plate), long legs, broad wings. Medium size (~0.7 blocks). The facial disc is a flattened cuboid on the front of the head.
- [ ] **Step 2:** Create male texture (white underparts, golden-buff back, dark eyes) and female texture (buff-washed underparts with dark spotting).
- [ ] **Step 3:** Source screech, hiss, and chirrup sounds from xeno-canto BY-SA. Update attribution.
- [ ] **Step 4:** Spawn rules — Plains, Sunflower Plains, near Villages. NOT forests. Low spawn weight (3). Group 1-1 (solitary). Nocturnal spawn conditions (low light).
- [ ] **Step 5:** Verify in-game — Barn Owl active at night, roosting in dark spaces by day, flies low over plains with wavering motion, hunts rabbits.

**--- PHASE 5 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## Phase 6: Peregrine Falcon

**Goal:** Second raptor validates RaptorEntity reuse. Adds soaring and stooping flight.

### Task 6.1: Soaring Flight Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/flight/SoaringFlightGoal.java`

- [ ] **Step 1:** Create `SoaringFlightGoal` — bird circles at high altitude using thermals. Flies in wide circles 20-40 blocks above ground. Occasional wing flaps between long glides. Used when not actively hunting.

### Task 6.2: Stooping Flight Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/flight/StoopingFlightGoal.java`

- [ ] **Step 1:** Create `StoopingFlightGoal` — high-speed dive attack. When prey is spotted from altitude: fold wings (narrow hitbox), accelerate downward at extreme speed, strike at bottom. The stoop is the Peregrine's signature move. Implement as a multi-phase goal: CLIMBING (gain altitude), TARGETING (spot prey), DIVING (fold wings, accelerate), STRIKING (attack), PULLOUT (climb back up).

### Task 6.3: Peregrine Falcon Entity

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/raptor/PeregrineFalconEntity.java`
- Modify: `ModEntities.java`, `ModItems.java`, `ModSounds.java`, `BritishBirdsMod.java`

- [ ] **Step 1:** Create `PeregrineFalconEntity` extending `RaptorEntity`. Config: diurnal, stoop hunt style, not silent, cannot hover. Sexual dimorphism true (female 30% larger — adjust scale in renderer). Targets other birds (chickens, parrots, and other mod birds). Spawn egg colours: #4A5568 (slate-grey back), #F5F0E0 (pale barred breast). Size: 0.5f x 0.6f.
- [ ] **Step 2:** Add to HuntingGoal: STOOP technique — climb to altitude, spot prey, dive.

### Task 6.4: Peregrine Model, Texture, Sounds

**Files:**
- Create: `PeregrineFalconModel.java`, `PeregrineFalconRenderer.java`, `PeregrineFalconRenderState.java`
- Modify: `BirdModelLayers.java`, `BritishBirdsClient.java`
- Create: textures (`peregrine_falcon/peregrine_adult.png`, `peregrine_falcon/peregrine_juvenile.png`)
- Create: sounds from xeno-canto (https://xeno-canto.org/species/Falco-peregrinus)

- [ ] **Step 1:** Create `PeregrineFalconModel` — compact torpedo shape, pointed wings, prominent moustachial stripe. Medium-large size (~0.7 blocks). Wing shape changes with flight phase (spread for soaring, folded for stoop).
- [ ] **Step 2:** Adult texture: slate-grey back, dark head/moustache, pale barred underparts, yellow cere/feet. Juvenile: brown with streaked (not barred) underparts, blue-grey cere.
- [ ] **Step 3:** Source kak alarm, chitter, and ee-chip sounds from xeno-canto BY-SA. Update attribution.
- [ ] **Step 4:** Spawn rules — Stony Peaks, Windswept Hills, near structures. Very low spawn weight (2). Group 1-1.
- [ ] **Step 5:** Verify in-game — Peregrine soars at high altitude, dives to attack chickens/birds, perches on high points.

**--- PHASE 6 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## Phase 7: Mallard

**Goal:** Water bird archetype. Tests swimming, dabbling, waddling, and sexual dimorphism.

### Task 7.1: AbstractWaterBird

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/base/AbstractWaterBird.java`

- [ ] **Step 1:** Create `AbstractWaterBird` extending `AbstractBritishBird`. Adds: swimming navigation (can pathfind through water), water detection (seeks water), buoyancy (floats on surface). Override `travel()` to handle swimming. Override `getPathfindingMalus()` to make water attractive (negative malus).

### Task 7.2: Water Movement Goals

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/water/SwimmingGoal.java`
- Create: `src/main/java/com/birbs/britishbirds/ai/water/DabblingGoal.java`
- Create: `src/main/java/com/birbs/britishbirds/ai/movement/WaddlingMovementGoal.java`
- Create: `src/main/java/com/birbs/britishbirds/ai/flight/DirectFlightGoal.java`
- Create: `src/main/java/com/birbs/britishbirds/ai/flight/WaterTakeoffGoal.java`

- [ ] **Step 1:** `SwimmingGoal` — float on water surface, paddle around. Navigate to random water positions within range.
- [ ] **Step 2:** `DabblingGoal` — upend animation (tail up, head down) while on water. Triggered periodically while swimming.
- [ ] **Step 3:** `WaddlingMovementGoal` — side-to-side gait on land. Similar to hopping but with body roll.
- [ ] **Step 4:** `DirectFlightGoal` — strong, straight, rapid wingbeats. No undulation. For traveling between water bodies.
- [ ] **Step 5:** `WaterTakeoffGoal` — near-vertical launch from water with rapid wingbeats.
- [ ] **Step 6:** `WaterLandingGoal` — feet-forward braking approach to water, splash particles on touchdown. Triggers when bird is flying and water is below.

### Task 7.3: Duckling Follow Goal

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/ai/social/DucklingFollowGoal.java`

- [ ] **Step 1:** Create `DucklingFollowGoal` — baby ducks follow the nearest adult Mallard in a line. Similar to `FollowParentGoal` but with tighter following distance and line formation. Multiple ducklings space themselves behind the parent.

### Task 7.4: Waterfowl Entity & Mallard Entity

**Files:**
- Create: `src/main/java/com/birbs/britishbirds/entity/waterfowl/WaterfowlEntity.java`
- Create: `src/main/java/com/birbs/britishbirds/entity/waterfowl/MallardEntity.java`
- Modify: `ModEntities.java`, `ModItems.java`, `ModSounds.java`, `BritishBirdsMod.java`

- [ ] **Step 1:** `WaterfowlEntity` extending `AbstractWaterBird`. Composes swimming, dabbling, waddling, direct flight, water takeoff, flocking. Config: canDive, gregariousness, ducklingFollowMother.
- [ ] **Step 2:** `MallardEntity` extending `WaterfowlEntity`. Sexual dimorphism: true (male has green head, female is mottled brown). Spawn egg colours: #2D6B33 (green head), #8B7355 (brown female). Size: 0.6f x 0.6f. Attributes: health 6.0, speed 0.2.
- [ ] **Step 3:** Register entity, spawn egg, sounds, attributes.

### Task 7.5: Mallard Model, Texture, Sounds

**Files:**
- Create: `MallardModel.java`, `MallardRenderer.java`, `MallardRenderState.java`
- Create: textures (drake, hen, duckling)
- Create: sounds from xeno-canto (https://xeno-canto.org/species/Anas-platyrhynchos)

- [ ] **Step 1:** `MallardModel` — heavy body, flat bill (wider cuboid), short legs set far back. Webbed feet (wider foot part). Drake tail curl (extra curled cuboid on male). Animations: swim paddle, dabble/upend (body pitch 90°), waddle walk (body roll), wing flap.
- [ ] **Step 2:** Drake texture: green head (#2D6B33), white neck ring, chestnut breast (#8B4513), grey flanks, black rump, yellow bill (#DAA520), orange legs (#FF8C00), blue-purple speculum (#6A5ACD) with white bars. Female texture: mottled brown (#8B7355) all over, orange-brown bill, same speculum. Duckling: yellow (#FFD700) belly/face, black back with 4 yellow spots.
- [ ] **Step 3:** Source sounds: female quack (the classic duck quack), male "raehb" (quiet raspy call), wing whistle. Update attribution.
- [ ] **Step 4:** Spawn rules — River, Swamp, Mangrove Swamp, near water in Plains/Forest. Weight 8, group 3-6 (gregarious).
- [ ] **Step 5:** Verify in-game — Mallards swim on water, dabble/upend, waddle on land, drakes look different from hens, ducklings follow mother.

**--- PHASE 7 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## Phase 8: Polish & Integration

**Goal:** Refined dawn chorus, spawn balancing, inter-species interactions, sound variety.

### Task 8.1: Dawn Chorus System
- [ ] Expand `BirdSoundManager` to schedule multiple species singing in the correct order at dawn: Robin first (earliest singer), then Blue Tit, then others.

### Task 8.2: Spawn Weight Balancing
- [ ] Test and tune spawn weights across biomes. Common birds (Robin: 10, Blue Tit: 12) should appear frequently. Scarce birds (Peregrine: 2, Barn Owl: 3) should be rare encounters.

### Task 8.3: Inter-Species Interactions
- [ ] Peregrine's hunting goal should target mod birds (Robin, Blue Tit) in addition to vanilla chickens.
- [ ] Small birds should flee from raptors (add `AvoidEntityGoal` targeting `RaptorEntity`).
- [ ] Blue Tit flocking should include Robin in mixed winter flocks (extend `FlockingGoal` compatibility).

### Task 8.4: Sound Variety
- [ ] Add 2-3 more sound variants per call type for each species (download additional xeno-canto recordings).
- [ ] Update `sounds.json` to include all variants so Minecraft picks randomly.

### Task 8.5: Creative Tab
- [ ] Create a "British Birds" creative mode tab containing all spawn eggs, organized by family.

### Task 8.6: Final Testing
- [ ] Test all 5 birds across relevant biomes.
- [ ] Verify no crashes on dedicated server (all client code in `src/client/`).
- [ ] Verify spawn rates feel natural.
- [ ] Verify sounds play correctly with subtitles.
- [ ] Verify entity persistence (save/load world with birds present).

**--- PHASE 8 COMPLETE — PAUSE FOR USER COMMIT ---**

---

## File Summary

### New Files (by phase)

**Phase 1:** 12 files
- `settings.gradle`, `gradle.properties`, `build.gradle`
- `fabric.mod.json`, `BritishBirdsMod.java`, `BritishBirdsClient.java`
- `ModEntities.java`, `ModItems.java`, `ModSounds.java`
- `AbstractBritishBird.java`, `RobinEntity.java`
- `BirdModelLayers.java`, `RobinModel.java`, `RobinRenderer.java`, `RobinRenderState.java`
- Placeholder textures and sounds, `sounds.json`, `en_us.json`

**Phase 2:** 3 files
- `AbstractFlyingBird.java`
- `HoppingMovementGoal.java`, `PerchingGoal.java`, `FlutteringFlightGoal.java`

**Phase 3:** 4 files
- `SmallPasserineEntity.java`
- `GroundForagingGoal.java`, `FollowDiggingPlayerGoal.java`, `TerritorialGoal.java`
- `BirdSpawnRules.java`
- Real textures and sounds

**Phase 4:** 5 files
- `BlueTitEntity.java`
- `AcrobaticClingingGoal.java`, `FlockingGoal.java`
- `BlueTitModel.java`, `BlueTitRenderer.java`, `BlueTitRenderState.java`

**Phase 5:** 6 files
- `RaptorEntity.java`, `BarnOwlEntity.java`
- `ActivityScheduleGoal.java`, `QuarteringFlightGoal.java`, `HuntingGoal.java`
- `BarnOwlModel.java`, `BarnOwlRenderer.java`, `BarnOwlRenderState.java`

**Phase 6:** 3 files
- `PeregrineFalconEntity.java`
- `SoaringFlightGoal.java`, `StoopingFlightGoal.java`
- `PeregrineFalconModel.java`, `PeregrineFalconRenderer.java`, `PeregrineFalconRenderState.java`

**Phase 7:** 8 files
- `AbstractWaterBird.java`, `WaterfowlEntity.java`, `MallardEntity.java`
- `SwimmingGoal.java`, `DabblingGoal.java`, `WaddlingMovementGoal.java`, `DirectFlightGoal.java`, `WaterTakeoffGoal.java`, `DucklingFollowGoal.java`
- `MallardModel.java`, `MallardRenderer.java`, `MallardRenderState.java`

**Phase 8:** Modifications to existing files only.
