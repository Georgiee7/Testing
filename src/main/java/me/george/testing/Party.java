package me.george.testing;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.List;

/**
 * Represents a team/party that consists of players, which have their own teams of Pokémon.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Party {

    private final List<Player> players;

    @Setter
    private Player leader;

    public Party(Collection<Player> players) {
        this.players = (List<Player>) players;
        this.leader = Utils.randElement(this.players);
    }

    public Party(Collection<Player> players, Player leader) {
        this.players = (List<Player>) players;
        this.leader = leader;
    }

    /**
     * Adds a player to the party.
     *
     * @param player The player to add.
     */
    public void addPlayer(Player player) {
        this.players.add(player);
    }

    /**
     * Removes a player from the party.
     *
     * @param player The player to remove.
     */
    public void removePlayer(Player player) {
        this.players.remove(player);
    }
}
