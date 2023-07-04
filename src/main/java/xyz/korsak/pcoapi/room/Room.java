package xyz.korsak.pcoapi.room;

import lombok.*;
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

    public Room(List<Player> players) {
        this.players = players;
    }
}
