package xyz.korsak.pcoapi.player;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Player {

    private String id;
    private String name;
    private Long balance;
    private String token;

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

    public Player(String id, String name, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
    }
}
