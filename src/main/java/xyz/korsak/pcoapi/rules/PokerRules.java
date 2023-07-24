package xyz.korsak.pcoapi.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PokerRules {
    private int startingChips;
    private int ante;
    private int smallBlind;
    private int bigBlind;
}
