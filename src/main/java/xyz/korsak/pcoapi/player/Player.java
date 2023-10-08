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
    private Long chips;
    private Long stakedChips;
    private String token;

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, Long chips) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.chips = chips;
        this.stakedChips = 0L;
    }

    public Player(String id, String name, Long chips) {
        this.id = id;
        this.name = name;
        this.chips = chips;
        this.stakedChips = 0L;
    }

    public Player(String id, String name, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.chips = 0L;
        this.stakedChips = 0L;
    }

    public void addToStake(Long bet) {
        this.stakedChips += bet;
    }
}
