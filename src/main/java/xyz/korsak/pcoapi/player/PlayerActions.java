package xyz.korsak.pcoapi.player;

import java.util.ArrayList;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PlayerActions extends ArrayList<String> {
    public PlayerActions(String... actions) {
        for (String action : actions) {
            add(action);
        }
    }

    public PlayerActions getInitialActions() {
        return new PlayerActions("Fold", "Check", "Bet");
    }
}

