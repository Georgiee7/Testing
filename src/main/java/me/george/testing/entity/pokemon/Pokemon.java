package me.george.testing.entity.pokemon;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.combatant.stats.PermanentStat;
import me.george.testing.entity.combatant.stats.PermanentStats;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Represents a base Pokémon of a specific type.
 * Created by George K. on 4/12/2024.
 */
@Getter
public abstract class Pokemon extends Combatant {
    private static final int[] EXP_REQUIREMENTS = new int[PokemonManager.MAX_LEVEL]; // Shared by all Pokémon, filled in as levels are reached.

    public static final int MAX_MOVES = 4;

    private final PokemonType type;

    private final List<MoveType> moves;

    @Setter(AccessLevel.PROTECTED)
    private boolean shiny = false;

    @Setter(AccessLevel.PROTECTED)
    private Player owner;

    protected Pokemon(PokemonType type, PermanentStats permanentStats) {
        super(type.getName(), permanentStats);
        this.type = type;
        this.moves = new CopyOnWriteArrayList<>(MoveType.getStartingMoves(type.getElements()));
        initializeStats();
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
        int previousHP = getHP();
        super.levelUp();
        calculateStats();
        if (isAlive())
            setHealth(getHealth() + getHP() - previousHP); // Keeps the damage taken, like the games do.
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
        return EXP_REQUIREMENTS;
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
     * Whether this Pokémon has an element.
     *
     * @param element The element in question.
     * @return hasElement
     */
    public boolean hasElement(Element element) {
        return Utils.contains(getElements(), element);
    }

    /**
     * Gets the moves this Pokémon knows.
     *
     * @return moves
     */
    public List<MoveType> getMoves() {
        return Collections.unmodifiableList(this.moves);
    }

    /**
     * Teaches this Pokémon a move, if it doesn't know it and knows less than the maximum.
     *
     * @param move The move to learn.
     * @return learnedMove
     */
    public boolean learnMove(MoveType move) {
        Utils.verifyNotNull(move, "Tried to teach a Pokemon a null move.");
        if (this.moves.size() >= MAX_MOVES || this.moves.contains(move))
            return false;

        return this.moves.add(move);
    }

    /**
     * Makes this Pokémon forget a move.
     *
     * @param move The move to forget.
     * @return forgotMove
     */
    public boolean forgetMove(MoveType move) {
        return this.moves.remove(move);
    }

    /**
     * Picks the move to use on its turn.
     * Placeholder; picks at random, a player's Pokémon would be told which one by its player.
     *
     * @return chosenMove
     */
    public MoveType chooseMove() {
        return this.moves.isEmpty() ? MoveType.TACKLE : Utils.randElement(this.moves);
    }

    /**
     * Whether this Pokémon is immune to an element.
     *
     * @param element The element in question.
     * @return isImmuneToElement
     */
    protected boolean isImmuneTo(Element element) {
        return Arrays.stream(getElements()).anyMatch(element::hasNoEffectOn);
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
        return Arrays.stream(getElements()).anyMatch(e -> e.isWeakAgainst(element));
    }

    /**
     * Whether this Pokémon is normal against an element.
     *
     * @param element The element in question.
     * @return isNormalAgainstElement
     */
    public boolean isNormalAgainst(Element element) {
        return Arrays.stream(getElements()).anyMatch(e -> e.isNormalAgainst(element));
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
