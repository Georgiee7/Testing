package me.george.testing.manager;

import lombok.Getter;
import me.george.testing.Party;
import me.george.testing.Testing;
import me.george.testing.instanced.battle.Battle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles battle-related mechanics.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class BattleManager extends Manager {

    private static final Map<UUID, Battle> ACTIVE_BATTLES = new HashMap<>();

    @Override
    public void onStop() {
        super.onStop();

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
    }
}
