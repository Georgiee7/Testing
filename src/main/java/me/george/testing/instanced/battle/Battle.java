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
import java.util.stream.Collectors;

/**
 * Represents an instanced battle scenario.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Battle implements InstancedScenario {

    private final List<Party> parties;

    @Setter
    private boolean paused;

    @Setter
    private boolean finished;

    private Party winner;

    public Battle(Collection<Party> parties) {
        this.parties = (List<Party>) parties;
        start();
    }

    @Override
    public void start() {
        log("Started battle between the parties of: " + this.parties.stream().map(party -> party.getLeader().getName()).collect(Collectors.joining(", ")));
        this.paused = false;
        this.finished = false;
    }

    @Override
    public void pause() {
        this.paused = true;
        log("Battle paused.");
    }

    @Override
    public void unpause() {
        this.paused = false;
        log("Battle unpaused.");
    }

    @Override
    public void finish() {
        this.paused = false;
        this.winner = calculateWinner();
        this.finished = true;
        log("Battle finished with winning party: " + this.winner.getPlayers().stream().map(Player::getName).collect(Collectors.joining(", ")));
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
     * @param args The arguments to format the message with.
     */
    protected void log(String message, Object... args) {
        Manager.getInstance(BattleManager.class).log(message, args);
    }
}
