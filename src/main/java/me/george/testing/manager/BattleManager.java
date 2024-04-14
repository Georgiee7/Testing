package me.george.testing.manager;

import lombok.Getter;
import me.george.testing.Testing;
import me.george.testing.instanced.battle.Battle;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles battle-related mechanics.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class BattleManager extends Manager {

    private static final Map<Integer, Battle> ACTIVE_BATTLES = new HashMap<>();

    @Override
    public void onStop() {
        super.onStop();

        Testing.logInfo("Stopping %d battles...", ACTIVE_BATTLES.size());
        ACTIVE_BATTLES.values().forEach(Battle::stop);
    }
}
