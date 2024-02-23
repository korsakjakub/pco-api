package xyz.korsak.pcoapi.game;

import lombok.Builder;
import xyz.korsak.pcoapi.rules.PokerRules;

@Builder(toBuilder = true)
public record Game(PokerRules rules, GameState state, GameStage stage, int stakedChips, int currentBetSize,
                   int currentTurnIndex, int dealerIndex, int smallBlindIndex, int bigBlindIndex,
                   int actionsTakenThisRound, int numberOfPlayers, int numberOfHandsCompleted) {
    public static class GameBuilder {
        private PokerRules rules;
        private GameState state;
        private GameStage stage;
        private int stakedChips;

        private int currentBetSize;
        private int currentTurnIndex;
        private int dealerIndex;
        private int smallBlindIndex;
        private int bigBlindIndex;
        private int actionsTakenThisRound;
        private int numberOfPlayers;
        private int numberOfHandsCompleted;
        private int numberOfActivePlayersInHand;

        public GameBuilder() {
            this.rules = new PokerRules();
            this.state = GameState.WAITING;
            this.stage = GameStage.PRE_FLOP;
            this.numberOfHandsCompleted = 0;
        }

        public GameBuilder dealerBlindsAndCurrentIndices(int index) {
            if (this.numberOfPlayers == 0)
                return this;
            return this.dealerIndex(index % this.numberOfPlayers)
                    .smallBlindIndex((index + 1) % this.numberOfPlayers)
                    .bigBlindIndex((index + 2) % this.numberOfPlayers)
                    .currentTurnIndex((index + 1) % this.numberOfPlayers);
        }

        public GameBuilder incActionsTakenThisRound() {
            return this.actionsTakenThisRound(++this.actionsTakenThisRound);
        }

        public GameBuilder decActionsTakenThisRound() {
            return this.actionsTakenThisRound(--this.actionsTakenThisRound);
        }

        public GameBuilder incDealerIndex() {
            return this.dealerBlindsAndCurrentIndices(++this.dealerIndex);
        }

        public GameBuilder incHandsCompleted() {
            return this.numberOfHandsCompleted(++this.numberOfHandsCompleted);
        }

        public GameBuilder addToStake(int bet) {
            return this.stakedChips(this.stakedChips + bet);
        }
    }
}
