package me.george.testing.parties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.player.Player;

import java.util.Collection;
import java.util.List;

/**
 * Represents a team/party that consists of players, which have their own teams of Pokémon.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Party {

    private final List<Player> players;

    @Setter(AccessLevel.PROTECTED)
    private Player leader;

    protected Party(Collection<Player> players, Player leader) {
        this.players = (List<Player>) players;
        this.leader = leader;
    }

    /**
     * Adds a player to the party.
     *
     * @param player The player to add.
     */
    protected void addPlayer(Player player) {
        this.players.add(player);
    }

    /**
     * Removes a player from the party.
     *
     * @param player The player to remove.
     */
    protected void removePlayer(Player player) {
        this.players.remove(player);
    }

    /**
     * Whether a player is a member of the party or not.
     *
     * @param player The player in question.
     * @return isPartyMember
     */
    protected boolean isMember(Player player) {
        return this.players.contains(player);
    }

    /**
     * Whether a player is the leader of the party.
     *
     * @param player The player in question.
     * @return isPartyLeader
     */
    protected boolean isLeader(Player player) {
        return isMember(player) && this.leader == player;
    }

    /**
     * Gets the amount of player members in the party.
     *
     * @return memberCount
     */
    protected int getSize() {
        return getPlayers().size();
    }
}
