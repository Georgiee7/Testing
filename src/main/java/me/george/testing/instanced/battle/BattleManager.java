package me.george.testing.instanced.battle;

import me.george.testing.parties.Party;
import me.george.testing.Testing;
import me.george.testing.manager.Manager;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Handles battle-related mechanics.
 * Created by George K. on 4/14/2024.
 */
public class BattleManager extends Manager {

    private static final Map<UUID, Battle> CACHED_BATTLES = new HashMap<>();

    @Override
    public void onStop() {
        int battles = CACHED_BATTLES.size();
        Testing.logInfo("Stopping %d battles...", battles);
        new ArrayList<>(CACHED_BATTLES.values()).forEach(Battle::stop);
        Testing.logInfo("Stopped %d battles.", battles);
    }

    /**
     * Creates a new battle between parties.
     *
     * @param parties The parties involved in the battle.
     * @return newBattle
     */
    public static Battle createBattle(Collection<Party> parties) {
        Utils.verifyNotNull(parties, "Tried to create a battle between invalid parties.");
        Utils.verify(!parties.isEmpty(), "Tried to create a battle with no parties.");
        Utils.verify(parties.size() >= 2, "Tried to create a battle with less than 2 parties.");

        Battle battle = new Battle(parties);
        CACHED_BATTLES.put(battle.getUuid(), battle);
        return battle;
    }

    /**
     * Creates and immediately starts a new battle between parties.
     *
     * @param parties The parties involved in the battle.
     * @return newStartedBattle
     */
    public static Battle createAndStartBattle(Collection<Party> parties) {
        Battle battle = createBattle(parties);
        start(battle);
        return battle;
    }

    /**
     * Starts a battle between parties.
     *
     * @param battle The battle to start.
     */
    public static void start(Battle battle) {
        updateBattle(battle, Battle::start);
    }

    /**
     * Pauses a battle.
     *
     * @param battle The battle to pause.
     */
    public static void pause(Battle battle) {
        updateBattle(battle, Battle::pause);
    }

    /**
     * Resumes a battle.
     *
     * @param battle The battle to resume.
     */
    public static void resume(Battle battle) {
        updateBattle(battle, Battle::resume);
    }

    /**
     * Sets a battle to a finished state.
     *
     * @param battle The battle to finish.
     */
    public static void finish(Battle battle) {
        updateBattle(battle, Battle::finish);
    }

    /**
     * Stops a battle immediately calling its stop hook.
     *
     * @param battle The battle to stop.
     */
    public static void stop(Battle battle) {
        updateBattle(battle, Battle::stop);
    }

    private static void updateBattle(Battle battle, Consumer<Battle> battleConsumer) {
        Utils.verifyNotNull(battle, "Tried to update an invalid battle.");
        Utils.verifyNotNull(battleConsumer, "Tried to update a battle with an invalid consumer.");
        Utils.verify(!battle.isFinished(), "Tried to update a finished battle.");

        battleConsumer.accept(battle);
    }

    /**
     * Gets all cached battles as an unmodifiable snapshot.
     *
     * @return cachedBattles
     */
    public static Collection<Battle> getBattles() {
        return Collections.unmodifiableCollection(new ArrayList<>(CACHED_BATTLES.values()));
    }

    /**
     * Stops all cached battles.
     */
    public static void stopBattles() {
        new ArrayList<>(CACHED_BATTLES.values()).stream().filter(battle -> !battle.isFinished()).forEach(BattleManager::stop);
    }
}
