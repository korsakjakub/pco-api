package xyz.korsak.pcoapi.game;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;

import java.util.List;

@Slf4j
class GameServiceTest {

    @Test
    public void testDistributeChips() {
        // Given
        int pot = 100;
        List<Player> players = List.of(
            new PlayerBuilder("p1", "p1", 1000).build(),
            new PlayerBuilder("p2", "p2", 1000).build(),
            new PlayerBuilder("p3", "p3", 1000).build()
        );
        players.get(0).setInvestedChips(500);
        players.get(1).setInvestedChips(500);
        players.get(2).setInvestedChips(20);
        List<Player> winners = List.of( new PlayerBuilder("p1", "p1", 1000).build() );
        // When
        var winnings = GameService.distributeWinnings(players, winners);
        // Then
        Assertions.assertEquals(520, winnings.get("p1"));
        Assertions.assertEquals(-500, winnings.get("p2"));
        Assertions.assertEquals(-20, winnings.get("p3"));
    }
}