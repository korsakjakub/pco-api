package xyz.korsak.pcoapi.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import xyz.korsak.pcoapi.game.Game;
import xyz.korsak.pcoapi.game.GameStage;
import xyz.korsak.pcoapi.game.GameState;
import xyz.korsak.pcoapi.player.Player;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class GetGameResponse {
    private GameState state;
    private GameStage stage;
    private int stakedChips;
    private int currentBetSize;
    private String currentTurnPlayerId;
    private String dealerPlayerId;
    private String smallBlindPlayerId;
    private String bigBlindPlayerId;

    public GetGameResponse(Game g, List<Player> p) {
        this.state = g.state();
        this.stage = g.stage();
        this.stakedChips = g.stakedChips();
        this.currentBetSize = g.currentBetSize();
        this.currentTurnPlayerId = p.get(g.currentTurnIndex() % p.size()).getId();
        this.dealerPlayerId = p.get(g.dealerIndex() % p.size()).getId();
        this.smallBlindPlayerId = p.get(g.smallBlindIndex() % p.size()).getId();
        this.bigBlindPlayerId = p.get(g.bigBlindIndex() % p.size()).getId();
    }
}
