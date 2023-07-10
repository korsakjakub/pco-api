package xyz.korsak.pcoapi.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.GameException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        players.add(new Player("player1", 100L));
        players.add(new Player("player2", 200L));
        room.setPlayers(players);
        room.setGame(new Game());
        room.getGame().setCurrentBetSize(50L);

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
        players.add(new Player("player1", 100L));
        players.add(new Player("player2", 200L));
        room.setPlayers(players);
        room.setGame(new Game());
        room.getGame().setCurrentBetSize(50L);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> gameService.getCurrentPlayer(room, 2));
    }

    @Test
    void getCurrentPlayer_InsufficientChips_ThrowsException() {
        // Arrange
        Room room = new Room();
        List<Player> players = new ArrayList<>();
        players.add(new Player("player1", 100L));
        players.add(new Player("player2", 200L));
        room.setPlayers(players);
        room.setGame(new Game());
        room.getGame().setCurrentBetSize(150L);

        // Act and Assert
        assertThrows(GameException.class, () -> gameService.getCurrentPlayer(room, 0));
    }

    @Test
    void getRoomWithAuthorization_ValidAuthorization_ReturnsRoom() {
        // Arrange
        String roomId = "roomId";
        String playerId = "playerId";
        String playerToken = "playerToken";
        Room room = new Room();
        when(auth.authorizePlayer(roomId, playerId, playerToken)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(room);
        room.setGame(new Game());
        room.getGame().setState(GameState.IN_PROGRESS);
        room.getGame().setCurrentTurnIndex(0);

        // Act
        Room result = gameService.getRoomWithAuthorization(roomId, playerId, playerToken);

        // Assert
        assertNotNull(result);
        assertEquals(room, result);
    }

    @Test
    void getRoomWithAuthorization_InvalidAuthorization_ThrowsException() {
        // Arrange
        String roomId = "roomId";
        String playerId = "playerId";
        String playerToken = "playerToken";
        when(auth.authorizePlayer(roomId, playerId, playerToken)).thenReturn(false);

        // Act and Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                gameService.getRoomWithAuthorization(roomId, playerId, playerToken));
    }

    @Test
    void getRoomWithAuthorization_InvalidGameState_ThrowsException() {
        // Arrange
        String roomId = "roomId";
        String playerId = "playerId";
        String playerToken = "playerToken";
        Room room = new Room();
        when(auth.authorizePlayer(roomId, playerId, playerToken)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(room);
        room.setGame(new Game());
        room.getGame().setState(GameState.WAITING);
        room.getGame().setCurrentTurnIndex(0);

        // Act and Assert
        assertThrows(GameException.class, () ->
                gameService.getRoomWithAuthorization(roomId, playerId, playerToken));
    }
}
