package me.george.testing.manager;

import me.george.testing.Testing;
import me.george.testing.data.storage.supported.PersistenceManager;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.instanced.battle.BattleManager;
import me.george.testing.parties.PartyManager;
import me.george.testing.utils.ReflectionUtil;
import me.george.testing.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a base manager responsible for the handling of a system.
 * Created by George K. on 4/13/2024.
 */
public class Manager {
    private static final List<Manager> managers = new CopyOnWriteArrayList<>();
    private static final Map<Class<? extends Manager>, Manager> INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * Method to be called when the program starts.
     */
    public void onStart() {

    }

    /**
     * Method to be called when the program shuts down.
     */
    public void onStop() {

    }

    /**
     * Register managers.
     */
    private static void register() {
        addManager(PokemonManager.class);
        addManager(PlayerManager.class);
        addManager(CombatManager.class);
        addManager(BattleManager.class);
        addManager(PartyManager.class);
        addManager(PersistenceManager.class);
    }

    /**
     * Enables all managers after registering them, executing their starting hook.
     * This method registers the built-in managers, invokes their {@link #onStart()} hooks and
     * then performs inter-manager relinking (for example restoring Pokemon owner relationships).
     */
    public static void registerManagers() {
        Testing.logInfo("Registering managers...");
        register();
        for (Manager manager : getManagers()) {
            manager.log("Enabling...");
            manager.onStart();
        }

        // Perform cross-manager linking after startup
        relinkPersistentEntities();

        Testing.logInfo("Registered %d managers.", getManagers().size());
    }

    /**
     * Re-links persisted entities loaded from disk across managers.
     * <p>
     * This method will:
     * <ul>
     *     <li>Set each {@link Pokemon}'s in-memory owner reference from its persisted owner UUID (if present).</li>
     *     <li>Attach Pokémon instances to {@link Player} objects based on player's persisted pokemon UUID lists.</li>
     * </ul>
     * It's safe to call after managers have loaded their persisted state.
     */
    public static void relinkPersistentEntities() {
        try {
            Collection<Pokemon> pokemons = PokemonManager.getPokemons();
            int totalPokemons = pokemons.size();
            int linkedFromOwner = 0;
            List<UUID> missingOwners = new ArrayList<>();

            for (Pokemon p : pokemons) {
                UUID ownerUuid = p.getOwnerUuid();
                if (ownerUuid != null) {
                    Player owner = PlayerManager.getPlayer(ownerUuid);
                    if (owner != null) {
                        if (!owner.hasPokemon(p))
                            owner.addPokemon(p);

                        p.setOwner(owner);
                        linkedFromOwner++;
                    } else {
                        missingOwners.add(ownerUuid);
                    }
                }
            }

            Collection<Player> players = PlayerManager.getPlayers();
            int totalPlayers = players.size();
            int linkedFromPlayerUuids = 0;
            List<UUID> missingPokemons = new ArrayList<>();
            int duplicateOwnerConflicts = 0;

            for (Player pl : players) {
                List<UUID> uuids = pl.getPersistedPokemonUuids();
                if (uuids == null || uuids.isEmpty())
                    continue;

                for (UUID uuid : uuids) {
                    Pokemon p = PokemonManager.getPokemon(uuid);
                    if (p == null) {
                        missingPokemons.add(uuid);
                        continue;
                    }

                    if (!pl.hasPokemon(p)) {
                        pl.addPokemon(p);
                        linkedFromPlayerUuids++;
                    }

                    // If the pokemon already had a different owner, log a conflict.
                    Player currentOwner = p.getOwner();
                    if (currentOwner == null) {
                        p.setOwner(pl);
                    } else if (!currentOwner.getUuid().equals(pl.getUuid())) {
                        duplicateOwnerConflicts++;
                        Testing.warn("Relinker: pokemon %s already owned by %s; assigning to %s in memory (no disk change)", p.getUuid(), currentOwner.getName(), pl.getName());
                        p.setOwner(pl);
                    }
                }

                // Clear persisted list after we've reattached runtime instances so serialization uses live set next time.
                try {
                    pl.clearPersistedPokemonUuids();
                } catch (Throwable t) {
                    // shouldn't happen but log defensively.
                    Testing.warn("Failed to clear persisted pokemon UUIDs for player %s: %s", pl.getUuid(), t.getMessage());
                }
            }

            Testing.logInfo("Relinker summary: %d pokemons scanned, %d linked from ownerUuid, %d players scanned, %d linked from player lists, %d missing owner UUIDs, %d missing pokemon UUIDs, %d owner conflicts", totalPokemons, linkedFromOwner, totalPlayers, linkedFromPlayerUuids, missingOwners.size(), missingPokemons.size(), duplicateOwnerConflicts);
            if (!missingOwners.isEmpty())
                Testing.warn("Missing owners for %d ownerUuid(s): %s", missingOwners.size(), missingOwners);

            if (!missingPokemons.isEmpty())
                Testing.warn("Missing pokemons for %d pokemonUuid(s) referenced by players: %s", missingPokemons.size(), missingPokemons);
        } catch (Throwable th) {
            Testing.warn("Failed to relink Pokemon owners: %s", th.getMessage());
        }
    }

