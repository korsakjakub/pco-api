package xyz.korsak.pcoapi.player;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class PlayerDTO {

    private String id;
    private String name;
    private int chips;
    private int stakedChips;
    private int investedChips;
    private String token;
    private List<String> actions;
    private PlayerState state;

    public PlayerDTO(Player player) {
        this.id = player.getId();
        this.name = player.getName();
        this.token = player.getToken();
        this.chips = player.getChips();
        this.stakedChips = player.getStakedChips();
        this.investedChips = player.getInvestedChips();
        this.actions = player.getActions().getActions();
        this.state = player.getState();
    }
}
