package xyz.korsak.pcoapi.game;

import lombok.*;
import xyz.korsak.pcoapi.rules.PokerRules;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Game {
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

    public Game() {
        this.rules = new PokerRules();
        this.state = GameState.WAITING;
        this.stage = GameStage.PRE_FLOP;
    }

    public Game(GameState gameState, int currentTurnIndex, int numberOfPlayers) {
        this.rules = new PokerRules();
        this.state = gameState;
        this.stage = GameStage.PRE_FLOP;
        this.currentTurnIndex = currentTurnIndex;
        this.numberOfPlayers = numberOfPlayers;
        this.stakedChips = 0;
        this.currentBetSize = 0;
        this.dealerIndex = 0;
        this.smallBlindIndex = 1;
        this.bigBlindIndex = this.numberOfPlayers != 0 ? 2 % this.numberOfPlayers : 0;
        this.actionsTakenThisRound = 0;
    }

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

        public GameBuilder dealerIndex(int index) {
            if(this.numberOfPlayers == 0)
                return this;
            return this.dealerIndex(index % this.numberOfPlayers)
                    .smallBlindIndex((index + 1) % this.numberOfPlayers)
                    .bigBlindIndex((index + 2) % this.numberOfPlayers);
        }
        public GameBuilder nextTurnIndex() {
            return this.currentTurnIndex((this.currentTurnIndex + 1) % this.numberOfPlayers);
        }

        public GameBuilder incrementActionsTakenThisRound() {
            return this.actionsTakenThisRound(this.actionsTakenThisRound++);
        }

        public GameBuilder decrementActionsTakenThisRound() {
            return this.actionsTakenThisRound(this.actionsTakenThisRound--);
        }

        public GameBuilder addToStake(int bet) {
            return this.stakedChips(this.stakedChips + bet);
        }
    }
}
