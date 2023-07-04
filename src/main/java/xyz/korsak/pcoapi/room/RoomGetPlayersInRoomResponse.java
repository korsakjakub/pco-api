package xyz.korsak.pcoapi.room;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerResponseWithoutToken;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class RoomGetPlayersInRoomResponse {
    private List<PlayerResponseWithoutToken> players;

    public RoomGetPlayersInRoomResponse(List<Player> pwt) {
        this.players = new ArrayList<>();
        for (Player p : pwt) {
            this.players.add(new PlayerResponseWithoutToken(p.getId(), p.getName(), p.getBalance()));
        }
    }
}
