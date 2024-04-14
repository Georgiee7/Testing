package me.george.testing.instanced.battle;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.Party;
import me.george.testing.Player;
import me.george.testing.instanced.InstancedScenario;
import me.george.testing.manager.BattleManager;
import me.george.testing.manager.Manager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents an instanced battle scenario.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Battle implements InstancedScenario {

    private final UUID uuid;

    private final List<Party> parties;

    @Setter
    private State state;

    private Party winner;

    @Setter
    private boolean draw;

    public Battle(Collection<Party> parties) {
        this.uuid = UUID.randomUUID();
        this.parties = (List<Party>) parties;
        this.draw = false;
        start();
    }

    @Override
    public void start() {
        log("Started battle between the parties of: " + this.parties.stream().map(party -> party.getLeader().getName()).collect(Collectors.joining(", ")));
        setState(State.ACTIVE);
    }

    @Override
    public void pause() {
        setState(State.PAUSED);
        log("Battle paused.");
    }

    @Override
    public void unpause() {
        setState(State.ACTIVE);
        log("Battle unpaused.");
    }

    @Override
    public void finish() {
        this.winner = calculateWinner();

        if (winner == null)
            this.draw = true;

        setState(State.FINISHED);
        log(!this.draw ? "Battle finished with winning party: " + this.winner.getPlayers().stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "))
                : "The battle ended in a draw.");
    }

    /**
     * Calculates the battle's winning party.
     *
     * @return winningParty
     */
    protected Party calculateWinner() {
        for (Party party : this.parties) {
            boolean hasAlivePokemon = party.getPlayers().stream()
                    .anyMatch(player -> !player.getPokemons().isEmpty());

            if (hasAlivePokemon)
                return party;
        }

        return null;
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
}
