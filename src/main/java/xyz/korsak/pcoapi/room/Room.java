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
public class Room {

    private String id;
    private String name;
    private List<Player> players;
    private String token;
    private Game game;
    private String queueId;

    public Room(List<Player> players) {
        this.players = players;
    }

    public Room(String id, String name, List<Player> players, String token) {
        this.id = id;
        this.name = name;
        this.players = players;
        this.token = token;
    }
}
