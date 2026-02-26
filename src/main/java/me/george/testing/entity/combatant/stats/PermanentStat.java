package me.george.testing.entity.combatant.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A registry of permanent stats a combatant may have.
 * Created by George K. on 4/13/2024.
 */
@AllArgsConstructor
@Getter
public enum PermanentStat {
    HP("Health"),
    ATK("Attack"),
    DEF("Defense"),
    SP_ATK("Special Attack"),
    SP_DEF("Special Defense"),
    SPEED("Speed");

    @JsonIgnore
    private final String displayName;
}
