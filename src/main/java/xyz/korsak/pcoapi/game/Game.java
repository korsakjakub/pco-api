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
    private int lastToPlayIndex;
    private int actionsTakenThisRound;

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
    }

    public Game(GameState gameState, int currentTurnIndex) {
        this.rules = new PokerRules();
        this.state = gameState;
        this.stage = GameStage.PRE_FLOP;
        this.currentTurnIndex = currentTurnIndex;
        this.stakedChips = 0;
        this.currentBetSize = 0;
        this.dealerIndex = 0;
        this.smallBlindIndex = 1;
        this.bigBlindIndex = 2;
        this.actionsTakenThisRound = 0;
    }

    public void nextStage() {
        int currentStageOrdinal = this.stage.ordinal();
        GameStage[] stages = GameStage.values();
        int nextStageOrdinal = (currentStageOrdinal + 1) % stages.length;
        this.stage = stages[nextStageOrdinal];
    }

    public void nextTurnIndex(int numberOfPlayers) {
        this.currentTurnIndex = (currentTurnIndex + 1) % numberOfPlayers;
    }

    public int firstToPlayIndex(int numberOfPlayers) {
        return (this.dealerIndex + 1) % numberOfPlayers;
    }

    public void updateLastToPlay(int numberOfPlayers) {
        this.lastToPlayIndex = (this.currentTurnIndex + (numberOfPlayers - 1)) % numberOfPlayers;
    }

    public void incrementActionsTakenThisRound() {
        this.actionsTakenThisRound++;
    }
}
