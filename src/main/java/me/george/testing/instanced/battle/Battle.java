package me.george.testing.instanced.battle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.Testing;
import me.george.testing.entity.Entity;
import me.george.testing.instanced.InstancedScenario;
import me.george.testing.manager.Manager;
import me.george.testing.parties.Party;
import me.george.testing.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * An instanced, turn-based battle scenario between parties that consist of entities such as players, NPCs and Pokémon.
 * Combatants are strictly Pokémon that may belong to their own, natural party, or to a player's or NPC's team.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Battle implements InstancedScenario {

    private final UUID uuid;

    private final List<Party> parties;
    private final Queue<Entity> turnQueue;
    private final Queue<Turn> pastTurns;

    private volatile Turn currentTurn;

    private volatile State state = State.NOT_STARTED;

    private volatile Party winner;

    @Setter(AccessLevel.PROTECTED)
    private volatile boolean draw;

    protected Battle(Collection<Party> parties) {
        this.uuid = UUID.randomUUID();
        this.parties = new ArrayList<>(parties);
        this.turnQueue = new ConcurrentLinkedQueue<>();
        this.pastTurns = new ConcurrentLinkedQueue<>();
        this.draw = false;
    }

    @Override
    public void start() {
        log("Starting battle between the parties of: " + this.parties.stream()
                .map(party -> party.getLeader().getName())
                .collect(Collectors.joining(", ")));

        setState(State.ACTIVE);
        initializeTurnQueue();
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
    }

    private void initializeTurnQueue() {
        this.parties.stream().map(Party::getCombatants).forEach(this.turnQueue::addAll); // Only combatants take turns, players command them.
    }

    private void processNextTurn() {
        if (!isFinished()) {
            Entity attacker = pollNextAttacker();
            Entity defender = getRandomOpponent(attacker); // Placeholder; a normal battle would include some sort of targeting system like targeting the lowest HP opponent first. This should obviously not deal with the active player's turn, they're supposed to play themselves.
            if (attacker != null && defender != null) {
                // TODO: Handle attack between attacker and defender.
                Testing.logInfo(attacker.getName() + " attacked " + defender.getName()); // debug
                this.currentTurn = new Turn(attacker, defender);
                this.pastTurns.add(this.currentTurn);
                this.turnQueue.add(attacker); // Back of the queue for its next turn.
            }

            checkBattleStatus();
        }
    }

    // Gets the next alive combatant in the queue, dropping the dead ones.
    private Entity pollNextAttacker() {
        Entity attacker = this.turnQueue.poll();
        while (attacker != null && !attacker.isAlive())
            attacker = this.turnQueue.poll();

        return attacker;
    }

    private void checkBattleStatus() {
        long partiesStanding = this.parties.stream().filter(party -> countAliveCombatants(party) > 0).count();
        if (partiesStanding <= 1)
            finish();
    }

    private Entity getRandomOpponent(Entity entity) {
        if (entity == null)
            return null;

        List<Entity> opponents = this.parties.stream()
                .filter(party -> !party.isMember(entity))
                .flatMap(party -> party.getCombatants().stream())
                .filter(Entity::isAlive)
                .toList();

        return opponents.isEmpty() ? null : Utils.randElement(opponents);
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

    private record Turn(Entity attacker, Entity defender) {}
}
