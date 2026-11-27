package me.george.testing.entity.pokemon.spec;

import lombok.Getter;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.utils.GeneralException;
import me.george.testing.utils.ReflectionUtil;
import me.george.testing.utils.Utils;
import me.george.testing.utils.range.ComparisonMatcher;
import me.george.testing.utils.range.ValueMatcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pokémon described as text, like "CHARMANDER[level=5-10]" or "3x{PIKACHU,EEVEE}[shiny=true]". The same spec can match Pokémon
 * (searching a box, or a challenge that counts Fire types) and create them (wild encounters or rewards).
 * Written as [amount x]TYPE[argument=value,...], where TYPE is a Pokémon type, a set of them or * for any. A value can be a single
 * value (5), a range (5-10), a comparison (>=30, which can also be written as level>=30) or a set ({FIRE,WATER}).
 * The arguments are the fields marked with {@link SpecArgument}, found once through reflection, so adding an argument is just
 * adding a field: parsing it, writing it back and the usage all come from the field.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class PokemonSpec {
    private static final Pattern SPEC_PATTERN = Pattern.compile("^(?:(\\d+)\\s*x\\s*)?([^\\[\\]]+?)\\s*(?:\\[(.*)])?$");
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("^(\\w+)\\s*(>=|<=|>|<|=)\\s*(.+)$");
    private static final List<Field> ARGUMENTS = ReflectionUtil.getAnnotatedFields(PokemonSpec.class, SpecArgument.class);
    private static final String ANY_TYPE = "*";

    private final int amount; // How many Pokémon it creates.
    private final ValueMatcher<PokemonType> type; // Null for any type.

    @SpecArgument(description = "The level, such as 5, 5-10 or >=30.")
    private ValueMatcher<Integer> level;

    @SpecArgument(description = "An element it has, such as FIRE or {FIRE,WATER}.")
    private ValueMatcher<Element> element;

    @SpecArgument(description = "Whether it's shiny, true or false.")
    private Boolean shiny;

    private PokemonSpec(int amount, ValueMatcher<PokemonType> type) {
        Utils.verify(amount > 0, "A Pokemon spec's amount must be at least 1, not %d.", amount);
        this.amount = amount;
        this.type = type;
    }

    /**
     * Parses a spec.
     *
     * @param input The text to parse, such as "3xCHARMANDER[level=5-10]".
     * @return pokemonSpec
     */
    public static PokemonSpec parse(String input) {
        Utils.verify(!Utils.isNullOrEmpty(input), "Tried to parse an empty Pokemon spec.");
        Matcher matcher = SPEC_PATTERN.matcher(input.trim());
        if (!matcher.matches())
            throw new GeneralException("Invalid Pokemon spec '%s'. Usage: %s", input, getUsage());

        int amount = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 1;
        String type = matcher.group(2).trim();
        PokemonSpec spec = new PokemonSpec(amount, type.equals(ANY_TYPE) ? null : ValueMatcher.parse(type, PokemonType.class));
        if (matcher.group(3) != null)
            for (String argument : splitArguments(matcher.group(3)))
                spec.parseArgument(argument);

        return spec;
    }

    // Splits arguments by their commas, but not the commas inside a set like {FIRE,WATER}.
    private static List<String> splitArguments(String arguments) {
        List<String> split = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (char character : arguments.toCharArray()) {
            if (character == '{')
                depth++;
            else if (character == '}')
                depth--;

            if (character == ',' && depth == 0) {
                split.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        split.add(current.toString());
        return split.stream().map(String::trim).filter(argument -> !argument.isEmpty()).toList();
    }

    private void parseArgument(String argument) {
        Matcher matcher = ARGUMENT_PATTERN.matcher(argument);
        if (!matcher.matches())
            throw new GeneralException("Invalid argument '%s', it should be written as name=value.", argument);

        Field field = getArgumentField(matcher.group(1));
        Utils.verify(getValue(field) == null, "The argument '%s' is set more than once.", field.getName());

        String operator = matcher.group(2);
        String value = operator.equals("=") ? matcher.group(3) : operator + matcher.group(3); // level>=30 is the same as level=>=30.
        setValue(field, parseValue(field, value.trim()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object parseValue(Field field, String value) {
        if (field.getType() == ValueMatcher.class)
            return ValueMatcher.parse(value, (Class) ReflectionUtil.getGenericArgument(field, 0)); // The type of the values, like Integer for ValueMatcher<Integer>.

        if (field.getType() == Boolean.class) {
            Utils.verify(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"), "The argument '%s' must be true or false, not '%s'.", field.getName(), value);
            return Boolean.parseBoolean(value);
        }

        throw new GeneralException("Spec arguments of type %s aren't supported.", Utils.getSimpleName(field.getType()));
    }

    private static Field getArgumentField(String name) {
        for (Field field : ARGUMENTS)
            if (field.getName().equalsIgnoreCase(name))
                return field;

        throw new GeneralException("Unknown argument '%s'. Usage: %s", name, getUsage());
    }

    private Object getValue(Field field) {
        try {
            return field.get(this);
        } catch (IllegalAccessException exception) {
            throw new GeneralException(exception, "Could not read the argument '%s'.", field.getName());
        }
    }

    private void setValue(Field field, Object value) {
        try {
            field.set(this, value);
        } catch (IllegalAccessException exception) {
            throw new GeneralException(exception, "Could not set the argument '%s'.", field.getName());
        }
    }

    /**
     * Whether a Pokémon matches this spec. The amount doesn't matter for matching.
     *
     * @param pokemon The Pokémon in question.
     * @return matches
     */
    public boolean matches(Pokemon pokemon) {
        return pokemon != null
                && (this.type == null || this.type.contains(pokemon.getType()))
                && (this.level == null || this.level.contains(pokemon.getLevel()))
                && (this.element == null || Arrays.stream(pokemon.getElements()).anyMatch(this.element::contains))
                && (this.shiny == null || this.shiny == pokemon.isShiny());
    }

    /**
     * Creates the Pokémon this spec describes, rolling what it leaves open: the type out of the ones that match, and the level within its range.
     * A spec with an open comparison, like level>=30, can only match, as there's no top to roll up to.
     *
     * @return createdPokemon, as many as the amount.
     */
    public List<Pokemon> create() {
        List<PokemonType> types = Arrays.stream(PokemonType.values())
                .filter(type -> this.type == null || this.type.contains(type))
                .filter(type -> this.element == null || Arrays.stream(type.getElements()).anyMatch(this.element::contains))
                .toList();
        Utils.verify(!types.isEmpty(), "No Pokemon types match the spec %s.", this);

        List<Pokemon> pokemons = new ArrayList<>();
        for (int i = 0; i < this.amount; i++) {
            int rolledLevel = this.level != null ? this.level.getRandomValue() : 1;
            Utils.verify(Utils.isValidLevel(rolledLevel), "The spec %s rolled the invalid level %d.", this, rolledLevel);

            Pokemon pokemon = PokemonManager.create(Utils.randElement(types), null);
            while (pokemon.getLevel() < rolledLevel)
                pokemon.addExp(pokemon.getNeededExp() - pokemon.getExp());

            if (this.shiny != null)
                PokemonManager.setShiny(pokemon, this.shiny);

            pokemons.add(pokemon);
        }

        return pokemons;
    }

    /**
     * Describes the Pokémon this spec matches, such as "shiny Fire Pokémon at level 30+".
     *
     * @return description
     */
    public String describe() {
        StringBuilder description = new StringBuilder();
        if (this.amount > 1)
            description.append(this.amount).append(' ');

        if (this.shiny != null)
            description.append(this.shiny ? "shiny " : "non-shiny ");

        if (this.element != null)
            description.append(this.element.describe()).append(' ');

        description.append(this.type != null ? this.type.describe() : "Pokémon");
        if (this.level != null)
            description.append(" at level ").append(this.level.describe());

        return description.toString();
    }

    /**
     * Gets how a spec is written, with every argument it can have.
     *
     * @return usage
     */
    public static String getUsage() {
        StringBuilder usage = new StringBuilder("[amount x]TYPE[argument=value,...], where TYPE is a Pokémon, a set of them like {PIKACHU,EEVEE} or * for any. Arguments:");
        for (Field field : ARGUMENTS)
            usage.append("\n- ").append(field.getName()).append(": ").append(field.getAnnotation(SpecArgument.class).description());

        return usage.toString();
    }

    @Override
    public String toString() {
        List<String> arguments = new ArrayList<>();
        for (Field field : ARGUMENTS) {
            Object value = getValue(field);
            if (value instanceof ComparisonMatcher<?> comparison)
                arguments.add(field.getName() + comparison.toConfigString()); // level>=30, rather than level=>=30.
            else if (value instanceof ValueMatcher<?> matcher)
                arguments.add(field.getName() + "=" + matcher.toConfigString());
            else if (value != null)
                arguments.add(field.getName() + "=" + value);
        }

        return (this.amount > 1 ? this.amount + "x" : "")
                + (this.type != null ? this.type.toConfigString() : ANY_TYPE)
                + (!arguments.isEmpty() ? "[" + String.join(",", arguments) + "]" : "");
    }
}
