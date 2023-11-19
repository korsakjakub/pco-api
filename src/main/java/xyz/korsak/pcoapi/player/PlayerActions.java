package xyz.korsak.pcoapi.player;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PlayerActions extends ArrayList<String> {
    public PlayerActions(String... actions) {
        this.addAll(Arrays.asList(actions));
    }

    public PlayerActions getInitialActions() {
        return new PlayerActions("Fold", "Check", "Bet");
    }

//    public PlayerActions(int gameStake, int playerStake) {
//        if (gameStake == playerStake) {
//            return getInitialActions();
//        }
//        if (gameStake > playerStake) {
//            return new PlayerActions("Fold", "Call", "Raise");
//        }
//        return null;
//    }
}

