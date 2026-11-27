package me.george.testing.entity.pokemon.box;

import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.entity.pokemon.box.PokemonFilter.HealthStatus;
import me.george.testing.entity.pokemon.box.PokemonFilter.LevelRange;
import me.george.testing.entity.pokemon.box.PokemonFilter.Ownership;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests filtering and sorting Pokémon in a box.
 * Created by George K. on 11/27/2026.
 */
public class PokemonBoxTest {
    private final Player MOCK_OWNER = PlayerManager.create("BOX_OWNER");

    private final Pokemon BULBASAUR = givePokemon(PokemonType.BULBASAUR, 12);
    private final Pokemon CHARMANDER = givePokemon(PokemonType.CHARMANDER, 5);
    private final Pokemon CHARIZARD = givePokemon(PokemonType.CHARIZARD, 40);
    private final Pokemon SQUIRTLE = givePokemon(PokemonType.SQUIRTLE, 30);
    private final Pokemon PIKACHU = givePokemon(PokemonType.PIKACHU, 60);

    private final PokemonBox MOCK_BOX = PokemonBox.of(MOCK_OWNER);

    @Test
    public void testShowsEveryPokemonByDefault() {
        assertEquals(List.of(BULBASAUR, CHARMANDER, CHARIZARD, SQUIRTLE, PIKACHU), MOCK_BOX.getPokemons()); // Pokédex order.
    }

    @Test
    public void testElementFilter() {
        MOCK_BOX.setFilterValue(PokemonFilter.ELEMENT, Element.FIRE);
        assertEquals(List.of(CHARMANDER, CHARIZARD), MOCK_BOX.getPokemons());

        MOCK_BOX.setFilterValue(PokemonFilter.ELEMENT, Element.FLYING); // Charizard's second element.
        assertEquals(List.of(CHARIZARD), MOCK_BOX.getPokemons());
    }

    @Test
    public void testLevelFilter() {
        MOCK_BOX.setFilterValue(PokemonFilter.LEVEL, LevelRange.LEVEL_26_TO_50);
        assertEquals(List.of(CHARIZARD, SQUIRTLE), MOCK_BOX.getPokemons());
    }

    @Test
    public void testStatusFilter() {
        SQUIRTLE.decreaseHP(1);
        PIKACHU.kill();

        MOCK_BOX.setFilterValue(PokemonFilter.STATUS, HealthStatus.SHOW_HURT);
        assertEquals(List.of(SQUIRTLE), MOCK_BOX.getPokemons());

        MOCK_BOX.setFilterValue(PokemonFilter.STATUS, HealthStatus.SHOW_FAINTED);
        assertEquals(List.of(PIKACHU), MOCK_BOX.getPokemons());

        MOCK_BOX.setFilterValue(PokemonFilter.STATUS, HealthStatus.SHOW_HEALTHY);
        assertEquals(List.of(BULBASAUR, CHARMANDER, CHARIZARD), MOCK_BOX.getPokemons());
    }

    @Test
    public void testOwnerFilter() {
        Player other = PlayerManager.create("BOX_OTHER");
        Pokemon othersPokemon = PokemonManager.create(PokemonType.EEVEE, null);
        PokemonManager.setPokemonOwner(othersPokemon, other);
        Pokemon wildPokemon = PokemonManager.create(PokemonType.RATTATA, null);

        PokemonBox everyPokemon = PokemonBox.ofEveryPokemon(MOCK_OWNER);
        everyPokemon.setFilterValue(PokemonFilter.OWNER, Ownership.SHOW_MINE);
        assertTrue(everyPokemon.getPokemons().containsAll(MOCK_OWNER.getPokemons()));
        assertFalse(everyPokemon.getPokemons().contains(othersPokemon));
        assertFalse(everyPokemon.getPokemons().contains(wildPokemon));

        everyPokemon.setFilterValue(PokemonFilter.OWNER, Ownership.SHOW_WILD);
        assertTrue(everyPokemon.getPokemons().contains(wildPokemon));
        assertFalse(everyPokemon.getPokemons().contains(othersPokemon));

        everyPokemon.setFilterValue(PokemonFilter.OWNER, Ownership.SHOW_OTHERS);
        assertTrue(everyPokemon.getPokemons().contains(othersPokemon));
        assertFalse(everyPokemon.getPokemons().contains(CHARMANDER));
    }

