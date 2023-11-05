package xyz.korsak.pcoapi.room;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private Authorization authorization;
    private RoomService roomService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        roomService = new RoomService(authorization, roomRepository);
    }

    @Test
    void testCreateRoom() {
        RedisRoomRepository roomRepository = Mockito.mock(RedisRoomRepository.class);

        RoomService roomService = new RoomService(authorization, roomRepository);

        Room r = roomService.createRoom("Test Room");

        assertEquals("Test Room", r.getName());
    }

    @Test
    public void testGetRoomById() {
        // Arrange
        String roomId = "123";
        Room expectedRoom = new Room(roomId, "Test Room", new ArrayList<>(), "456");
        when(roomRepository.findById(roomId)).thenReturn(expectedRoom);

        // Act
        Room retrievedRoom = roomService.getRoomById(roomId);

        // Assert
        Assertions.assertEquals(expectedRoom, retrievedRoom);
        verify(roomRepository, Mockito.times(1)).findById(roomId);
    }

    @Test
    public void testGetRoomByToken() {
        // Arrange
        String roomToken = "456";
        Room expectedRoom = new Room("123", "Test Room", new ArrayList<>(), roomToken);
        when(roomRepository.findByToken(roomToken)).thenReturn(expectedRoom);

        // Act
        Room retrievedRoom = roomService.getRoomByToken(roomToken);

        // Assert
        Assertions.assertEquals(expectedRoom, retrievedRoom);
        verify(roomRepository, Mockito.times(1)).findByToken(roomToken);
    }

    @Test
    public void testAddPlayerToRoom() {
        // Arrange
        String roomId = "123";
        Room room = new Room(roomId, "Test Room", new ArrayList<>(), "456");
        String playerName = "John Doe";
        String playerId = "789";
        String playerToken = "xyz";
        Player player = new PlayerBuilder(playerId, playerName, playerToken).build();
        String roomToken = "456";
        when(roomRepository.findById(roomId)).thenReturn(room);
        when(authorization.getRoomByIdWithOwnerAuthorization(roomId, roomToken)).thenReturn(room);

        // Act
        Player addedPlayer = roomService.addPlayerToRoom(roomId, player, roomToken);

        // Assert
        Assertions.assertNotNull(addedPlayer);
        Assertions.assertNotNull(addedPlayer.getToken());
        Assertions.assertEquals(addedPlayer.getName(), playerName);
        Assertions.assertEquals(1, room.getPlayers().size());
        Assertions.assertEquals(playerName, room.getPlayers().get(0).getName());
        verify(roomRepository, Mockito.times(1)).create(room);
    }


    @Test
    public void testGetPlayersInRoom() {
        // Arrange
        String roomId = "123";
        Room room = new Room(roomId, "Test Room", new ArrayList<>(), "456");
        List<Player> expectedPlayers = new ArrayList<>();
        Player player1 = new PlayerBuilder("John Doe").token("abc").build();
        Player player2 = new PlayerBuilder("Jane Smith").token("def").build();
        expectedPlayers.add(player1);
        expectedPlayers.add(player2);
        room.setPlayers(expectedPlayers);
        when(roomRepository.findById(roomId)).thenReturn(room);

        // Act
        GetPlayersResponse retrievedPlayers = roomService.getPlayersInRoom(roomId);

        // Assert
        Assertions.assertEquals(expectedPlayers.get(0).getName(), retrievedPlayers.getPlayers().get(0).getName());
        Assertions.assertEquals(expectedPlayers.get(1).getName(), retrievedPlayers.getPlayers().get(1).getName());
        Assertions.assertEquals(expectedPlayers.get(0).getId(), retrievedPlayers.getPlayers().get(0).getId());
        Assertions.assertEquals(expectedPlayers.get(1).getId(), retrievedPlayers.getPlayers().get(1).getId());
        verify(roomRepository, Mockito.times(1)).findById(roomId);
    }

    @Test
    public void testGetPlayerInRoom_ExistingPlayer_Success() throws UnauthorizedAccessException {
        // Arrange
        String roomId = "room1";
        String roomToken = "roomToken";
        String playerId = "player1";
        String playerToken = "token1";

        // Create a sample room with players
        List<Player> players = new ArrayList<>();
        Player player = new PlayerBuilder(playerId, "Player 1", 100, 0, playerToken).build();
        players.add(player);
        Room room = new Room(roomId, "Test Room", players, roomToken);

        // Mock the roomRepository.findById() method
        when(roomRepository.findById(roomId)).thenReturn(room);
        when(authorization.getPlayerWithAuthorization(roomId, playerId, playerToken)).thenReturn(player);

        // Act
        Player result = roomService.getPlayerInRoom(roomId, playerId, playerToken);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(playerId, result.getId());
        Assertions.assertEquals(playerToken, result.getToken());
    }

    @Test
    public void testGetPlayerInRoom_NonExistingRoom_UnauthorizedAccessException() {
        // Arrange
        String roomId = "room1";
        String playerId = "player1";
        String playerToken = "token1";

        // Mock the roomRepository.findById() method to return null (non-existing room)
        when(roomRepository.findById(roomId)).thenReturn(null);
        when(authorization.getPlayerWithAuthorization(roomId, playerId, playerToken)).thenThrow(new UnauthorizedAccessException());

        // Act and Assert
        Assertions.assertThrows(UnauthorizedAccessException.class, () -> {
            roomService.getPlayerInRoom(roomId, playerId, playerToken);
        });
    }

    @Test
    public void testGetPlayerInRoom_NonExistingPlayer_UnauthorizedAccessException() {
        // Arrange
        String roomId = "room1";
        String roomToken = "roomToken";
        String playerId = "player1";
        String playerToken = "token1";

        // Create a sample room with players
        List<Player> players = new ArrayList<>();
        players.add(new PlayerBuilder("player2", "Player 2", 100, 0, "token2").build());
        Room room = new Room(roomId, "Test Room", players, roomToken);

        // Mock the roomRepository.findById() method
        when(roomRepository.findById(roomId)).thenReturn(room);
        when(authorization.getPlayerWithAuthorization(roomId, playerId, playerToken)).thenThrow(new UnauthorizedAccessException());

        // Act and Assert
        Assertions.assertThrows(UnauthorizedAccessException.class, () -> {
            roomService.getPlayerInRoom(roomId, playerId, playerToken);
        });
    }

    @Test
    public void testDeleteRoom() {
        // Arrange
        String roomName = "room";
        Room r = roomService.createRoom(roomName);

        // Act
        when(roomRepository.findById(r.getId())).thenReturn(new Room(r.getId(), r.getName(), null, r.getToken()));
        when(authorization.authorizeRoomOwner(r.getId(), r.getToken())).thenReturn(true);
        roomService.deleteRoom(r.getId(), r.getToken());

        // Assert
        verify(roomRepository, Mockito.times(1)).delete(r.getId());
    }

    @Test
    void deletePlayerInRoom_ValidPlayer_SuccessfullyDeletesPlayer() throws UnauthorizedAccessException {
        // Arrange
        String roomId = "roomId";
        String playerId = "playerId";
        String token = "token";

        Room room = new Room(roomId, "Room", new ArrayList<>(), "roomToken");
        Player player = new PlayerBuilder(playerId, "Player", 100, 0, token).build();
        room.getPlayers().add(player);

        when(roomRepository.findById(roomId)).thenReturn(room);
        when(authorization.getPlayerWithAuthorization(roomId, playerId, token)).thenReturn(player);
        doNothing().when(roomRepository).create(room);

        // Act
        assertDoesNotThrow(() -> roomService.deletePlayerInRoom(roomId, playerId, token));

        // Assert
        assertFalse(room.getPlayers().contains(player));
        verify(roomRepository, times(1)).create(room);
    }

    @Test
    void deletePlayerInRoom_InvalidRoom_ThrowsUnauthorizedAccessException() {
        // Arrange
        String roomId = "invalidRoomId";
        String playerId = "playerId";
        String token = "token";

        when(roomRepository.findById(roomId)).thenReturn(null);

        // Act and Assert
        assertThrows(UnauthorizedAccessException.class, () -> roomService.deletePlayerInRoom(roomId, playerId, token));
        verify(roomRepository, never()).create(any(Room.class));
    }

    @Test
    void deletePlayerInRoom_InvalidPlayer_ThrowsUnauthorizedAccessException() {
        // Arrange
        String roomId = "roomId";
        String playerId = "invalidPlayerId";
        String token = "token";

        Room room = new Room(roomId, "Room", new ArrayList<>(), "roomToken");

        when(roomRepository.findById(roomId)).thenReturn(room);
        when(authorization.getPlayerWithAuthorization(roomId, playerId, token)).thenThrow(new UnauthorizedAccessException());

        // Act and Assert
        assertThrows(UnauthorizedAccessException.class, () -> roomService.deletePlayerInRoom(roomId, playerId, token));
        verify(roomRepository, never()).create(any(Room.class));
    }

    @Test
    void deletePlayerInRoom_UnauthorizedAccess_ThrowsUnauthorizedAccessException() {
        // Arrange
        String roomId = "roomId";
        String playerId = "playerId";
        String token = "invalidToken";

        Room room = new Room(roomId, "Room", new ArrayList<>(), "roomToken");
        Player player = new PlayerBuilder(playerId, "Player", 100, 0, "playerToken").build();
        room.getPlayers().add(player);

        when(roomRepository.findById(roomId)).thenReturn(room);
        when(authorization.getPlayerWithAuthorization(roomId, playerId, token)).thenThrow(new UnauthorizedAccessException());

        // Act and Assert
        assertThrows(UnauthorizedAccessException.class, () -> roomService.deletePlayerInRoom(roomId, playerId, token));
        verify(roomRepository, never()).create(any(Room.class));
    }
}
