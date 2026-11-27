package me.george.testing.entity.pokemon.spec;

import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.entity.pokemon.box.PokemonBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests parsing Pokémon specs, matching Pokémon with them and creating Pokémon from them.
 * Created by George K. on 11/27/2026.
 */
public class PokemonSpecTest {

    @Test
    public void testParsing() {
        PokemonSpec spec = PokemonSpec.parse("3xCHARMANDER[level=5-10, shiny=true]");
        assertEquals(3, spec.getAmount());
        assertTrue(spec.getType().contains(PokemonType.CHARMANDER));
        assertTrue(spec.getLevel().contains(7));
        assertTrue(spec.getShiny());
        assertEquals("3xCHARMANDER[level=5-10,shiny=true]", spec.toString());

        PokemonSpec anySpec = PokemonSpec.parse("*[element={FIRE,WATER},level>=30]");
        assertEquals(anySpec.toString(), PokemonSpec.parse(anySpec.toString()).toString()); // Writing it back parses to the same spec.
    }

    @Test
    public void testMatching() {
        PokemonSpec spec = PokemonSpec.parse("*[element={FIRE,WATER}, level>=10]");
        assertFalse(spec.matches(createPokemon(PokemonType.CHARMANDER, 9)));
        assertTrue(spec.matches(createPokemon(PokemonType.CHARMANDER, 10)));
        assertTrue(spec.matches(createPokemon(PokemonType.SQUIRTLE, 12)));
        assertFalse(spec.matches(createPokemon(PokemonType.PIKACHU, 12)));
        assertFalse(PokemonSpec.parse("CHARMANDER[shiny=true]").matches(createPokemon(PokemonType.CHARMANDER, 1)));
    }

    @Test
    public void testCreating() {
        List<Pokemon> pokemons = PokemonSpec.parse("2x{PIKACHU,SQUIRTLE}[level=5-10,shiny=true]").create();
        assertEquals(2, pokemons.size());
        for (Pokemon pokemon : pokemons) {
            assertTrue(pokemon.getType() == PokemonType.PIKACHU || pokemon.getType() == PokemonType.SQUIRTLE);
            assertTrue(pokemon.getLevel() >= 5 && pokemon.getLevel() <= 10);
            assertTrue(pokemon.isShiny());
        }

        for (Pokemon pokemon : PokemonSpec.parse("5x*[element=FIRE]").create())
            assertTrue(pokemon.hasElement(Element.FIRE)); // Only types with the element are picked.
    }

    @Test
    public void testDescribing() {
        assertEquals("shiny Fire or Water Pokémon at level 30+", PokemonSpec.parse("*[element={FIRE,WATER},level>=30,shiny=true]").describe());
        assertEquals("3 Charmander at level 5-10", PokemonSpec.parse("3xCHARMANDER[level=5-10]").describe());
    }

    @Test
    public void testUsage() {
        String usage = PokemonSpec.getUsage();
        assertTrue(usage.contains("level"));
        assertTrue(usage.contains("element"));
        assertTrue(usage.contains("shiny"));
    }

    @Test
    public void testInvalidSpecs() {
        assertThrows(Throwable.class, () -> PokemonSpec.parse("MISSINGNO"));
        assertThrows(Throwable.class, () -> PokemonSpec.parse("CHARMANDER[speed=5]")); // Not an argument.
        assertThrows(Throwable.class, () -> PokemonSpec.parse("CHARMANDER[level=5,level=6]"));
        assertThrows(Throwable.class, () -> PokemonSpec.parse("CHARMANDER[shiny=maybe]"));
        assertThrows(Throwable.class, () -> PokemonSpec.parse("0xCHARMANDER"));
        assertThrows(Throwable.class, () -> PokemonSpec.parse("CHARMANDER[level>=30]").create()); // Can only match.
        assertThrows(Throwable.class, () -> PokemonSpec.parse("PIKACHU[element=FIRE]").create()); // No such Pikachu.
    }

    @Test
    public void testBoxSearch() {
        Pokemon charmander = createPokemon(PokemonType.CHARMANDER, 15);
        createPokemon(PokemonType.PIKACHU, 15);

        PokemonBox box = PokemonBox.ofEveryPokemon(null);
        box.setSearch(PokemonSpec.parse("*[element=FIRE]"));
        assertEquals(List.of(charmander), box.getPokemons());
    }

    private static Pokemon createPokemon(PokemonType type, int level) {
        Pokemon pokemon = PokemonManager.create(type, null);
        while (pokemon.getLevel() < level)
            pokemon.addExp(pokemon.getNeededExp() - pokemon.getExp());

        return pokemon;
    }

    @AfterEach
    public void tearDown() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
