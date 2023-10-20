package xyz.korsak.pcoapi.player;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.queue.QueueService;
import xyz.korsak.pcoapi.room.RoomService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PlayerControllerTest {

    @Test
    void getPlayer_ReturnsPlayer_WhenAuthorized() {
        // Arrange
        String roomId = "123";
        String playerId = "456";
        String authorizationHeader = "Bearer <player-token>";
        Player expectedPlayer = new Player("456", 0);

        RoomService roomService = mock(RoomService.class);
        QueueService queueService = mock(QueueService.class);
        when(roomService.getPlayerInRoom(eq(roomId), eq(playerId), anyString())).thenReturn(expectedPlayer);

        PlayerController playerController = new PlayerController(queueService, roomService);

        // Act
        ResponseEntity<Player> response = playerController.getPlayer(roomId, playerId, authorizationHeader);

        // Assert
        verify(roomService, times(1)).getPlayerInRoom(eq(roomId), eq(playerId), anyString());
        verifyNoMoreInteractions(roomService);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPlayer, response.getBody());
    }

    @Test
    void getPlayer_ThrowsUnauthorizedAccessException_WhenPlayerIsNull() {
        // Arrange
        String roomId = "123";
        String playerId = "456";
        String authorizationHeader = "Bearer <player-token>";

        RoomService roomService = mock(RoomService.class);
        QueueService queueService = mock(QueueService.class);
        when(roomService.getPlayerInRoom(eq(roomId), eq(playerId), anyString())).thenReturn(null);

        PlayerController playerController = new PlayerController(queueService, roomService);

        // Act and Assert
        assertThrows(UnauthorizedAccessException.class,
                () -> playerController.getPlayer(roomId, playerId, authorizationHeader));

        verify(roomService, times(1)).getPlayerInRoom(eq(roomId), eq(playerId), anyString());
        verifyNoMoreInteractions(roomService);
    }
}
