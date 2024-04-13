package me.george.testing.animals;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an animal object.
 * Created by George K. on 4/12/2024.
 */
@Setter
@Getter
public abstract class Animal {

    private static final String NO_NAME_PLACEHOLDER = "No name";

    private String name;

    public Animal() {
        this(NO_NAME_PLACEHOLDER);
    }

    public Animal(String name) {
        this.name = name;
    }

    private String getInternalName() {
        return getClass().getSimpleName();
    }

    // optional setters (will revoke final attribute)
}
