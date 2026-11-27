# Testing

A simple project I made to teach colleagues and friends some Java concepts in a familiar, gamified way. It's a small Pokémon-style battle system, so instead of the usual `Animal` and `Dog` examples you get Pokémon, moves, parties and battles. I also used it to show some of my favorite design patterns and concepts.

It's plain Java 17, no game engine or server, so you can just read through it. Keep in mind it's just a proof of concept and not something I'm actively working on, more of an open ended thing I add to every now and then.

## Start here

If you only have a few minutes, these are the files worth looking at (all under `src/main/java/me/george/testing/`):

- `instanced/battle/Battle.java` - how a battle runs: its states, rounds and turns.
- `instanced/battle/event/BattleEventManager.java` - the event bus. Listen to events with a priority, and cancel or change them.
- `instanced/battle/damage/DamagePipeline.java` - how damage gets worked out, step by step.
- `entity/pokemon/box/PokemonFilter.java` and `PokemonSorter.java` - filtering and sorting with enums.
- `entity/pokemon/PokemonType.java` - the registry of every Pokémon.

## What's in it

- **Inheritance and abstraction** - `Entity` > `Combatant` > `Pokemon`, and `Move` > `TargetMove` > the actual moves. Each level only adds what it needs.
- **Enums as registries** - `PokemonType` and `MoveType` hold the data of every Pokémon and move, and know how to create them (constructor references like `Charmander::new`).
- **Enums with behavior** - the filters, sorters and damage modifiers are enums that each carry a function (a check, a `Comparator` or a multiplier), so adding a new one is just adding a line.
- **Template method** - `Move.perform()` rolls whether the move works, and each move only writes its own `execute()`.
- **Strategy** - `StatsCalculator`, so the stat formula can be swapped out.
- **Pipeline** - `DamagePipeline` runs a list of modifiers in order: critical hits, random spread, same type bonus and type effectiveness. Each battle has its own, so a battle can add something like weather without touching the others.
- **Event bus (observer)** - battles fire events when they start, when a turn starts, when a move is used, when damage is dealt, when a Pokémon faints and when they finish. Listeners have priorities, can cancel events or change the damage, and a broken listener can't break the battle. If you've used Bukkit/Spigot events it works pretty much the same way.
- **State machine** - a battle goes from not started, to active (and paused), to finished. Finishing a battle twice does nothing.
- **Managers** - each system has a manager with start and stop hooks, all registered in one place (`Manager`).
- **Functional interfaces** - `Consumer`, `Supplier`, `Function`, `Comparator` and a small `TriFunction`. For example, the Pokémon box reads its Pokémon from a `Supplier`, so it always shows the current ones.
- **Defensive copies and thread-safe collections** - getters hand out read-only views, and the shared caches use concurrent collections.
