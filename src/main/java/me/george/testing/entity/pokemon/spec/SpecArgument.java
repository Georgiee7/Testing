package me.george.testing.entity.pokemon.spec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field of {@link PokemonSpec} as an argument that can be written in a spec, named after the field.
 * The field's type decides how its value is parsed, and the description goes in the usage.
 * Created by George K. on 11/27/2026.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecArgument {

    /**
     * What the argument is, with an example of its value.
     *
     * @return description
     */
    String description();
}
