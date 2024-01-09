package xyz.korsak.pcoapi.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    @Mock
    private Authorization auth;

    @Mock
    private RoomRepository roomRepository;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gameService = new GameService(auth, roomRepository);
    }

    @Test
    void getCurrentPlayer_ValidTurnIndex_ReturnsPlayer() {
        // Arrange
        Room room = new Room();
        List<Player> players = new ArrayList<>();
        players.add(new PlayerBuilder("player1", 100).build());
        players.add(new PlayerBuilder("player2", 200).build());
        room.setPlayers(players);
        room.setGame(new Game());
        room.getGame().setCurrentBetSize(50);

        // Act
        Player currentPlayer = gameService.getCurrentPlayer(room, 0);

        // Assert
        assertNotNull(currentPlayer);
        assertEquals("player1", currentPlayer.getName());
    }

    @Test
    void getCurrentPlayer_InvalidTurnIndex_ThrowsException() {
        // Arrange
        Room room = new Room();
        List<Player> players = new ArrayList<>();
        players.add(new PlayerBuilder("player1", 100).build());
        players.add(new PlayerBuilder("player2", 200).build());
        room.setPlayers(players);
        room.setGame(new Game());
        room.getGame().setCurrentBetSize(50);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> gameService.getCurrentPlayer(room, 2));
    }
}
