package xyz.korsak.pcoapi.room;

import lombok.*;
import xyz.korsak.pcoapi.game.Game;
import xyz.korsak.pcoapi.player.Player;

import java.util.List;

@Builder(toBuilder = true)
public record Room(String id, List<Player> players, String token, Game game, String queueId) {
    public static class RoomBuilder {
        private String id;
        private List<Player> players;
        private String token;
        private Game game;
        private String queueId;

        public RoomBuilder() {
        }

        public RoomBuilder(String id, List<Player> players, String token) {
            this.id = id;
            this.players = players;
            this.token = token;
            this.game = new Game.GameBuilder().build();
        }

        public RoomBuilder(List<Player> players) {
            this.players = players;
        }
    }
}
