package xyz.korsak.pcoapi.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.korsak.pcoapi.player.Player;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Queue {
    private String roomId;
    private String id;
    private List<Player> players;

    public Queue(String roomId, String queueId) {
        this.roomId = roomId;
        this.id = queueId;
    }

    public Player removePlayer(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                players.remove(player);
                return player;
            }
        }
        return null;
    }
}
