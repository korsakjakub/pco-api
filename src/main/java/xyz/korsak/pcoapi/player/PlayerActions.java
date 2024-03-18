package xyz.korsak.pcoapi.player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerActions {

    private final List<String> actions;

    private PlayerActions() {
        this.actions = createDefaultActions();

    }

    private PlayerActions(List<String> actions) {
        this.actions = actions;
    }

    public static List<String> createDefaultActions() {
        return Arrays.asList("Fold", "Check", "Bet");
    }

    public static List<String> createActionsBasedOnBet(int betSize, int playerStake) {
        if (betSize == 0 && betSize == playerStake) {
            return Arrays.asList("Fold", "Check", "Bet");
        } else if (betSize == playerStake) {
            return Arrays.asList("Fold", "Check", "Raise");
        } else if (betSize > playerStake) {
            return Arrays.asList("Fold", "Call", "Raise");
        } else {
            return Collections.emptyList();
        }
    }

    public List<String> getActions() {
        return Collections.unmodifiableList(actions);
    }
}
