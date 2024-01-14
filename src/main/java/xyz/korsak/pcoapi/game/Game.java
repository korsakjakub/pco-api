package xyz.korsak.pcoapi.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import xyz.korsak.pcoapi.rules.PokerRules;

@Getter
@Setter
@AllArgsConstructor
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

    public void addToStake(int bet) {
        stakedChips += bet;
    }

    public Game() {
        this.rules = new PokerRules();
        this.state = GameState.WAITING;
        this.stage = GameStage.PRE_FLOP;
        this.stakedChips = 0;
        this.currentBetSize = 0;
        this.dealerIndex = 0;
        this.currentTurnIndex = 1;
        this.smallBlindIndex = 1;
        this.bigBlindIndex = 2;
        this.actionsTakenThisRound = 0;
        this.numberOfPlayers = 2;
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
        this.bigBlindIndex = 2 % this.numberOfPlayers;
        this.actionsTakenThisRound = 0;
    }

    public void setDealerIndex(int newIndex) {
        this.dealerIndex = newIndex % this.numberOfPlayers;
        this.smallBlindIndex = (newIndex + 1) % this.numberOfPlayers;
        this.bigBlindIndex = (newIndex + 2) % this.numberOfPlayers;
    }

    public void incrementDealerIndex() {
        this.setDealerIndex(dealerIndex + 1);
    }

    public void nextStage() {
        int currentStageOrdinal = this.stage.ordinal();
        GameStage[] stages = GameStage.values();
        int nextStageOrdinal = (currentStageOrdinal + 1) % stages.length;
        this.stage = stages[nextStageOrdinal];
    }

    public void nextTurnIndex() {
        this.currentTurnIndex = (this.currentTurnIndex + 1) % this.numberOfPlayers;
    }

    public void incrementActionsTakenThisRound() {
        this.actionsTakenThisRound++;
    }

    public void decrementActionsTakenThisRound() {
        this.actionsTakenThisRound--;
    }
}
