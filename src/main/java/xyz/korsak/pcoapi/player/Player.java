package xyz.korsak.pcoapi.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    private String id;
    private String name;
    private int chips;
    private int stakedChips;
    private int investedChips;
    private String token;
    private PlayerActions actions;
    private boolean active;

    public Player(String id, String name, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.chips = 1000;
        this.stakedChips = 0;
        this.investedChips = 0;
        this.actions = new PlayerActions();
        this.active = true;
    }

    public void addToStake(int bet) {
        this.stakedChips += bet;
    }
}