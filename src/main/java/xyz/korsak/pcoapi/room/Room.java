package xyz.korsak.pcoapi.room;

import xyz.korsak.pcoapi.player.Player;

import java.util.List;
import java.util.UUID;

public class Room {
    private String id;


    private String name;
    private String token;
    private List<Player> players;

    public Room() {
    }

    public Room(String id, String name, String token, List<Player> players) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.players = players;
    }
    public Room(List<Player> players) {
        this.id = UUID.randomUUID().toString();
        this.players = players;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String getId() {
        return id;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", players=" + players +
                '}';
    }
}
