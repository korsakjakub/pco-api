package xyz.korsak.pcoapi.room;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class RoomServiceTest {
    @Test
    void testCreateRoom() {
        RedisRoomRepository roomRepository = Mockito.mock(RedisRoomRepository.class);

        RoomService roomService = new RoomService(roomRepository);

        when(roomRepository.existsWithName("Test Room")).thenReturn(true);

        Room room = roomService.createRoom("Test Room");

        assertEquals("Test Room", room.getName());
    }

    @Test
    void testGetPlayersInRoom_Successful() {
        String roomId = "123";
        String token = "valid-token";

        RedisRoomRepository roomRepository = Mockito.mock(RedisRoomRepository.class);

        RoomService roomService = new RoomService(roomRepository);

        Room room = new Room();
        room.setId(roomId);
        room.setToken(token);
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        List<Player> players = Arrays.asList(player1, player2);
        room.setPlayers(players);

        when(roomRepository.findById(roomId)).thenReturn(room);

        List<Player> result = roomService.getPlayersInRoom(roomId, token);

        assertEquals(players, result);

        verify(roomRepository, times(1)).findById(roomId);
    }

    @Test
    void testGetPlayersInRoom_Unauthorized() {
        String roomId = "123";
        String token = "invalid-token";

        RedisRoomRepository roomRepository = Mockito.mock(RedisRoomRepository.class);

        RoomService roomService = new RoomService(roomRepository);

        when(roomRepository.findById(roomId)).thenReturn(null);

        assertThrows(UnauthorizedAccessException.class, () -> roomService.getPlayersInRoom(roomId, token));

        verify(roomRepository, times(1)).findById(roomId);
    }
}
