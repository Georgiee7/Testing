package me.george.testing.entity.pokemon.types.t1.fire;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.george.testing.entity.combatant.stats.PermanentStat;
import me.george.testing.entity.combatant.stats.PermanentStats;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonType;

import java.util.Map;
import java.util.UUID;

/**
 * The Charmander Pokémon.
 * Created by George K. on 4/13/2024.
 */
public class Charmander extends Pokemon {

    @JsonCreator
    public Charmander(
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("name") String name,
            @JsonProperty("alive") boolean alive,
            @JsonProperty("health") int health,
            @JsonProperty("exp") int exp,
            @JsonProperty("level") int level,
            @JsonProperty("permanentStats") PermanentStats permanentStats,
            @JsonProperty("evs") Map<PermanentStat, Integer> evs,
            @JsonProperty("shiny") boolean shiny,
            @JsonProperty("ownerUuid") UUID ownerUuid) {
        super(uuid != null ? uuid : UUID.randomUUID(),
              name != null ? name : PokemonType.CHARMANDER.getName(),
              alive, health, exp > 0 ? exp : 0, level > 0 ? level : 1,
              PokemonType.CHARMANDER, permanentStats, evs, shiny, ownerUuid);
    }

    public Charmander(PermanentStats permanentStats) {
        super(PokemonType.CHARMANDER, permanentStats);
    }
}
