package me.george.testing.entity.pokemon.box;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.spec.PokemonSpec;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A view of a set of Pokémon (such as a player's) that can be filtered and sorted, like a PC box.
 * The box reads its Pokémon from a supplier every time, so it always shows the current ones.
 * Created by George K. on 11/27/2026.
 */
public class PokemonBox {

    @Getter
    private final Player owner; // May be null, for a box that isn't anyone's.

    private final Supplier<Collection<Pokemon>> source;

    private final Enum<?>[] filterSettings = new Enum<?>[PokemonFilter.values().length]; // The picked value of each filter, by ordinal.

    @Getter
    private PokemonFilter selectedFilter = PokemonFilter.values()[0];

    @Getter
    @Setter
    private PokemonSorter sorter = PokemonSorter.DEFAULT;

    @Getter
    @Setter
    private PokemonSpec search; // Only shows the Pokémon matching it, such as "*[level>=30]". Null to search nothing.

    public PokemonBox(Player owner, Supplier<Collection<Pokemon>> source) {
        Utils.verifyNotNull(source, "Tried to create a Pokemon box with no source.");
        this.owner = owner;
        this.source = source;
        clearFilters();
    }

    /**
     * Creates a box of a player's Pokémon.
     *
     * @param player The player whose Pokémon to show.
     * @return playerBox
     */
    public static PokemonBox of(Player player) {
        Utils.verifyNotNull(player, "Tried to create a Pokemon box for a null player.");
        return new PokemonBox(player, player::getPokemons);
    }

    /**
     * Creates a box of every loaded Pokémon, seen by a player.
     *
     * @param viewer The player looking at the box, used by filters such as the owner one. May be null.
     * @return everyPokemonBox
     */
    public static PokemonBox ofEveryPokemon(Player viewer) {
        return new PokemonBox(viewer, PokemonManager::getPokemons);
    }

    /**
     * Gets the Pokémon that pass every filter, sorted by the current sorter.
     *
     * @return shownPokemon
     */
    public List<Pokemon> getPokemons() {
        return this.source.get().stream()
                .filter(this::applyFilters)
                .sorted(this.sorter.getComparator())
                .toList();
    }

    /**
     * Runs an action on every shown Pokémon, such as healing every hurt one.
     *
     * @param action The action to run.
     */
    public void forEachShown(Consumer<Pokemon> action) {
        getPokemons().forEach(action);
    }

    private boolean applyFilters(Pokemon pokemon) {
        if (this.search != null && !this.search.matches(pokemon))
            return false;

        for (PokemonFilter filter : PokemonFilter.values())
            if (!filter.filter(this.owner, pokemon, this.filterSettings[filter.ordinal()]))
                return false;

        return true;
    }

    /**
     * Selects the next filter, whose value nextFilterValue changes.
     */
    public void nextFilter() {
        this.selectedFilter = Utils.nextEnumLoop(this.selectedFilter);
    }

    /**
     * Changes the selected filter to its next value, going through "all" (null) for filters that have it.
     */
    public void nextFilterValue() {
        int filterId = this.selectedFilter.ordinal();
        Enum<?> newValue = Utils.nextEnumOrNullLoop(this.selectedFilter.getEnumValues(), this.filterSettings[filterId]);
        if (newValue == null && !this.selectedFilter.isAddDefaultAllValue())
            newValue = Utils.nextEnumOrNullLoop(this.selectedFilter.getEnumValues(), null); // Skip null, back to the first value.

        this.filterSettings[filterId] = newValue;
    }

    /**
     * Gets the picked value of a filter.
     *
     * @param filter The filter to get the value of.
     * @return filterValue
     */
    public Enum<?> getFilterValue(PokemonFilter filter) {
        return this.filterSettings[filter.ordinal()];
    }

    /**
     * Sets the picked value of a filter.
     *
     * @param filter The filter to set.
     * @param value  The value to set it to, null for all if the filter allows it.
     */
    public void setFilterValue(PokemonFilter filter, Enum<?> value) {
        Utils.verify(filter.isValidValue(value), "Tried to set the %s filter to an invalid value: %s", filter.getName(), value);
        this.filterSettings[filter.ordinal()] = value;
    }

    /**
     * Resets every filter, showing every Pokémon.
     */
    public void clearFilters() {
        for (PokemonFilter filter : PokemonFilter.values())
            this.filterSettings[filter.ordinal()] = filter.isAddDefaultAllValue() ? null : filter.getEnumValues()[0];
    }

    /**
     * Selects the next sorter.
     */
    public void nextSorter() {
        this.sorter = Utils.nextEnumLoop(this.sorter);
    }

    /**
     * Selects the previous sorter.
     */
    public void previousSorter() {
        this.sorter = Utils.previousEnumLoop(this.sorter);
    }
}
