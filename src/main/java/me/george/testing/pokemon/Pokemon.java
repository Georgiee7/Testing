package me.george.testing.pokemon;

import lombok.Getter;
import me.george.testing.pokemon.stats.PermanentStat;
import me.george.testing.pokemon.stats.PermanentStats;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a base Pokémon of a specific type.
 * Created by George K. on 4/12/2024.
 */
@Getter
public abstract class Pokemon {

    private final UUID uuid;

    private final PokemonType type;
    private final PermanentStats permanentStats;

    private int exp;
    private int level;

    protected Pokemon(PokemonType type, PermanentStats permanentStats) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.permanentStats = permanentStats;
    }

    protected Pokemon(PokemonType type) {
        this(type, new PermanentStats());
    }

    public int getStat(PermanentStat permanentStat) {
        return this.permanentStats.getStat(permanentStat);
    }

    /**
     * Gives exp to this Pokémon, will proc level-up.
     *
     * @param exp The exp to give.
     */
    public void addExp(int exp) {
        // TODO
    }

    /**
     * Makes this Pokémon level up if below the max level.
     */
    public void levelUp() {
        // TODO
    }

    /**
     * Sets the level of this Pokémon.
     *
     * @param level The level to set it to.
     */
    public void setLevel(int level) {
        // TODO
    }

    /**
     * Whether this Pokémon is weak against another (type) of Pokémon.
     *
     * @param other The Pokémon in question.
     * @return isWeakAgainst
     */
    public boolean isWeakAgainst(Pokemon other) {
        return this.type.isWeakAgainst(other.getType());
    }

    /**
     * Whether this Pokémon is immune from the attacks of another (type) of Pokémon.
     *
     * @param other The Pokémon in question.
     * @return isWeakAgainst
     */
    public boolean isImmuneFrom(Pokemon other) {
        return this.type.isImmuneAgainst(other.getType());
    }

    /**
     * Whether this Pokémon is effective against another (type) of Pokémon.
     *
     * @param other The Pokémon in question.
     * @return isWeakAgainst
     */
    public boolean isEffectiveAgainst(Pokemon other) {
        return this.type.isEffectiveAgainst(other.getType());
    }

    /**
     * Whether this Pokémon's attack behave normally against another (type) of Pokémon.
     *
     * @param other The Pokémon in question.
     * @return isWeakAgainst
     */
    public boolean isNormalAgainst(Pokemon other) {
        return this.type.isNormalAgainst(other.getType());
    }

    @Override
    public String toString() {
        return "Pokemon: " + this.type.getName() + "\n" +
                "Level: " + this.level + "\n" +
                "Exp: " + this.exp + "\n" +
                "Element: " + Arrays.stream(this.type.getElements()).map(Element::getDisplayName).collect(Collectors.joining(", ")) +
                (this.permanentStats != null ?
                        (
                        "\n" +
                        "--------" + "\n" +
                        "Stats: " + this.permanentStats.getStats().entrySet().stream().map(stat -> stat.getKey().getDisplayName() + ": " + stat.getValue()).collect(Collectors.joining("\n"))
                        ) :
               "");
    }
}
