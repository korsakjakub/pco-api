package xyz.korsak.pcoapi.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.korsak.pcoapi.player.Player;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class RoomGetPlayersInRoomResponse {
    private List<Player> players;

    public RoomGetPlayersInRoomResponse(List<Player> pwt) {
        this.players = new ArrayList<>(pwt);
        for (Player p : this.players) {
            p.setToken(null);
        }
    }
}
