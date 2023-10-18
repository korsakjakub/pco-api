package xyz.korsak.pcoapi.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class PokerRules {
    private int startingChips;
    private int ante;
    private int smallBlind;
    private int bigBlind;

    public PokerRules() {
        this.startingChips = 1000;
        this.ante = 5;
        this.smallBlind = 10;
        this.bigBlind = 20;
    }
}
