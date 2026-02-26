package me.george.testing.entity.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.Entity;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a player that can own Pokémon slaves.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Player extends Entity {
    // Runtime set of Pokemon instances (not serialized directly).
    @JsonIgnore
    private final Set<Pokemon> pokemons;

    private final ConcurrentMap<Integer, PokemonType> pokedex;

    @JsonIgnore
    @Setter(AccessLevel.PROTECTED)
    private Pokemon activePokemon;

    // Temporary holder for persisted pokemon UUIDs during deserialization.
    @JsonProperty("pokemonUuids")
    private List<UUID> persistedPokemonUuids = new ArrayList<>();

    @JsonCreator
    protected Player(@JsonProperty("uuid") UUID uuid, @JsonProperty("name") String name) {
        super(uuid != null ? uuid : UUID.randomUUID(), name);
        this.pokemons = ConcurrentHashMap.newKeySet();
        this.pokedex = new ConcurrentHashMap<>();
    }

    protected Player(String name) {
        this(null, name);
    }

    /**
     * JSON getter used by Jackson during serialization.
     * Returns the UUIDs of Pokémon this player currently owns. The actual in-memory
     * Pokémon instances are stored in {@link #pokemons} and are not serialized directly.
     */
    @JsonGetter("pokemonUuids")
    @SuppressWarnings("unused")
    public List<UUID> getPokemonUuidsForSerialization() {
         List<UUID> uuids = new ArrayList<>();
         for (Pokemon p : this.pokemons)
             uuids.add(p.getUuid());

         return uuids;
     }

    /**
     * JSON setter used by Jackson during deserialization.
     * Jackson will populate the persisted UUID list here; the actual {@link #pokemons}
     * set is re-linked to in-memory Pokemon instances by Manager.relinkPersistentEntities().
     */
    @JsonSetter("pokemonUuids")
    @SuppressWarnings("unused")
    public void setPokemonUuidsForDeserialization(List<UUID> uuids) {
        this.persistedPokemonUuids = (uuids == null) ? new ArrayList<>() : new ArrayList<>(uuids);
    }

    public List<UUID> getPersistedPokemonUuids() {
        return Collections.unmodifiableList(this.persistedPokemonUuids);
    }

    /**
     * Clears the persisted UUID list that was populated by Jackson during deserialization.
     * This should be called after relinking persisted UUIDs to in-memory Pokemon instances so
     * the next serialization uses the live {@link #pokemons} set.
     */
    @SuppressWarnings("unused")
    public void clearPersistedPokemonUuids() {
        this.persistedPokemonUuids = new ArrayList<>();
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    protected int[] getCachedExpRequirement() {
        return new int[PlayerManager.MAX_LEVEL];
    }

    @Override
    protected int getNeededExp(int level) {
        if (level < 1)
            level = 1;

        int levelExp = getCachedExpRequirement()[level - 1];
        if (levelExp > 0)
            return levelExp;

        if (level > 1) {
            int lastLevel = level - 1;
            levelExp = (int) (getNeededExp(lastLevel) + Math.pow(lastLevel, 2) + (lastLevel * 30));
        } else {
            levelExp = 500;
        }

        getCachedExpRequirement()[level - 1] = levelExp;
        return levelExp;
    }

    @Override
    protected boolean isMaxLevel() {
        return getLevel() >= PlayerManager.MAX_LEVEL;
    }

    /**
     * Adds a Pokémon to the player's party.
     *
     * @param pokemon The Pokémon to give to the player.
     */
    public void addPokemon(Pokemon pokemon) {
        this.pokemons.add(pokemon);
    }

    /**
     * Removes a Pokémon from the player's party.
     *
     * @param pokemon The pokemon to remove.
     */
    public void removePokemon(Pokemon pokemon) {
        despawnActivePokemon();
        this.pokemons.remove(pokemon);
    }

    /**
     * Whether a player has a specific Pokémon in their team.
     *
     * @param pokemon The Pokémon in question.
     * @return hasPokemonInTeam
     */
    public boolean hasPokemon(Pokemon pokemon) {
        return this.pokemons.contains(pokemon);
    }

    /**
     * Despawns the player's active Pokémon. Should handle proper execution and storage.
     */
    public void despawnActivePokemon() {
        setActivePokemon(null);
    }

    /**
     * Unlocks a specific Pokémon entry for the player's pokedex.
     *
     * @param type The Pokémon type to unlock
     */
    public void unlock(PokemonType type) {
        this.pokedex.putIfAbsent(type.getPokedexId(), type);
    }

    /**
     * Unlearns a pokedex entry from the player's pokedex.
     *
     * @param type The Pokémon type to unlearn.
     */
    public void unlearn(PokemonType type) {
        this.pokedex.remove(type.getPokedexId(), type);
    }

    /**
     * Checks whether a player has unlocked a type of Pokémon in their pokedex.
     *
     * @param type The type of Pokémon to check.
     * @return hasUnlockedPokemon
     */
    public boolean hasUnlocked(PokemonType type) {
        return this.pokedex.containsValue(type);
    }

    /**
     * Returns an unmodifiable view of the player's pokemons to protect internal state.
     *
     * @return pokemons
     */
    public Set<Pokemon> getPokemons() {
        return Collections.unmodifiableSet(this.pokemons);
    }

    /**
     * Returns an unmodifiable view of the player's pokedex to protect internal state.
     *
     * @return pokedex
     */
    public Map<Integer, PokemonType> getPokedex() {
        return Collections.unmodifiableMap(this.pokedex);
    }
}
