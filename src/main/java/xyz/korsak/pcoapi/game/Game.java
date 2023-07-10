package xyz.korsak.pcoapi.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.korsak.pcoapi.rules.PokerRules;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    private PokerRules rules;
    private GameState state;
    private GameStage stage;
    private Long stakedChips;
    private Long currentBetSize;
    private int currentTurnIndex;
    private int dealerIndex;
    private int smallBlindIndex;
    private int bigBlindIndex;

    public void addToStake(Long bet) {
        stakedChips += bet;
    }

    public Game(GameState gameState, int currentTurnIndex) {
        this.state = gameState;
        this.stage = GameStage.PRE_FLOP;
        this.currentTurnIndex = currentTurnIndex;
        this.stakedChips = 0L;
        this.currentBetSize = 0L;
        this.dealerIndex = 0;
        this.smallBlindIndex = 1;
        this.bigBlindIndex = 2;
    }
}
