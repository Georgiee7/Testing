package me.george.testing.entity.pokemon.box;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.entity.combatant.stats.PermanentStat;
import me.george.testing.entity.pokemon.Pokemon;

import java.util.Comparator;

/**
 * The orders a Pokémon box can sort its Pokémon in.
 * Pokémon that sort the same fall back to Pokédex order and then their name, so the order never changes between views.
 * Created by George K. on 11/27/2026.
 */
@Getter
@AllArgsConstructor
public enum PokemonSorter {
    POKEDEX("Pokédex Number", Comparator.comparingInt((Pokemon pokemon) -> pokemon.getType().getPokedexId())),
    NAME("Name (A to Z)", Comparator.comparing(Pokemon::getName, String.CASE_INSENSITIVE_ORDER)),
    LEVEL("Level (Highest to Lowest)", Comparator.comparingInt(Pokemon::getLevel).reversed()),
    LEVEL_REVERSED("Level (Lowest to Highest)", Comparator.comparingInt(Pokemon::getLevel)),
    HEALTH("Health (Lowest First)", Comparator.comparingDouble(PokemonSorter::getHealthPercentage)),
    SPEED("Speed (Fastest First)", Comparator.comparingInt(Pokemon::getSpeed).reversed()),
    TOTAL_STATS("Total Stats (Highest First)", Comparator.comparingInt(PokemonSorter::getTotalStats).reversed());

    public static final PokemonSorter DEFAULT = POKEDEX;

    private static final Comparator<Pokemon> DISPLAY_ORDER = Comparator.comparingInt((Pokemon pokemon) -> pokemon.getType().getPokedexId())
            .thenComparing(Pokemon::getName, String.CASE_INSENSITIVE_ORDER);

    private final String name;
    private final Comparator<Pokemon> sortOrder;

    /**
     * Gets the comparator of this sorter, ties included.
     *
     * @return comparator
     */
    public Comparator<Pokemon> getComparator() {
        return this.sortOrder.thenComparing(DISPLAY_ORDER);
    }

    private static double getHealthPercentage(Pokemon pokemon) {
        return pokemon.getHP() > 0 ? (double) pokemon.getHealth() / pokemon.getHP() : 0;
    }

    private static int getTotalStats(Pokemon pokemon) {
        int total = 0;
        for (PermanentStat stat : PermanentStat.values())
            total += pokemon.getPermanentStats().getStat(stat);

        return total;
    }
}
