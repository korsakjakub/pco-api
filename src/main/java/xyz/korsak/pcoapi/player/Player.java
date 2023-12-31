package xyz.korsak.pcoapi.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.*;

import java.util.List;

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
    private List<String> actions;
    private boolean active;

    public Player(PlayerBuilder pb) {
        this.id = pb.getId();
        this.name = pb.getName();
        this.chips = pb.getChips();
        this.stakedChips = pb.getStakedChips();
        this.token = pb.getToken();
        this.actions = pb.getActions();
        this.active = pb.isActive();
    }

    public void addToStake(int bet) {
        this.stakedChips += bet;
    }
}
