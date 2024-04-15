package me.george.testing.pokemon;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.player.Player;
import me.george.testing.pokemon.stats.PermanentStat;
import me.george.testing.pokemon.stats.PermanentStats;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a base Pokémon of a specific type.
 * Created by George K. on 4/12/2024.
 */
@Getter
public abstract class Pokemon {
    private static final int[] CACHED_EXP_REQUIREMENT = new int[PokemonManager.MAX_LEVEL];

    private final UUID uuid;

    private final PokemonType type;
    private final PermanentStats permanentStats;

    @Setter(AccessLevel.PROTECTED)
    private Map<PermanentStat, Integer> evs;

    @Setter(AccessLevel.PROTECTED)
    private String name;

    @Setter(AccessLevel.PROTECTED)
    private int exp;

    @Setter(AccessLevel.PROTECTED)
    private int level;

    @Setter(AccessLevel.PROTECTED)
    private boolean shiny = false;

    @Setter(AccessLevel.PROTECTED)
    private Player owner;

    @Setter
    private int health;

    protected Pokemon(PokemonType type, PermanentStats permanentStats) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.evs = new HashMap<>();
        setExp(0);
        setLevel(15);
        this.permanentStats = permanentStats == null ? new PermanentStats(this) : permanentStats;

        setName(type.getName()); // Should be overwritten in case of a custom display name.
    }

    protected Pokemon(PokemonType type) {
        this(type, null);
    }

    /**
     * Gets this pokemon's base stats from its type.
     *
     * @return baseStats
     */
    public Map<PermanentStat, Integer> getBaseStats() {
        return this.type.getBaseStats();
    }

    /**
     * Adds Effort Values (EVs) to a specific stat of the Pokémon.
     *
     * @param stat   The stat to which EVs should be added.
     * @param amount The amount of EVs to add. Must be a positive integer.
     */
    public void addEv(PermanentStat stat, int amount) {
        if (amount <= PermanentStats.MIN_EV_AMOUNT)
            return;

        // Ensure the total EVs do not exceed the maximum allowed per Pokémon.
        int remainingEVs = Math.min(PermanentStats.MAX_EV_AMOUNT_PER_POKEMON - getTotalEVs(), amount);
        this.evs.put(stat, this.evs.getOrDefault(stat, 0) + remainingEVs);
        this.permanentStats.randomizeStats(this);
    }

    private int getTotalEVs() {
        return this.evs.values().stream().mapToInt(Integer::intValue).sum();
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
     * Decreases the Pokémon's HP by an amount, may kill it.
     *
     * @param amount The amount to decrease it by.
     */
    public void decreaseHP(int amount) {
        this.permanentStats.decreaseStat(PermanentStat.HP, amount);
        if (getHP() <= 0)
            kill();
    }

    /**
     * Sets the Pokémon's hp to an amount.
     *
     * @param amount The amount to set it to.
     */
    protected void setHP(int amount) {
        this.permanentStats.setStat(PermanentStat.HP, amount);
    }

    /**
     * Kills the Pokémon.
     */
    public void kill() {
        if (isAlive())
            this.health = 0;

        // TODO anything else
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
     * Gets the Pokémon's elements based on its type.
     *
     * @return elements
     */
    public Element[] getElements() {
        return this.type.getElements();
    }

    private int getStat(PermanentStat permanentStat) {
        return this.permanentStats.getStat(permanentStat);
    }

    /**
     * Whether this Pokémon is immune to an element.
     *
     * @param element The element in question.
     * @return isImmuneToElement
     */
    public boolean isImmuneTo(Element element) {
        return Arrays.stream(getElements()).anyMatch(e -> e.isImmuneFrom(element));
    }

    /**
     * Whether this Pokémon is effective against an element.
     *
     * @param element The element in question.
     * @return isEffectiveAgainstElement
     */
    public boolean isEffectiveAgainst(Element element) {
        return Arrays.stream(getElements()).anyMatch(e -> e.isEffectiveAgainst(element));
    }

    /**
     * Whether this Pokémon is weak against an element.
     *
     * @param element The element in question.
     * @return isWeakAgainstElement
     */
    public boolean isWeakAgainst(Element element) {
        return Arrays.stream(getElements()).anyMatch(e -> isWeakAgainst(element));
    }

    /**
     * Whether this Pokémon is normal against an element.
     *
     * @param element The element in question.
     * @return isNormalAgainstElement
     */
    public boolean isNormalAgainst(Element element) {
        return Arrays.stream(getElements()).anyMatch(e -> isNormalAgainst(element));
    }

    /**
     * Gives exp to this Pokémon, can proc level-up.
     *
     * @param exp The exp to give.
     */
    public void addExp(int exp) {
        if (isMaxLevel() || exp <= 0)
            return;

        setExp(this.exp + exp);
        while (this.exp >= getNeededExp() && !(isMaxLevel()))
            levelUp();
    }

    /**
     * Makes this Pokémon level up if below the max level.
     */
    public void levelUp() {
        setExp(getExp() - getNeededExp());
        setLevel(this.level + 1);
    }

    /**
     * Returns the amount of exp needed to level up.
     *
     * @return neededExp
     */
    public int getNeededExp() {
        return getNeededExp(getLevel());
    }

    private static int getNeededExp(int level) {
        // Statically defined exp formula.
        if (level < 1)
            level = 1;

        int levelExp = CACHED_EXP_REQUIREMENT[level - 1];
        if (levelExp > 0)
            return levelExp;

        if (level > 1) {
            int lastLevel = level - 1;
            levelExp = (int) (getNeededExp(lastLevel) + Math.pow(lastLevel, 2) + (lastLevel * 24) + 150);
        } else {
            levelExp = 176;
        }

        CACHED_EXP_REQUIREMENT[level - 1] = levelExp;
        return levelExp;
    }

    /**
     * Whether this Pokémon has reached max level or not.
     *
     * @return isMaxLevel
     */
    public boolean isMaxLevel() {
        return this.level >= PokemonManager.MAX_LEVEL;
    }

    /**
     * Whether this Pokémon is alive or not.
     *
     * @return isAlive
     */
    public boolean isAlive() {
        return getHP() > 0;
    }

    @Override
    public String toString() {
        return "Pokemon: " + this.type.getName() + "\n" +
                "Name: " + this.name + "\n" +
                "Level: " + this.level + "/" + PokemonManager.MAX_LEVEL + "\n" +
                "Exp: " + this.exp + "/" + getNeededExp() + "\n" +
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
