package xyz.korsak.pcoapi.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.*;

@ToString
@Getter
@Setter
@JsonDeserialize(builder = PlayerBuilder.class)
public class Player {

    private String id;
    private String name;
    private int chips;
    private int stakedChips;
    private String token;
    private PlayerActions actions;

    public Player(PlayerBuilder pb) {
        this.id = pb.getId();
        this.name = pb.getName();
        this.chips = pb.getChips();
        this.stakedChips = pb.getStakedChips();
        this.token = pb.getToken();
        this.actions = pb.getActions();
    }

    public void addToStake(int bet) {
        this.stakedChips += bet;
    }
}
