package me.george.testing.challenge.challenges;

import me.george.testing.challenge.ActionType;
import me.george.testing.challenge.ChallengeAction;
import me.george.testing.challenge.CountChallenge;

/**
 * A challenge to win a number of battles.
 * Created by George K. on 11/27/2026.
 */
public class WinBattlesChallenge extends CountChallenge<WinBattlesChallenge> {

    public WinBattlesChallenge(int goal) {
        super(ActionType.WIN_BATTLE, goal);
    }

    @Override
    protected boolean matches(ChallengeAction action) {
        return true;
    }

    @Override
    protected String makeDescription() {
        return getGoal() == 1 ? "Win a battle." : "Win " + getGoal() + " battles.";
    }
}
