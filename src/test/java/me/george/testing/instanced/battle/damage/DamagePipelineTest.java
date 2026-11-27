package me.george.testing.instanced.battle.damage;

import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.manager.CombatManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the damage pipeline and its modifiers.
 * Created by George K. on 11/27/2026.
 */
public class DamagePipelineTest {
    private final Pokemon CHARMANDER = createPokemon(PokemonType.CHARMANDER, 20);
    private final Pokemon BULBASAUR = createPokemon(PokemonType.BULBASAUR, 20);
    private final Pokemon SQUIRTLE = createPokemon(PokemonType.SQUIRTLE, 20);
    private final Pokemon SANDSHREW = createPokemon(PokemonType.SANDSHREW, 20);

    @Test
    public void testBaseDamageFormula() {
        assertEquals(19, DamagePipeline.calculateBaseDamage(50, 40, 100, 100));
        assertEquals(2, DamagePipeline.calculateBaseDamage(1, 1, 1, 999)); // Always at least 2 before modifiers.
    }

    @Test
    public void testDefaultModifiersAndOrder() {
        assertEquals(List.of(DefaultDamageModifier.values()), DamagePipeline.createDefault().getModifiers());
    }

    @Test
    public void testSameTypeAttackBonus() {
        DamagePipeline pipeline = new DamagePipeline().addModifier(DefaultDamageModifier.SAME_TYPE_ATTACK_BONUS);
        assertEquals(CombatManager.SAME_TYPE_ATTACK_BONUS_MULTIPLIER, calculate(pipeline, CHARMANDER, SQUIRTLE, MoveType.EMBER).getMultiplier(DefaultDamageModifier.SAME_TYPE_ATTACK_BONUS));
        assertEquals(CombatManager.NORMAL_DAMAGE_MULTIPLIER, calculate(pipeline, CHARMANDER, SQUIRTLE, MoveType.WATER_GUN).getMultiplier(DefaultDamageModifier.SAME_TYPE_ATTACK_BONUS));
    }

    @Test
    public void testTypeEffectiveness() {
        DamagePipeline pipeline = new DamagePipeline().addModifier(DefaultDamageModifier.TYPE_EFFECTIVENESS);
        assertEquals(CombatManager.EFFECTIVE_DAMAGE_MULTIPLIER, calculate(pipeline, CHARMANDER, BULBASAUR, MoveType.EMBER).getTotalMultiplier()); // Fire vs Grass/Poison.
        assertEquals(CombatManager.NOT_EFFECTIVE_DAMAGE_MULTIPLIER, calculate(pipeline, CHARMANDER, SQUIRTLE, MoveType.EMBER).getTotalMultiplier());

        DamageCalculation immune = calculate(pipeline, SQUIRTLE, SANDSHREW, MoveType.THUNDER_SHOCK); // Electric vs Ground.
        assertTrue(immune.isImmune());
        assertEquals(0, immune.getDamage());
    }

    @Test
    public void testDamageUsesTheMovesCategory() {
        DamagePipeline pipeline = new DamagePipeline();
        DamageCalculation physical = calculate(pipeline, CHARMANDER, SQUIRTLE, MoveType.TACKLE);
        assertEquals(DamagePipeline.calculateBaseDamage(20, 40, CHARMANDER.getAttack(), SQUIRTLE.getDefense()), physical.getBaseDamage());

        DamageCalculation special = calculate(pipeline, CHARMANDER, SQUIRTLE, MoveType.EMBER);
        assertEquals(DamagePipeline.calculateBaseDamage(20, 40, CHARMANDER.getSpAttack(), SQUIRTLE.getSpDefense()), special.getBaseDamage());
    }

    @Test
    public void testMinimumDamage() {
        DamagePipeline pipeline = new DamagePipeline().addModifier(DamageModifier.of("Tiny", calculation -> 0.001F));
        assertEquals(1, calculate(pipeline, CHARMANDER, SQUIRTLE, MoveType.TACKLE).getDamage()); // A hit always deals at least 1.
    }

    @Test
    public void testRandomSpread() {
        DamagePipeline pipeline = new DamagePipeline().addModifier(DefaultDamageModifier.RANDOM);
        for (int i = 0; i < 100; i++) {
            DamageCalculation calculation = calculate(pipeline, CHARMANDER, SQUIRTLE, MoveType.TACKLE);
            assertTrue(calculation.getDamage() >= (int) (calculation.getBaseDamage() * CombatManager.MIN_RANDOM_DAMAGE_MULTIPLIER));
            assertTrue(calculation.getDamage() <= calculation.getBaseDamage());
        }
    }

    @Test
    public void testCustomModifier() {
        DamageModifier rain = DamageModifier.of("Rain", calculation -> calculation.getMove().getElement() == Element.WATER ? 1.5F : 1F);
        DamagePipeline pipeline = new DamagePipeline().addModifier(rain);

        DamageCalculation waterGun = calculate(pipeline, SQUIRTLE, CHARMANDER, MoveType.WATER_GUN);
        assertEquals(1.5F, waterGun.getMultiplier(rain));
        assertEquals((int) (waterGun.getBaseDamage() * 1.5F), waterGun.getDamage());
        assertEquals(1F, calculate(pipeline, SQUIRTLE, CHARMANDER, MoveType.TACKLE).getMultiplier(rain));

        pipeline.removeModifier(rain);
        assertFalse(pipeline.getModifiers().contains(rain));
    }

    private static DamageCalculation calculate(DamagePipeline pipeline, Pokemon attacker, Pokemon defender, MoveType move) {
        return pipeline.calculate(attacker, defender, move.create(attacker, defender));
    }

    private static Pokemon createPokemon(PokemonType type, int level) {
        Pokemon pokemon = PokemonManager.create(type, null);
        while (pokemon.getLevel() < level)
            pokemon.addExp(pokemon.getNeededExp() - pokemon.getExp());

        return pokemon;
    }

    @AfterAll
    public static void tearDown() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
