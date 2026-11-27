package me.george.testing.entity.pokemon.move.moves;

import me.george.testing.Testing;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.entity.pokemon.move.base.DamagingMove;
import me.george.testing.entity.pokemon.move.base.TargetMove;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A damaging move with no mechanics of its own, made for the moves that don't have their own class (yet).
 * Created by George K. on 11/27/2026.
 */
public class SimpleDamagingMove extends TargetMove implements DamagingMove {

    public SimpleDamagingMove(MoveType type, Combatant caster, Combatant... targets) {
        super(type, caster, targets);
    }

    @Override
    protected boolean execute() {
        Testing.logInfo("%s used %s on %s!", getCaster().getName(), getName(), Arrays.stream(getTargets())
                .map(Combatant::getName)
                .collect(Collectors.joining(", ")));
        return true;
    }
}
