package me.george.testing.instanced.battle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.Entity;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.entity.pokemon.move.base.DamagingMove;
import me.george.testing.entity.pokemon.move.base.Move;
import me.george.testing.instanced.InstancedScenario;
import me.george.testing.instanced.battle.damage.DamageCalculation;
import me.george.testing.instanced.battle.damage.DamagePipeline;
import me.george.testing.instanced.battle.event.BattleEventManager;
import me.george.testing.instanced.battle.event.BattleFinishEvent;
import me.george.testing.instanced.battle.event.BattleStartEvent;
import me.george.testing.instanced.battle.event.DamageEvent;
import me.george.testing.instanced.battle.event.FaintEvent;
import me.george.testing.instanced.battle.event.MoveUseEvent;
import me.george.testing.instanced.battle.event.TurnStartEvent;
import me.george.testing.manager.Manager;
import me.george.testing.parties.Party;
import me.george.testing.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An instanced, turn-based battle scenario between parties that consist of entities such as players, NPCs and Pokémon.
 * Combatants are strictly Pokémon that may belong to their own, natural party, or to a player's or NPC's team.
 * The battle plays in rounds: every alive Pokémon gets a turn each round, fastest first.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Battle implements InstancedScenario {

    private final UUID uuid;

    private final List<Party> parties;
    private final Queue<Pokemon> turnQueue; // The Pokémon yet to play this round, in order.
    private final Queue<Turn> pastTurns;

    private final DamagePipeline damagePipeline; // This battle's own, so it can have modifiers other battles don't.

    private volatile Turn currentTurn;

    private volatile int round;

    private volatile State state = State.NOT_STARTED;

    private volatile Party winner;

    @Setter(AccessLevel.PROTECTED)
    private volatile boolean draw;

    protected Battle(Collection<Party> parties) {
        this.uuid = UUID.randomUUID();
        this.parties = new ArrayList<>(parties);
        this.turnQueue = new ConcurrentLinkedQueue<>();
        this.pastTurns = new ConcurrentLinkedQueue<>();
        this.damagePipeline = DamagePipeline.createDefault();
        this.draw = false;
    }

    @Override
    public void start() {
        if (BattleEventManager.call(new BattleStartEvent(this)).isCancelled()) {
            log("Battle start was cancelled.");
            return;
        }

        log("Starting battle between the parties of: " + this.parties.stream()
                .map(party -> party.getLeader().getName())
                .collect(Collectors.joining(", ")));

        setState(State.ACTIVE);
        processNextTurn();
    }

    @Override
    public void pause() {
        setState(State.PAUSED);
        log("Battle paused.");
    }

    @Override
    public void resume() {
        setState(State.ACTIVE);
        log("Battle resumed.");
        processNextTurn();
    }

    @Override
    public void finish() {
        if (isFinished())
            return; // Finishing twice (such as stopping a battle that just ended) does nothing.

        this.winner = calculateWinner();
        if (this.winner == null)
            this.draw = true;

        setState(State.FINISHED);
        BattleManager.removeBattle(this);
        log((!this.draw ? "Battle finished with winning party:\n" + this.winner.getMembers()
                .stream()
                .map(Entity::getName)
                .collect(Collectors.joining(", "))
                : "The battle ended in a draw.")
                + "\nTotal Turns: " + getTotalTurns());

        BattleEventManager.call(new BattleFinishEvent(this));
    }

    /**
     * Plays the next turn. A server would call this on a timer, or once the player whose turn it is picks their move.
     */
    public void nextTurn() {
        Utils.verify(isActive(), "Tried to play a turn of a battle that isn't active.");
        processNextTurn();
    }

    /**
     * Plays the battle out on its own, until it finishes or reaches a number of turns. If it hasn't finished by then, it's finished
     * with the party that has the most alive combatants winning. Does nothing if it already finished.
     *
     * @param maxTurns The most turns to play.
     */
    public void simulate(int maxTurns) {
        if (isFinished())
            return; // It can end on its own at any turn, such as the first one played when it started.

        Utils.verify(isActive(), "Tried to simulate a battle that isn't active.");
        for (int turn = 0; turn < maxTurns && isActive(); turn++)
            processNextTurn();

        finish();
    }

    private void processNextTurn() {
        if (!isActive())
            return;

        if (this.turnQueue.isEmpty())
            startNextRound();

        Pokemon attacker = pollNextAttacker();
        Pokemon defender = getRandomOpponent(attacker); // Placeholder; a normal battle would include some sort of targeting system like targeting the lowest HP opponent first. This should obviously not deal with the active player's turn, they're supposed to play themselves.
        if (attacker != null && defender != null) {
            this.currentTurn = playTurn(attacker, defender);
            this.pastTurns.add(this.currentTurn);
        }

        checkBattleStatus();
    }

    // Starts a new round, in which every alive Pokémon gets a turn, fastest first. Ties are random, like the games.
    private void startNextRound() {
        List<Pokemon> pokemon = new ArrayList<>(this.parties.stream().flatMap(Battle::getAlivePokemon).toList());
        Collections.shuffle(pokemon, Utils.getRandom());
        pokemon.sort(Comparator.comparingInt(Pokemon::getSpeed).reversed());

        this.round++;
        this.turnQueue.addAll(pokemon);
    }

    // Plays a Pokémon's turn: picking a move and using it on the defender, letting listeners change or stop each step.
    private Turn playTurn(Pokemon attacker, Pokemon defender) {
        if (BattleEventManager.call(new TurnStartEvent(this, attacker)).isCancelled())
            return new Turn(attacker, defender, null, 0); // Skipped.

        MoveType moveType = attacker.chooseMove();
        Move move = moveType.create(attacker, defender);
        if (BattleEventManager.call(new MoveUseEvent(this, attacker, defender, move)).isCancelled() || !move.perform())
            return new Turn(attacker, defender, moveType, 0); // Stopped or failed.

        if (!(move instanceof DamagingMove))
            return new Turn(attacker, defender, moveType, 0);

        DamageCalculation calculation = this.damagePipeline.calculate(attacker, defender, move);
        DamageEvent damageEvent = BattleEventManager.call(new DamageEvent(this, calculation));
        if (damageEvent.isCancelled())
            return new Turn(attacker, defender, moveType, 0);

        defender.decreaseHP(damageEvent.getDamage());
        log("%s took %d damage%s.", defender.getName(), damageEvent.getDamage(), calculation.isCritical() ? " (critical hit)" : "");
        if (!defender.isAlive()) {
            log("%s fainted!", defender.getName());
            BattleEventManager.call(new FaintEvent(this, defender, attacker));
        }

        return new Turn(attacker, defender, moveType, damageEvent.getDamage());
    }

    // Gets the next alive Pokémon in the queue, dropping the ones that fainted before their turn.
    private Pokemon pollNextAttacker() {
        Pokemon attacker = this.turnQueue.poll();
        while (attacker != null && !attacker.isAlive())
            attacker = this.turnQueue.poll();

        return attacker;
    }

    private void checkBattleStatus() {
        long partiesStanding = this.parties.stream().filter(party -> countAliveCombatants(party) > 0).count();
        if (partiesStanding <= 1)
            finish();
    }

    private Pokemon getRandomOpponent(Pokemon pokemon) {
        if (pokemon == null)
            return null;

        List<Pokemon> opponents = this.parties.stream()
                .filter(party -> !party.isMember(pokemon))
                .flatMap(Battle::getAlivePokemon)
                .toList();

        return opponents.isEmpty() ? null : Utils.randElement(opponents);
    }

    // The alive Pokémon of a party, the combatants that take turns.
    private static Stream<Pokemon> getAlivePokemon(Party party) {
        return party.getCombatants().stream()
                .filter(Pokemon.class::isInstance)
                .map(Pokemon.class::cast)
                .filter(Entity::isAlive);
    }

    /**
     * Calculates the battle's winning party based on the number of alive combatants.
     * Returns null for a draw, when no party has more alive combatants than all the others.
     *
     * @return winningParty
     */
    protected Party calculateWinner() {
        Party winningParty = null;
        int maxAliveCombatants = 0;
        boolean tied = false;
        for (Party party : this.parties) {
            int aliveCombatants = countAliveCombatants(party);
            if (aliveCombatants > maxAliveCombatants) {
                maxAliveCombatants = aliveCombatants;
                winningParty = party;
                tied = false;
            } else if (aliveCombatants == maxAliveCombatants) {
                tied = true;
            }
        }

        return tied ? null : winningParty;
    }

    /**
     * Gets the total amount of turns this battle has been going on for.
     *
     * @return totalTurns
     */
    protected int getTotalTurns() {
        return this.pastTurns.size();
    }

    /**
     * Counts the number of alive combatants in a party.
     *
     * @param party The party to count alive combatants for.
     * @return The number of alive combatants.
     */
    protected int countAliveCombatants(Party party) {
        return (int) party.getCombatants().stream().filter(Entity::isAlive).count();
    }

    /**
     * Logs a message as this battle, handled.
     *
     * @param message The message to log.
     * @param args    The arguments to format the message with.
     */
    protected void log(String message, Object... args) {
        Manager.log(BattleManager.class, message, args);
    }

    /**
     * Whether the battle is in a specific state.
     *
     * @param state The state to check.
     * @return isInState
     */
    public boolean isState(State state) {
        return this.state == state;
    }

    /**
     * Whether the battle has started or not.
     *
     * @return battleHasStarted
     */
    public boolean hasStarted() {
        return !isState(State.NOT_STARTED);
    }

    /**
     * Whether the battle is active or not.
     *
     * @return battleIsActive
     */
    public boolean isActive() {
        return isState(State.ACTIVE);
    }

    /**
     * Whether the battle is paused or not.
     *
     * @return battleIsPaused
     */
    public boolean isPaused() {
        return isState(State.PAUSED);
    }

    /**
     * Whether the battle is finished or not.
     *
     * @return battleIsFinished
     */
    public boolean isFinished() {
        return isState(State.FINISHED);
    }

    /**
     * Sets the battle's state to a new state.
     *
     * @param newState The new state to set the battle to.
     */
    protected void setState(State newState) {
        Utils.verify(!newState.equals(State.NOT_STARTED), "Tried to set an initialized battle's state to '%s'", State.NOT_STARTED);
        this.state = newState;
    }

    /**
     * Possible states a battle may be in.
     */
    public enum State {
        NOT_STARTED,
        ACTIVE,
        PAUSED,
        FINISHED
    }

    /**
     * A turn that was played.
     *
     * @param attacker The Pokémon whose turn it was.
     * @param defender The Pokémon it targeted.
     * @param move     The move it used, null if its turn was skipped.
     * @param damage   The damage it dealt.
     */
    public record Turn(Pokemon attacker, Pokemon defender, MoveType move, int damage) {}
}
