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
        this.permanentStats = permanentStats == null ? new PermanentStats() : permanentStats;
    }

    protected Pokemon(PokemonType type) {
        this(type, new PermanentStats());
    }

    /**
     * Gets this Pokémon's health.
     *
     * @return hp
     */
    public int getHP() {
        return getStat(PermanentStat.HP);
    }

    /**
     * Gets this Pokémon's attack.
     *
     * @return attack
     */
    public int getAttack() {
        return getStat(PermanentStat.ATK);
    }

    /**
     * Gets this Pokémon's special attack.
     *
     * @return specialAttack
     */
    public int getSpAttack() {
        return getStat(PermanentStat.SP_ATK);
    }

    /**
     * Gets this Pokémon's defense.
     *
     * @return defense
     */
    public int getDefense() {
        return getStat(PermanentStat.DEF);
    }

    /**
     * Gets this Pokémon's special defense.
     *
     * @return specialDefense
     */
    public int getSpDefense() {
        return getStat(PermanentStat.SP_DEF);
    }

    /**
     * Gets this Pokémon's speed.
     *
     * @return speed
     */
    public int getSpeed() {
        return getStat(PermanentStat.SPEED);
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

    private int getStat(PermanentStat permanentStat) {
        return this.permanentStats.getStat(permanentStat);
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
                                        "Stats: " + "\n"
                                        + this.permanentStats.getStats().entrySet()
                                        .stream()
                                        .map(stat -> stat.getKey().getDisplayName() + ": " + stat.getValue())
                                        .collect(Collectors.joining("\n"))
                        ) :
                        "");
    }
}
