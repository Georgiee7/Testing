package me.george.testing.challenge;

import me.george.testing.entity.player.Player;
import me.george.testing.instanced.battle.event.BattleEventManager;
import me.george.testing.instanced.battle.event.RegisteredListener;
import me.george.testing.manager.Manager;
import me.george.testing.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles the challenges players are working on, passing them what players do in battles.
 * It only listens to battle events, so battles don't know challenges exist.
 * Created by George K. on 11/27/2026.
 */
public class ChallengeManager extends Manager {

    private static final Map<UUID, List<Challenge>> CHALLENGES_BY_PLAYER = new ConcurrentHashMap<>();
    private static final List<RegisteredListener<?>> LISTENERS = new CopyOnWriteArrayList<>();

    @Override
    public void onStart() {
        LISTENERS.addAll(BattleEventManager.register(new ChallengeListener()));
    }

    @Override
    public void onStop() {
        LISTENERS.forEach(BattleEventManager::unlisten);
        LISTENERS.clear();
        CHALLENGES_BY_PLAYER.clear();
    }

    /**
     * Gives a player a challenge to work on, activating it.
     *
     * @param player    The player to give it to.
     * @param challenge The challenge, which must not be given to anyone else, as it holds its own progress.
     */
    public static void give(Player player, Challenge challenge) {
        Utils.verifyNotNull(player, "Tried to give a challenge to a null player.");
        Utils.verifyNotNull(challenge, "Tried to give a null challenge.");
        CHALLENGES_BY_PLAYER.computeIfAbsent(player.getUuid(), uuid -> new CopyOnWriteArrayList<>()).add(challenge);
        synchronized (challenge) {
            challenge.activate();
        }
    }

    /**
     * Removes a challenge from a player.
     *
     * @param player    The player to remove it from.
     * @param challenge The challenge to remove.
     */
    public static void remove(Player player, Challenge challenge) {
        List<Challenge> challenges = CHALLENGES_BY_PLAYER.get(player.getUuid());
        if (challenges != null)
            challenges.remove(challenge);
    }

    /**
     * Gets the challenges a player has, done or not.
     *
     * @param player The player.
     * @return challenges
     */
    public static List<Challenge> getChallenges(Player player) {
        List<Challenge> challenges = CHALLENGES_BY_PLAYER.get(player.getUuid());
        return challenges != null ? Collections.unmodifiableList(challenges) : List.of();
    }

    /**
     * Passes an action to the challenges of the player who did it, the ones that use actions of its type.
     *
     * @param action The action that was done.
     */
    public static void offer(ChallengeAction action) {
        if (action.player() == null)
            return; // Done by a wild Pokémon.

        List<Challenge> challenges = CHALLENGES_BY_PLAYER.get(action.player().getUuid());
        if (challenges == null)
            return;

        for (Challenge challenge : challenges) {
            if (challenge.isComplete() || !challenge.listensTo(action.type()))
                continue;

            synchronized (challenge) { // A player can be in more than one battle at a time, on different threads.
                challenge.offer(action);
            }
        }
    }
}
