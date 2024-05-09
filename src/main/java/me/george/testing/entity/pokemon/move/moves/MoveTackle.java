package me.george.testing.entity.pokemon.move.moves;

import me.george.testing.Testing;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.pokemon.move.DamagingMove;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.entity.pokemon.move.TargetMove;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The basic tackle move of Pokémon.
 * Created by George K. on 5/9/2024.
 */
public class MoveTackle extends TargetMove implements DamagingMove {

    public MoveTackle(Combatant caster, Combatant[] targets) {
        super(MoveType.TACKLE, caster, targets);
    }

    @Override
    protected boolean execute() {
        Testing.logInfo("%s used Tackle on %s!", getCaster().getName(), Arrays.stream(getTargets())
                .map(Combatant::getName)
                .collect(Collectors.joining(", ")));
        return true;
    }
}