    @Test
    public void testCombinedFilters() {
        MOCK_BOX.setFilterValue(PokemonFilter.ELEMENT, Element.FIRE);
        MOCK_BOX.setFilterValue(PokemonFilter.LEVEL, LevelRange.LEVEL_1_TO_25);
        assertEquals(List.of(CHARMANDER), MOCK_BOX.getPokemons());

        MOCK_BOX.clearFilters();
        assertEquals(5, MOCK_BOX.getPokemons().size());
    }

    @Test
    public void testCyclingFilterValues() {
        // Status has no "all" (null) value, its first value shows everything.
        assertEquals(PokemonFilter.STATUS, MOCK_BOX.getSelectedFilter());
        assertEquals(HealthStatus.SHOW_ALL, MOCK_BOX.getFilterValue(PokemonFilter.STATUS));
        for (int i = 0; i < HealthStatus.values().length; i++)
            MOCK_BOX.nextFilterValue();
        assertEquals(HealthStatus.SHOW_ALL, MOCK_BOX.getFilterValue(PokemonFilter.STATUS)); // Looped back.

        // Element does, so it goes through every element and back to null.
        MOCK_BOX.nextFilter();
        MOCK_BOX.nextFilter();
        assertEquals(PokemonFilter.ELEMENT, MOCK_BOX.getSelectedFilter());
        assertNull(MOCK_BOX.getFilterValue(PokemonFilter.ELEMENT));

        MOCK_BOX.nextFilterValue();
        assertEquals(Element.values()[0], MOCK_BOX.getFilterValue(PokemonFilter.ELEMENT));
        for (int i = 0; i < Element.values().length; i++)
            MOCK_BOX.nextFilterValue();
        assertNull(MOCK_BOX.getFilterValue(PokemonFilter.ELEMENT));
    }

    @Test
    public void testInvalidFilterValues() {
        // Utils.verify throws an AssertionError when assertions are on (as in tests), and a GeneralException otherwise.
        assertThrows(Throwable.class, () -> MOCK_BOX.setFilterValue(PokemonFilter.ELEMENT, LevelRange.LEVEL_1_TO_25));
        assertThrows(Throwable.class, () -> MOCK_BOX.setFilterValue(PokemonFilter.STATUS, null));
    }

    @Test
    public void testSorters() {
        MOCK_BOX.setSorter(PokemonSorter.LEVEL);
        assertEquals(List.of(PIKACHU, CHARIZARD, SQUIRTLE, BULBASAUR, CHARMANDER), MOCK_BOX.getPokemons());

        MOCK_BOX.setSorter(PokemonSorter.LEVEL_REVERSED);
        assertEquals(List.of(CHARMANDER, BULBASAUR, SQUIRTLE, CHARIZARD, PIKACHU), MOCK_BOX.getPokemons());

        MOCK_BOX.setSorter(PokemonSorter.NAME);
        assertEquals(List.of(BULBASAUR, CHARIZARD, CHARMANDER, PIKACHU, SQUIRTLE), MOCK_BOX.getPokemons());

        SQUIRTLE.decreaseHP(SQUIRTLE.getHP() / 2);
        MOCK_BOX.setSorter(PokemonSorter.HEALTH);
        assertEquals(SQUIRTLE, MOCK_BOX.getPokemons().get(0));
    }

    @Test
    public void testCyclingSorters() {
        MOCK_BOX.nextSorter();
        assertEquals(PokemonSorter.NAME, MOCK_BOX.getSorter());

        MOCK_BOX.previousSorter();
        MOCK_BOX.previousSorter();
        assertEquals(PokemonSorter.TOTAL_STATS, MOCK_BOX.getSorter()); // Looped back to the last.
    }

    @Test
    public void testBoxShowsCurrentPokemon() {
        Pokemon newPokemon = givePokemon(PokemonType.MEW, 1);
        assertTrue(MOCK_BOX.getPokemons().contains(newPokemon)); // Given after the box was made.
    }

    @Test
    public void testForEachShown() {
        CHARMANDER.decreaseHP(3);
        SQUIRTLE.decreaseHP(3);

        MOCK_BOX.setFilterValue(PokemonFilter.STATUS, HealthStatus.SHOW_HURT);
        MOCK_BOX.forEachShown(Pokemon::heal);
        assertTrue(MOCK_BOX.getPokemons().isEmpty());
    }

    private Pokemon givePokemon(PokemonType type, int level) {
        Pokemon pokemon = PokemonManager.create(type, null);
        while (pokemon.getLevel() < level)
            pokemon.addExp(pokemon.getNeededExp() - pokemon.getExp());

        PokemonManager.setPokemonOwner(pokemon, MOCK_OWNER);
        return pokemon;
    }

    @AfterAll
    public static void tearDown() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
