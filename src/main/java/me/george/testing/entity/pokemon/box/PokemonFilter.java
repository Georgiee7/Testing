package me.george.testing.entity.pokemon.box;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.utils.TriFunction;
import me.george.testing.utils.Utils;

/**
 * The filters a Pokémon box can show its Pokémon by. Each filter has a set of values to pick from (an enum's values),
 * and the check that tells if a Pokémon matches the picked value. The box's owner is passed too, for filters that need it.
 * Created by George K. on 11/27/2026.
 */
@Getter
public enum PokemonFilter {
    STATUS("Status", PokemonFilter::checkStatus, HealthStatus.values(), false),
    OWNER("Owner", PokemonFilter::checkOwner, Ownership.values(), false),
    ELEMENT("Element", (owner, pokemon, value) -> Utils.contains(pokemon.getElements(), value), Element.values(), true),
    LEVEL("Level", (owner, pokemon, value) -> ((LevelRange) value).contains(pokemon.getLevel()), LevelRange.values(), true);

    private final String name;
    private final TriFunction<Player, Pokemon, Enum<?>, Boolean> filter;
    private final Enum<?>[] enumValues;
    private final boolean addDefaultAllValue; // Whether null is a value too, showing every Pokémon.

    PokemonFilter(String name, TriFunction<Player, Pokemon, Enum<?>, Boolean> filter, Enum<?>[] values, boolean addDefaultAllValue) {
        this.name = name;
        this.filter = filter;
        this.enumValues = values;
        this.addDefaultAllValue = addDefaultAllValue;
    }

    /**
     * Gets the filtering condition of this filter.
     *
     * @param owner     The owner of the box being filtered, may be null.
     * @param pokemon   The Pokémon to check.
     * @param enumValue The picked value of this filter, null meaning all if allowed.
     * @return passesFilter
     */
    public boolean filter(Player owner, Pokemon pokemon, Enum<?> enumValue) {
        return this.filter == null
                || (this.addDefaultAllValue && enumValue == null) // Default value is included.
                || this.filter.apply(owner, pokemon, enumValue); // Apply the filter.
    }

    /**
     * Whether a value is one this filter can be set to.
     *
     * @param enumValue The value in question, may be null.
     * @return isValidValue
     */
    public boolean isValidValue(Enum<?> enumValue) {
        return enumValue == null ? this.addDefaultAllValue : Utils.contains(this.enumValues, enumValue);
    }

    private static boolean checkStatus(Player owner, Pokemon pokemon, Enum<?> value) {
        return switch ((HealthStatus) value) {
            case SHOW_ALL -> true;
            case SHOW_HEALTHY -> pokemon.isAlive() && pokemon.getHealth() == pokemon.getHP();
            case SHOW_HURT -> pokemon.isAlive() && pokemon.getHealth() < pokemon.getHP();
            case SHOW_FAINTED -> !pokemon.isAlive();
        };
    }

    private static boolean checkOwner(Player owner, Pokemon pokemon, Enum<?> value) {
        return switch ((Ownership) value) {
            case SHOW_ALL -> true;
            case SHOW_MINE -> owner != null && owner.equals(pokemon.getOwner());
            case SHOW_WILD -> !pokemon.hasOwner();
            case SHOW_OTHERS -> pokemon.hasOwner() && !pokemon.getOwner().equals(owner);
        };
    }

    public enum HealthStatus {
        SHOW_ALL,
        SHOW_HEALTHY,
        SHOW_HURT,
        SHOW_FAINTED
    }

    public enum Ownership {
        SHOW_ALL,
        SHOW_MINE,
        SHOW_WILD,
        SHOW_OTHERS
    }

    @AllArgsConstructor
    public enum LevelRange {
        LEVEL_1_TO_25(1, 25),
        LEVEL_26_TO_50(26, 50),
        LEVEL_51_TO_75(51, 75),
        LEVEL_76_TO_100(76, 100);

        private final int min;
        private final int max;

        /**
         * Whether a level is within this range.
         *
         * @param level The level in question.
         * @return isInRange
         */
        public boolean contains(int level) {
            return level >= this.min && level <= this.max;
        }
    }
}
