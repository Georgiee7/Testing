package me.george.testing;

import me.george.testing.pokemon.Pokemon;
import me.george.testing.pokemon.PokemonManager;
import me.george.testing.pokemon.PokemonType;

/**
 * The root of the program.
 * Created by George K. on 4/12/2024.
 */
public class Testing {

    public static void main(String[] args) {
        Pokemon charmander = PokemonManager.create(PokemonType.CHARMANDER, null);
        System.out.println(charmander);
    }
}

