package xyz.korsak.pcoapi.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    private String id;
    private String name;
    @Builder.Default
    private int chips = 1000;
    @Builder.Default
    private int stakedChips = 0;
    @Builder.Default
    private int investedChips = 0;
    private String token;
    @Builder.Default
    private PlayerActions actions = new PlayerActions();
    @Builder.Default
    private PlayerState state = PlayerState.Active;
    private PlayerAvatar avatar;

    public Player(String id, String name, String token, PlayerAvatar avatar) {
        this.id = id;
        this.name = name;
        this.token = token;
        this.chips = 1000;
        this.stakedChips = 0;
        this.investedChips = 0;
        this.actions = new PlayerActions();
        this.state = PlayerState.Active;
        this.avatar = avatar;
    }

    public void addToStake(int bet) {
        this.stakedChips += bet;
    }
}