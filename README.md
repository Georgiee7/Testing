# Testing

A project I made to teach colleagues and friends Java concepts in a familiar, gamified way. It's a Pokémon-style battle system, so instead of the usual `Animal` and `Dog` examples you get Pokémon, moves, parties, battles and challenges. I also used it to show some of my favorite design patterns and concepts.

It's plain Java 17 with no game engine or server, so the code is easy to read through on its own. Keep in mind it's a proof of concept rather than something I'm actively working on, more of an open ended thing I add to every now and then.

## Start here

If you only have a few minutes, these are the files worth looking at (all under `src/main/java/me/george/testing/`):

- `instanced/battle/Battle.java` - how a battle runs: its states, rounds and turns.
- `instanced/battle/event/BattleEventManager.java` - the event bus. Listen to events with a priority, and cancel or change them.
- `instanced/battle/damage/DamagePipeline.java` - how damage gets worked out, step by step.
- `challenge/Challenge.java` and the `challenge/composite` package - challenges that combine into trees.
- `entity/pokemon/spec/PokemonSpec.java` - Pokémon written as text, like `3xCHARMANDER[level=5-10]`.
- `analytics/AnalyticsWriter.java` - sending battle stats in the background without ever holding up a battle.
- `entity/pokemon/box/PokemonFilter.java` and `PokemonSorter.java` - filtering and sorting with enums.
- `entity/pokemon/PokemonType.java` - the registry of every Pokémon.

## What's in it

- **Inheritance and abstraction** - `Entity` > `Combatant` > `Pokemon`, and `Move` > `TargetMove` > the actual moves. Each level only adds what it needs.
- **Enums as registries** - `PokemonType` and `MoveType` hold the data of every Pokémon and move, and know how to create them (constructor references like `Charmander::new`).
- **Enums with behavior** - the filters, sorters and damage modifiers are enums that each carry a function (a check, a `Comparator` or a multiplier), so adding a new one is just adding a line.
- **Template method** - `Move.perform()` rolls whether the move works, and each move only writes its own `execute()`. Challenges work the same way: `offer()` handles the callbacks, and each challenge only writes how it takes an action.
- **Strategy** - `StatsCalculator`, so the stat formula can be swapped out.
- **Pipeline** - `DamagePipeline` runs a list of modifiers in order: critical hits, random spread, same type bonus and type effectiveness. Each battle has its own, so a battle can add something like weather without touching the others.
- **Event bus (observer)** - battles fire events when they start, when a turn starts, when a move is used, when damage is dealt, when a Pokémon faints and when they finish. Listeners have priorities, can cancel events or change the damage, and a broken listener can't break the battle. They can be lambdas, or methods marked with `@BattleHandler`. If you've used Bukkit/Spigot events it works pretty much the same way.
- **Composite** - challenges like "win 3 battles, then knock out 5 Fire types or deal 500 damage with Fire moves". Small challenges count actions, and `AllChallenge`, `AnyChallenge` and `SequenceChallenge` combine them into trees. Their callbacks run exactly once, and their progress can be saved as a list of numbers and picked back up. Battles don't know challenges exist, the challenges just listen to battle events.
- **Reflection** - `@BattleHandler` methods are found once per class, have their signature checked when they're registered rather than when an event comes in, and get called through `MethodHandle`s. `PokemonSpec` fills the fields marked with `@SpecArgument` from text, reading each field's generic type to know how to parse it, so adding an argument is just adding a field.
- **Parsing text** - a `PokemonSpec` like `*[element={FIRE,WATER},level>=30]` both matches Pokémon (a box search, or a challenge counting Fire types) and creates them (`2x{PIKACHU,EEVEE}[level=5-10]`). Its values are ranges, sets or comparisons, and a spec written back out parses to the same spec.
- **Background work** - analytics queues battle stats in a bounded queue, so a battle never waits on it. One background thread sends them in batches, tries a failed batch again with a growing delay, drops rows instead of running out of memory when it's down, and sends what's left when it stops. While it's off, tracking does nothing and costs nothing.
- **State machine** - a battle goes from not started, to active (and paused), to finished. Finishing a battle twice does nothing.
- **Managers** - each system has a manager with start and stop hooks, all registered in one place (`Manager`). They start in the order they're registered and stop in reverse, and one failing doesn't stop the others.
- **Functional interfaces** - `Consumer`, `Supplier`, `Function`, `Comparator` and a small `TriFunction`. For example, the Pokémon box reads its Pokémon from a `Supplier`, so it always shows the current ones.
- **Defensive copies and thread-safe collections** - getters hand out read-only views, and the shared caches use concurrent collections.

## Future expansion

The scope is kept small on purpose so it stays easy to teach with, which leaves out some of the bigger systems. These are all things I've worked with on other projects and plan to add here when I have the time:

- **Saving and loading** - JSON serialization and deserialization for players, parties, Pokémon and challenge progress, with a database behind it instead of in-memory caches, and a version on the saved data so a change to it can't quietly break old saves.
- **More async work** - `CompletableFuture`s for things like saving and loading without holding up a battle, like analytics already does with its sending.
- **Client and server** - a server that runs the battles and clients that send their moves through packets, with a master server that keeps track of the servers and sends players to them. Also events that run across every server at once, like a community challenge everyone works on together, with the master server keeping the progress and every server staying in sync with it.
- **More design patterns** - for example moves as commands that can be queued, and builders for creating Pokémon.
