package me.george.testing;

import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.manager.Manager;
import me.george.testing.utils.Utils;

/**
 * The root of the program.
 * Created by George K. on 4/12/2024.
 */
public class Testing {

    public static void main(String[] args) {
        PokemonManager.registerManagers();

        Pokemon charmander = PokemonManager.create(PokemonType.CHARMANDER, null);
        charmander.addExp(5000);
        System.out.println(charmander);

        Runtime.getRuntime().addShutdownHook(new Thread(Manager::disableManagers));
    }

    /**
     * Send a warning.
     *
     * @param message The message to send.
     * @param args    The arguments to format the message with.
     */
    public static void warn(String message, Object... args) {
        logInfoOrWarn(message, true, args);
    }

    /**
     * Log information to the console.
     *
     * @param message The message to log.
     */
    public static void logInfo(String message) {
        info(message);
    }

    /**
     * Log information to the console.
     *
     * @param message The message to log.
     * @param args    The arguments to format the message with.
     */
    public static void logInfo(String message, Object... args) {
        String formattedMessage = Utils.format(message, args);
        info("[Testing] " + formattedMessage);
    }

    private static void info(String message) {
        System.out.println(message);
    }

    /**
     * Log information to the console.
     *
     * @param message The message to log.
     * @param args    The arguments to format the message with.
     */
    public static void logInfoOrWarn(String message, boolean shouldWarn, Object... args) {
        if (shouldWarn)
            info("[Warn] " + Utils.format(message, args));
        else
            info("[Testing] " + Utils.format(message, args));
    }

    /**
     * Handles an exception happening, without throwing a new exception.
     *
     * @param exception The exception that occurred.
     * @param message   The reported error message.
     * @param args      Any arguments for the message.
     */
    public static void handleError(Throwable exception, String message, Object... args) {
        exception.printStackTrace(System.out);
        Testing.logInfo("[handleError] " + message, args);
    }
}
