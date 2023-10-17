package xyz.korsak.pcoapi.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.korsak.pcoapi.game.Game;
import xyz.korsak.pcoapi.game.GameStage;
import xyz.korsak.pcoapi.game.GameState;
import xyz.korsak.pcoapi.player.Player;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class GetGameResponse {
    private GameState state;
    private GameStage stage;
    private Long stakedChips;
    private Long currentBetSize;
    private String currentTurnPlayerId;
    private String dealerPlayerId;
    private String smallBlindPlayerId;
    private String bigBlindPlayerId;

    public GetGameResponse(Game g, List<Player> p) {
        this.state = g.getState();
        this.stage = g.getStage();
        this.stakedChips = g.getStakedChips();
        this.currentBetSize = g.getCurrentBetSize();
        this.currentTurnPlayerId = p.get(g.getCurrentTurnIndex()).getId();
        this.dealerPlayerId = p.get(g.getDealerIndex()).getId();
        if (p.size() >= g.getSmallBlindIndex()) {
            this.smallBlindPlayerId = p.get(g.getSmallBlindIndex()).getId();
        }
        if (p.size() >= g.getBigBlindIndex()) {
            this.bigBlindPlayerId = p.get(g.getBigBlindIndex()).getId();
        }
    }
}
