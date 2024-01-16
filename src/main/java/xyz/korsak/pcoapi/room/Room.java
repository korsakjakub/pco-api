package xyz.korsak.pcoapi.room;

import lombok.*;
import xyz.korsak.pcoapi.game.Game;
import xyz.korsak.pcoapi.player.Player;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@Builder(toBuilder = true)
public class Room {

    private String id;
    private List<Player> players;
    private String token;
    private Game game;
    private String queueId;

    public Room(List<Player> players) {
        this.players = players;
    }

    public Room(String id, List<Player> players, String token) {
        this.id = id;
        this.players = players;
        this.token = token;
        this.game = new Game();
    }
}
