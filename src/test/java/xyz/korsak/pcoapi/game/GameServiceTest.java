package xyz.korsak.pcoapi.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.GameException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomRepository;
import xyz.korsak.pcoapi.rules.PokerRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    @Test
    void getCurrentPlayer_InsufficientChips_ThrowsException() {
        // Arrange
        Room room = new Room();
        List<Player> players = new ArrayList<>();
        players.add(new PlayerBuilder("player1", 100).build());
        players.add(new PlayerBuilder("player2", 200).build());
        room.setPlayers(players);
        room.setGame(new Game());
        room.getGame().setCurrentBetSize(150);

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

    @Test
    void start_ValidAuthorization_CreatesGameInProgress() {
        // Arrange
        String roomId = "roomId";
        String roomToken = "roomToken";
        Room room = new Room();
        room.setPlayers(Collections.singletonList(new PlayerBuilder("player1", 100).build()));
        when(auth.authorizeRoomOwner(roomId, roomToken)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(room);

        // Act
        gameService.start(roomId, roomToken);

        // Assert
        assertEquals(GameState.IN_PROGRESS, room.getGame().getState());
        // Add more assertions if needed
    }

    @Test
    void start_InvalidAuthorization_ThrowsUnauthorizedAccessException() {
        // Arrange
        String roomId = "roomId";
        String roomToken = "roomToken";
        when(auth.authorizeRoomOwner(roomId, roomToken)).thenReturn(false);

        // Act and Assert
        assertThrows(UnauthorizedAccessException.class, () -> gameService.start(roomId, roomToken));
    }

    @Test
    void setRules_ValidAuthorization_SetsRules() {
        // Arrange
        String roomId = "roomId";
        String roomToken = "roomToken";
        PokerRules pokerRules = new PokerRules();

        // Create a Room using the alternative constructor
        Room room = new Room(roomId, "RoomName", Arrays.asList(new PlayerBuilder().build(), new PlayerBuilder().build()), roomToken);

        when(auth.authorizeRoomOwner(roomId, roomToken)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(room);

        // Act
        gameService.setRules(roomId, roomToken, pokerRules);

        // Assert
        assertEquals(pokerRules, room.getGame().getRules());
        // Add more assertions if needed
    }

}
