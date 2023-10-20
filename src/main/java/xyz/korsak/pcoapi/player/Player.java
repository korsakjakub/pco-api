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
    private int chips;
    private int stakedChips;
    private String token;

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, int chips) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.chips = chips;
        this.stakedChips = 0;
    }

    public Player(String id, String name, int chips) {
        this.id = id;
        this.name = name;
        this.chips = chips;
        this.stakedChips = 0;
    }

    public Player(String id, String name, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.chips = 0;
        this.stakedChips = 0;
    }

    public void addToStake(int bet) {
        this.stakedChips += bet;
    }
}
