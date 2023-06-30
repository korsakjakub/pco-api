package xyz.korsak.pcoapi.room;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    private RoomService roomService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        roomService = new RoomService(roomRepository);
    }

    @Test
    void testCreateRoom() {
        RedisRoomRepository roomRepository = Mockito.mock(RedisRoomRepository.class);

        RoomService roomService = new RoomService(roomRepository);

        Room room = roomService.createRoom("Test Room");

        assertEquals("Test Room", room.getName());
    }

    @Test
    public void testGetRoomById() {
        // Arrange
        String roomId = "123";
        Room expectedRoom = new Room(roomId, "Test Room", "456", new ArrayList<>());
        Mockito.when(roomRepository.findById(roomId)).thenReturn(expectedRoom);

        // Act
        Room retrievedRoom = roomService.getRoomById(roomId);

        // Assert
        Assertions.assertEquals(expectedRoom, retrievedRoom);
        Mockito.verify(roomRepository, Mockito.times(1)).findById(roomId);
    }

    @Test
    public void testGetRoomByToken() {
        // Arrange
        String roomToken = "456";
        Room expectedRoom = new Room("123", "Test Room", roomToken, new ArrayList<>());
        Mockito.when(roomRepository.findByToken(roomToken)).thenReturn(expectedRoom);

        // Act
        Room retrievedRoom = roomService.getRoomByToken(roomToken);

        // Assert
        Assertions.assertEquals(expectedRoom, retrievedRoom);
        Mockito.verify(roomRepository, Mockito.times(1)).findByToken(roomToken);
    }

    @Test
    public void testAddPlayerToRoom() {
        // Arrange
        String roomId = "123";
        Room room = new Room(roomId, "Test Room", "456", new ArrayList<>());
        Player player = new Player("John Doe");
        Mockito.when(roomRepository.findById(roomId)).thenReturn(room);

        // Act
        String addedToken = roomService.addPlayerToRoom(roomId, player);

        // Assert
        Assertions.assertEquals(1, room.getPlayers().size());
        Assertions.assertEquals(player, room.getPlayers().get(0));
        Mockito.verify(roomRepository, Mockito.times(1)).findById(roomId);
        Mockito.verify(roomRepository, Mockito.times(1)).create(room);
    }

    @Test
    public void testGetPlayersInRoom() {
        // Arrange
        String roomId = "123";
        Room room = new Room(roomId, "Test Room", "456", new ArrayList<>());
        List<Player> expectedPlayers = new ArrayList<>();
        Player player1 = new Player("John Doe");
        player1.setToken("abc");
        Player player2 = new Player("Jane Smith");
        player2.setToken("def");
        expectedPlayers.add(player1);
        expectedPlayers.add(player2);
        room.setPlayers(expectedPlayers);
        Mockito.when(roomRepository.findById(roomId)).thenReturn(room);

        // Act
        List<Player> retrievedPlayers = roomService.getPlayersInRoom(roomId);

        // Assert
        Assertions.assertEquals(expectedPlayers, retrievedPlayers);
        Mockito.verify(roomRepository, Mockito.times(1)).findById(roomId);
    }

    @Test
    public void testGetPlayerInRoom_WhenAuthorizedAccess() {
        // Arrange
        String roomId = "123";
        String playerToken = "abc";
        Room room = new Room(roomId, "Test Room", "456", new ArrayList<>());
        List<Player> players = new ArrayList<>();
        Player player = new Player("John Doe");
        player.setToken(playerToken);
        players.add(player);
        room.setPlayers(players);
        Mockito.when(roomRepository.findById(roomId)).thenReturn(room);

        // Act
        Player retrievedPlayer = roomService.getPlayerInRoom(roomId, playerToken);

        // Assert
        Assertions.assertEquals(player, retrievedPlayer);
        Mockito.verify(roomRepository, Mockito.times(1)).findById(roomId);
    }

    @Test
    public void testGetPlayerInRoom_WhenUnauthorizedAccess() {
        // Arrange
        String roomId = "123";
        String playerToken = "xyz";
        Room room = new Room(roomId, "Test Room", "456", new ArrayList<>());
        List<Player> players = new ArrayList<>();
        Player player = new Player("John Doe");
        player.setToken("abc");
        players.add(player);
        room.setPlayers(players);
        Mockito.when(roomRepository.findById(roomId)).thenReturn(room);

        // Act and Assert
        Assertions.assertThrows(UnauthorizedAccessException.class, () ->
                roomService.getPlayerInRoom(roomId, playerToken));
        Mockito.verify(roomRepository, Mockito.times(1)).findById(roomId);
    }

    @Test
    public void testDeleteRoom() {
        // Arrange
        String roomId = "123";

        // Act
        roomService.deleteRoom(roomId);

        // Assert
        Mockito.verify(roomRepository, Mockito.times(1)).delete(roomId);
    }
}
