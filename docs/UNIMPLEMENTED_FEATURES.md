# Unimplemented / Partially Implemented Features

Features that have scaffolding, stubs, or partial code but aren't fully working yet.

## Breeding & Offspring
- `AbstractBritishBird.isFood()` returns false — no breeding foods defined
- `AbstractBritishBird.getBreedOffspring()` returns null — no breeding logic
- Baby birds render (textures exist for robin, peregrine, mallard) but can only be spawned manually
- No nesting behaviour, egg laying, or incubation

## Wing Tip Animation
- Wing tip `ModelPart`s are defined and textured in Robin, BlueTit, and Mallard models
- Extracted in constructor but never animated in `setupAnim()` — they're static
- Could articulate independently for feather-spread effects during flight

## Flocking (Partial)
- `FlockingGoal` exists with cohesion/separation logic
- Uses `GroundPathNavigation.moveTo()` so flocking is 2D ground-only
- No aerial flocking — birds can't flock in flight
- No V-formation for mallard migration flights
- Blue Tit and Mallard register it, but effectiveness is limited

## Peregrine Juvenile Plumage
- Juvenile texture exists (`peregrine_juvenile.png`)
- Renderer now selects it for babies (just fixed)
- But no actual ageing system — babies don't grow into adults with plumage change

## Ground Bird Archetype
- `GroundBirdEntity` mentioned in entity hierarchy and CLAUDE.md as `[future]`
- No implementation exists yet — needed for pheasant, partridge, etc.
- Would need burst-flight and running behaviours

## 95 More Bird Species
- `docs/british_birds_100.md` has the full 100-species list
- Only 5 are implemented (Robin, Blue Tit, Barn Owl, Peregrine Falcon, Mallard)
- Entity hierarchy and AI goal system are designed to scale

## Hopping Direction
- `HoppingMovementGoal` uses `getLookAngle()` for hop direction
- Bird's look direction may not match path direction, causing perpendicular hops

## Activity Schedule Roosting
- `ActivityScheduleGoal` exists with diurnal/nocturnal/crepuscular modes
- Roost-finding (`findDarkBlock()`) does random searches, may fail silently
- No actual roost animation — bird just stops moving

## Sound Variety
- Peregrine has limited variety (single BY-SA recording for all 5 sounds)
- 3 Robin sounds are CC BY-NC-SA (need BY-SA replacements for commercial use)
- No dawn chorus coordination between multiple robins

## Hand-Painted Textures
- All textures are programmatically generated via `tools/TextureGenerator.java`
- Functional but blocky — hand-painted textures would add much more detail

## Water Bird Swimming
- `AbstractWaterBird.travel()` has basic swimming with buoyancy
- No visible wake/splash particles
- No diving behaviour (for species that dive)

## Seasonal Behaviour
- No seasonal plumage changes (e.g. mallard eclipse plumage)
- No migration patterns
- No breeding season timing

## Future Entity Archetypes (Designed, Not Built)
- `GroundBirdEntity` — burst flights, running (pheasant, partridge, lark)
- `CorvidEntity` — intelligent crows, magpies, ravens
- `PigeonEntity` — direct powerful flight, ground feeding
- `HirundineEntity` — aerial insect feeders (swallows, martins, swifts)
- `WoodpeckerEntity` — tree clinging, drumming
- `WaderEntity` — long-legged shorebirds (lapwings, curlews)
- `DivingWaterBirdEntity` — underwater diving (cormorants, grebes, coots)

## Interaction with Players
- Robin follows digging players (`FollowDiggingPlayerGoal`) — works
- No other player interactions (feeding by hand, taming, etc.)
- No bird watching / discovery system
