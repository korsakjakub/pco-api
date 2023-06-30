package xyz.korsak.pcoapi.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public class Player {
    private String id;
    private String name;

    @JsonIgnore
    private String token;
    private Long balance;
    public Player() {
    }

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, Long balance) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.balance = balance;
    }

    public Player(String id, String name, Long balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public String getId() {
        return id;
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

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}
