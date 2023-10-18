package xyz.korsak.pcoapi.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.korsak.pcoapi.player.Player;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class GetPlayersResponse {
    private List<PlayerResponseWithoutToken> players;

    public GetPlayersResponse(List<Player> pwt) {
        this.players = new ArrayList<>();
        for (Player p : pwt) {
            this.players.add(new PlayerResponseWithoutToken(p.getId(), p.getName(), p.getChips(), p.getStakedChips()));
        }
    }
}
