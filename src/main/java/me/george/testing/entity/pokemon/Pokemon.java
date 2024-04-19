package me.george.testing.entity.pokemon;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.Combatant;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.pokemon.stats.PermanentStat;
import me.george.testing.entity.pokemon.stats.PermanentStats;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a base Pokémon of a specific type.
 * Created by George K. on 4/12/2024.
 */
@Getter
public abstract class Pokemon extends Combatant {
    private final PokemonType type;

    @Setter(AccessLevel.PROTECTED)
    private boolean shiny = false;

    @Setter(AccessLevel.PROTECTED)
    private Player owner;

    protected Pokemon(PokemonType type, PermanentStats permanentStats) {
        super(type.getName(), permanentStats);
        this.type = type;
    }

    protected Pokemon(PokemonType type) {
        this(type, null);
    }

    @Override
    public Map<PermanentStat, Integer> getBaseStats() {
        return this.type.getBaseStats();
    }

    @Override
    public void levelUp() {
        super.levelUp();
        calculateStats();
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
            levelExp = (int) (getNeededExp(lastLevel) + Math.pow(lastLevel, 2) + (lastLevel * 24) + 150);
        } else {
            levelExp = 176;
        }

        getCachedExpRequirement()[level - 1] = levelExp;
        return levelExp;
    }

    @Override
    protected int[] getCachedExpRequirement() {
        return new int[PokemonManager.MAX_LEVEL];
    }

    @Override
    protected boolean isMaxLevel() {
        return getLevel() >= PokemonManager.MAX_LEVEL;
    }

    /**
     * Gets the Pokémon's elements based on its type.
     *
     * @return elements
     */
    public Element[] getElements() {
        return this.type.getElements();
    }

    /**
     * Whether this Pokémon is immune to an element.
     *
     * @param element The element in question.
     * @return isImmuneToElement
     */
    protected boolean isImmuneTo(Element element) {
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

    @Override
    public boolean save() {
        return true;
    }

    /**
     * Whether this Pokémon is owned by someone or not.
     *
     * @return pokemonOwned
     */
    public boolean hasOwner() {
        return this.owner != null;
    }

    @Override
    public String toString() {
        return "Pokemon: " + this.type.getName() + "\n" +
                "Name: " + getName() + "\n" +
                "Level: " + getLevel() + "/" + PokemonManager.MAX_LEVEL + "\n" +
                "Exp: " + getExp() + "/" + getNeededExp() + "\n" +
                "Element: " + Arrays.stream(this.type.getElements()).map(Element::getDisplayName).collect(Collectors.joining(", ")) +
                (getPermanentStats() != null ?
                        (
                                "\n" +
                                        "--------" + "\n" +
                                        "Stats: " + "\n"
                                        + getPermanentStats().getStats().entrySet()
                                        .stream()
                                        .map(stat -> stat.getKey().getDisplayName() + ": " + stat.getValue())
                                        .collect(Collectors.joining("\n"))
                        ) :
                        "");
    }
}
