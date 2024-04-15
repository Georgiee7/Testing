package me.george.testing.instanced.battle;

import lombok.Getter;
import me.george.testing.parties.Party;
import me.george.testing.Testing;
import me.george.testing.manager.Manager;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handles battle-related mechanics.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class BattleManager extends Manager {

    private static final Map<UUID, Battle> ACTIVE_BATTLES = new HashMap<>();

    @Override
    public void onStop() {
        Testing.logInfo("Stopping %d battles...", ACTIVE_BATTLES.size());
        ACTIVE_BATTLES.values().forEach(Battle::stop);
    }

    /**
     * Starts a battle between parties.
     *
     * @param parties The parties involved in the battle.
     */
    public static void startBattle(Collection<Party> parties) {
        Battle battle = new Battle(parties);
        ACTIVE_BATTLES.put(battle.getUuid(), battle);
        updateBattle(battle, Battle::start);
    }

    /**
     * Updates a battle's state.
     *
     * @param battle The battle to update.
     * @param battleConsumer The operation to perform on the battle.
     */
    public static void updateBattle(Battle battle, Consumer<Battle> battleConsumer) {
        Utils.verifyNotNull(battle, "Tried to update an invalid battle.");
        Utils.verifyNotNull(battleConsumer, "Tried to update a battle with an invalid consumer.");
        Utils.verify(!battle.isFinished(), "Tried to update a finished battle.");

        battleConsumer.accept(battle);
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
}
