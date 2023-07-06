package xyz.korsak.pcoapi.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.korsak.pcoapi.rules.Rules;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    private Rules rules;
    private GameState state;
    private Long StakedChips;
}