    /**
     * Disables all managers after executing their disabling hook.
     * <p>
     * This will call {@link #onStop()} for each registered manager and then clear nothing else.
     */
    public static void disableManagers() {
        Testing.logInfo("Disabling managers...");
        for (Manager manager : getManagers()) {
            manager.log("Disabling...");
            manager.onStop();
        }

        Testing.logInfo("Managers have been disabled.");
    }

    /**
     * Get a manager's instance from its class.
     *
     * @param managerClass The class of the manager to get an instance of.
     * @param <T>          The manager type.
     * @return the manager instance or null if none registered
     */
    @SuppressWarnings("unchecked")
    public static <T extends Manager> T getInstance(Class<T> managerClass) {
        return (T) INSTANCE_MAP.get(managerClass);
    }

    /**
     * Register a manager class.
     * This constructs an instance of the provided manager class and registers it with the
     * central manager list. The manager's {@link #onStart()} will be invoked when
     * {@link #registerManagers()} is called.
     *
     * @param managerClass The manager class to register.
     */
    public static void addManager(Class<? extends Manager> managerClass) {
        addManager(ReflectionUtil.construct(managerClass));
    }

    /**
     * Register a manager instance.
     *
     * @param m The manager instance to add.
     */
    public static void addManager(Manager m) {
        Manager existing = INSTANCE_MAP.putIfAbsent(m.getClass(), m);
        Utils.verify(existing == null, "%s is already registered!", m.getName());

        managers.add(m);
    }

    /**
     * Returns an unmodifiable view of the registered managers.
     *
     * @return managers
     */
    public static List<Manager> getManagers() {
        return Collections.unmodifiableList(managers);
    }

    /**
     * Clears all registered managers. Intended for test isolation — allows tests
     * to reset static state between runs to prevent cross-test contamination.
     */
    public static void clearManagers() {
        managers.clear();
        INSTANCE_MAP.clear();
    }

    /**
     * Gets the simple class name of this manager, can be overridden for custom names.
     *
     * @return managerName
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Logs a message as this manager.
     *
     * @param message The message to log.
     * @param args    The optional arguments to format the message with.
     */
    public void log(String message, Object... args) {
        Testing.logInfo(getName() + " - " + message, args);
    }

    /**
     * Logs a formatted message as a specific manager.
     *
     * @param managerClass The class of the manager to use as a logger source.
     * @param message      The message to log.
     * @param args         The optional arguments to format the message with.
     */
    public static <TManagerClass extends Manager> void log(Class<TManagerClass> managerClass, String message, Object... args) {
        Testing.logInfo(Utils.getSimpleName(managerClass) + " - " + message, args);
    }
}
