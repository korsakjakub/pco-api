package xyz.korsak.pcoapi.rules;

import xyz.korsak.pcoapi.game.GameMode;

public record PokerRules(int startingChips, int ante, int smallBlind, int bigBlind, GameMode gameMode) {
    public PokerRules() {
        this(1000, 5, 10, 20, GameMode.CASH);
    }
}
