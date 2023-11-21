package xyz.korsak.pcoapi.player;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlayerBuilder {

    private String id;
    private String name;
    private int chips;
    private int stakedChips;
    private String token;
    private List<String> actions;

    public PlayerBuilder(String name) {
        this.name = name;
    }

    public PlayerBuilder(String name, int chips) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.chips = chips;
        this.stakedChips = 0;
        this.actions = PlayerActions.createDefaultActions();
    }

    public PlayerBuilder(String id, String name, int chips) {
        this.id = id;
        this.name = name;
        this.chips = chips;
        this.stakedChips = 0;
        this.actions = PlayerActions.createDefaultActions();
    }

    public PlayerBuilder(String id, String name, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.chips = 0;
        this.stakedChips = 0;
        this.actions = PlayerActions.createDefaultActions();
    }

    public PlayerBuilder(String id, String name, int chips, int stakedChips, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.chips = chips;
        this.stakedChips = stakedChips;
        this.actions = PlayerActions.createDefaultActions();
    }

    public PlayerBuilder token(String token) {
        this.token = token;
        return this;
    }

    public Player build() {
        return new Player(this);
    }
}
