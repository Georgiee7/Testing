package me.george.testing.animals;

/**
 * Represents an animal object.
 * Created by George K. on 4/12/2024.
 */
public abstract class Animal {

    private static final String NO_NAME_PLACEHOLDER = "No name";

    private String name;

    public Animal() {
        this(NO_NAME_PLACEHOLDER);
    }

    public Animal(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the animal.
     * @return animalName
     */
    public String getName() {
        return this.name;
    }

    /**
     * Changes the name of the animal.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    private String getInternalName() {
        return getClass().getSimpleName();
    }

    // optional setters (will revoke final attribute)
}
