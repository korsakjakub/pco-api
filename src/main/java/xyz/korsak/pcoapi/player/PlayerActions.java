package xyz.korsak.pcoapi.player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerActions {

    private final List<String> actions;

    public PlayerActions() {
        this.actions = createDefaultActions();

    }

    public PlayerActions(List<String> actions) {
        this.actions = actions;
    }

    public static List<String> createDefaultActions() {
        return Arrays.asList("Fold", "Check", "Bet");
    }

    public static PlayerActions createActionsBasedOnBet(int betSize, int playerStake) {
        if (betSize == 0 && betSize == playerStake) {
            return new PlayerActions(Arrays.asList("Fold", "Check", "Bet"));
        } else if (betSize == playerStake) {
            return new PlayerActions(Arrays.asList("Fold", "Check", "Raise"));
        } else if (betSize > playerStake) {
            return new PlayerActions(Arrays.asList("Fold", "Call", "Raise"));
        } else {
            return new PlayerActions();
        }
    }

    public List<String> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public String toString() {
        return actions.toString();
    }
}
