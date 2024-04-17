package me.george.testing.instanced.battle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.Testing;
import me.george.testing.entity.Entity;
import me.george.testing.instanced.InstancedScenario;
import me.george.testing.manager.Manager;
import me.george.testing.parties.Party;

import java.util.*;
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
    private final List<Turn> pastTurns;

    private Turn currentTurn;

    @Setter(AccessLevel.PROTECTED)
    private State state;

    private Party winner;

    @Setter(AccessLevel.PROTECTED)
    private boolean draw;

    public Battle(Collection<Party> parties) {
        this.uuid = UUID.randomUUID();
        this.parties = (List<Party>) parties;
        this.turnQueue = new LinkedList<>();
        this.pastTurns = new ArrayList<>();
        this.draw = false;
        start();
    }

    @Override
    public void start() {
        log("Started battle between the parties of: " + this.parties.stream().map(party -> party.getLeader().getName()).collect(Collectors.joining(", ")));
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
        this.winner = calculateWinner();
        if (this.winner == null)
            this.draw = true;

        setState(State.FINISHED);
        log(!this.draw ? "Battle finished with winning party: " + this.winner.getMembers()
                .stream()
                .map(Entity::getName)
                .collect(Collectors.joining(", "))
                : "The battle ended in a draw.");
    }

    private void initializeTurnQueue() {
        this.parties.stream().map(Party::getMembers).forEach(this.turnQueue::addAll);
    }

    private void processNextTurn() {
        if (!isFinished()) {
            Entity attacker = this.turnQueue.poll();
            Entity defender = getRandomOpponent(attacker); // Placeholder; a normal battle would include some sort of targeting system like targeting the lowest HP opponent first. This should obviously not deal with the active player's turn, they're supposed to play themselves.
            if (attacker != null && defender != null) {
                // TODO: Handle attack between attacker and defender.
                Testing.logInfo(attacker.getName() + " attacked " + defender.getName()); // debug
                this.currentTurn = new Turn(attacker, defender);
                this.pastTurns.add(this.currentTurn);
            }

            checkBattleStatus();
        }
    }

    private void checkBattleStatus() {
        for (Party party : this.parties) {
            if (party.getMembers().isEmpty()) {
                this.winner = party;
                finish();
                break;
            }
        }
    }

    private Entity getRandomOpponent(Entity entity) {
        Party opponentParty = this.parties.stream()
                .filter(party -> !party.isMember(entity))
                .findAny()
                .orElse(null);

        if (opponentParty != null) {
            List<Entity> opponentEntities = opponentParty.getMembers();
            Random random = new Random();
            return opponentEntities.get(random.nextInt(opponentEntities.size()));
        }

        return null;
    }

    /**
     * Calculates the battle's winning party based on the number of alive combatants.
     *
     * @return winningParty
     */
    protected Party calculateWinner() {
        Party winningParty = null;
        int maxAliveCombatants = Integer.MIN_VALUE;
        for (Party party : this.parties) {
            int aliveCombatants = countAliveCombatants(party);
            if (aliveCombatants > maxAliveCombatants) {
                maxAliveCombatants = aliveCombatants;
                winningParty = party;
            }
        }

        return winningParty;
    }

    /**
     * Gets the total amount of turns this battle has been going on for.
     *
     * @return totalTurns
     */
    protected int getTotalTurns() {
        return this.turnQueue.size();
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
        Manager.getInstance(BattleManager.class).log(message, args);
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

    public enum State {
        ACTIVE,
        PAUSED,
        FINISHED
    }

    private record Turn(Entity attacker, Entity defender) {}
}
