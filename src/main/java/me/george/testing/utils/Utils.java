package me.george.testing.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class of static utilities.
 * Created by George K. on 4/13/2024.
 */
public class Utils {

    /**
     * Capitalize every letter after a space.
     * @param sentence The sentence to capitalize.
     * @return capitalized
     */
    public static String capitalize(String sentence) {
        String[] split = sentence.replaceAll("_", " ").split(" ");
        List<String> out = new ArrayList<>();
        for (String s : split)
            out.add(!s.isEmpty() ? s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() : "");

        return String.join(" ", out);
    }
}
