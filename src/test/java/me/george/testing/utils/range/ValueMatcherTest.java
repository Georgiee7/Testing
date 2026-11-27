package me.george.testing.utils.range;

import me.george.testing.entity.pokemon.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests parsing, matching and rolling values written as text.
 * Created by George K. on 11/27/2026.
 */
public class ValueMatcherTest {

    @Test
    public void testRanges() {
        ValueMatcher<Integer> single = ValueMatcher.parse("5", Integer.class);
        assertTrue(single.contains(5));
        assertFalse(single.contains(6));

        ValueMatcher<Integer> range = ValueMatcher.parse("1-4", Integer.class);
        assertTrue(range.contains(1));
        assertTrue(range.contains(4));
        assertFalse(range.contains(5));
        for (int i = 0; i < 50; i++) {
            int rolled = range.getRandomValue();
            assertTrue(rolled >= 1 && rolled <= 4);
        }

        ValueMatcher<Element> elements = ValueMatcher.parse("FIRE-WATER", Element.class); // By their order: Fire, Electric, Water.
        assertTrue(elements.contains(Element.ELECTRIC));
        assertFalse(elements.contains(Element.GRASS));
    }

    @Test
    public void testComparisons() {
        ValueMatcher<Integer> comparison = ValueMatcher.parse(">=30", Integer.class);
        assertInstanceOf(ComparisonMatcher.class, comparison);
        assertTrue(comparison.contains(30));
        assertFalse(comparison.contains(29));
        assertThrows(Throwable.class, comparison::getRandomValue); // No top to roll up to.

        ValueMatcher<Element> enumComparison = ValueMatcher.parse(">FAIRY", Element.class);
        assertThrows(Throwable.class, enumComparison::getRandomValue); // Fairy is the last element.
        assertEquals(Element.NORMAL, ValueMatcher.parse("<FIGHTER", Element.class).getRandomValue());
    }

    @Test
    public void testSets() {
        ValueMatcher<Element> set = ValueMatcher.parse("{fire, WATER}", Element.class);
        assertTrue(set.contains(Element.FIRE));
        assertTrue(set.contains(Element.WATER));
        assertFalse(set.contains(Element.ELECTRIC));
        assertTrue(set.contains(set.getRandomValue()));
    }

    @Test
    public void testWritingAndDescribing() {
        assertEquals("5-10", ValueMatcher.parse("5 - 10", Integer.class).toConfigString());
        assertEquals("{FIRE,WATER}", ValueMatcher.parse("{fire,water}", Element.class).toConfigString());
        assertEquals("<=3", ValueMatcher.parse("<=3", Integer.class).toConfigString());

        assertEquals("Fire or Water", ValueMatcher.parse("{FIRE,WATER}", Element.class).describe());
        assertEquals("Fire, Water or Grass", ValueMatcher.parse("{FIRE,WATER,GRASS}", Element.class).describe());
        assertEquals("30+", ValueMatcher.parse(">=30", Integer.class).describe());
    }

    @Test
    public void testInvalidValues() {
        assertThrows(Throwable.class, () -> ValueMatcher.parse("10-5", Integer.class));
        assertThrows(Throwable.class, () -> ValueMatcher.parse("five", Integer.class));
        assertThrows(Throwable.class, () -> ValueMatcher.parse("LAVA", Element.class));
        assertThrows(Throwable.class, () -> ValueMatcher.parse("{}", Element.class));
    }
}
