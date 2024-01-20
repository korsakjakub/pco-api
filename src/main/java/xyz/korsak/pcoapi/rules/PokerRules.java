package xyz.korsak.pcoapi.rules;

public record PokerRules(int startingChips, int ante, int smallBlind, int bigBlind) {
    public PokerRules() {
        this(1000, 5, 10, 20);
    }
}
