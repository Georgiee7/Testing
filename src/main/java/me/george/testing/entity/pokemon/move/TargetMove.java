package me.george.testing.entity.pokemon.move;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.utils.Utils;

/**
 * Represents a move that may target one or more combatant(s).
 * Created by George K. on 5/9/2024.
 */
@Getter
@Setter
public abstract class TargetMove extends Move {

    private Combatant[] targets;

    protected TargetMove(MoveType type, Combatant caster, Combatant... targets) {
        super(type, caster);
        this.targets = targets;
    }

    @Override
    protected boolean execute() {
        return this.targets.length != 0 || shouldExecuteWhenTargetless();
    }

    /**
     * Whether this move should execute if there are no targets.
     *
     * @return shouldExecuteWhenTarget
     */
    protected boolean shouldExecuteWhenTargetless() {
        return false;
    }

    /**
     * Gets a random target out of all combatants.
     *
     * @return randomTarget
     */
    public Combatant getRandomTarget() {
        return Utils.randElement(this.targets);
    }

    /**
     * Gets an array of random targets out of all combatants.
     *
     * @param amount the number of random targets to return
     * @return array of random targets
     */
    public Combatant[] getRandomTargets(int amount) {
        // Make sure the requested amount is not greater than the number of targets
        if (amount > this.targets.length) {
            throw new IllegalArgumentException("Requested amount exceeds the number of targets");
        }

        shuffleTargets();
        Combatant[] randomTargets = new Combatant[amount];

        // Copy the first 'amount' elements from the shuffled array to the randomTargets array.
        System.arraycopy(this.targets, 0, randomTargets, 0, amount);
        return randomTargets;
    }

    /**
     * Shuffles the targets array.
     */
    private void shuffleTargets() {
        for (int i = this.targets.length - 1; i > 0; i--) {
            int index = Utils.nextInt(i + 1);

            // Swap elements
            Combatant temp = this.targets[index];
            this.targets[index] = this.targets[i];
            this.targets[i] = temp;
        }
    }
}
