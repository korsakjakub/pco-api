package xyz.korsak.pcoapi.room;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    @Test
    void getRoomByToken_ReturnsRoom_WhenTokenIsValid() {
        // Arrange
        String token = "abc123";
        Room expectedRoom = new Room("123", new ArrayList<>(), "456");

        RoomService roomService = mock(RoomService.class);
        when(roomService.getRoomByToken(eq(token))).thenReturn(expectedRoom);

        RoomController roomController = new RoomController(roomService, null);

        // Act
        Room response = roomController.getRoomByToken(token);

        // Assert
        verify(roomService, times(1)).getRoomByToken(eq(token));
        verifyNoMoreInteractions(roomService);
        assertEquals(expectedRoom.getId(), response.getId());
    }

    @Test
    void deleteRoom_DeletesRoom_WhenAuthorized() {
        // Arrange
        String roomId = "123";
        String authorizationHeader = "Bearer <room-token>";

        RoomService roomService = mock(RoomService.class);
        RoomController roomController = new RoomController(roomService, null);

        // Act
        roomController.deleteRoom(roomId, authorizationHeader);

        // Assert
        verify(roomService, times(1)).deleteRoom(eq(roomId), eq("<room-token>"));
        verifyNoMoreInteractions(roomService);
    }

    @Test
    void getPlayersInRoom_ReturnsPlayersInRoom_WhenValidRoomId() {
        // Arrange
        String roomId = "123";
        GetPlayersResponse expectedPlayers = new GetPlayersResponse(new ArrayList<>());

        RoomService roomService = mock(RoomService.class);
        when(roomService.getPlayersInRoom(eq(roomId))).thenReturn(expectedPlayers);

        RoomController roomController = new RoomController(roomService, null);

        // Act
        ResponseEntity<GetPlayersResponse> response = roomController.getPlayersInRoom(roomId);

        // Assert
        verify(roomService, times(1)).getPlayersInRoom(eq(roomId));
        verifyNoMoreInteractions(roomService);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPlayers, response.getBody());
    }

    @Test
    void deletePlayerInRoom_DeletesPlayer_WhenAuthorized() {
        // Arrange
        String roomId = "123";
        String playerId = "456";
        String authorizationHeader = "Bearer <player-token>";

        RoomService roomService = mock(RoomService.class);
        RoomController roomController = new RoomController(roomService, null);

        // Act
        roomController.deletePlayerInRoom(roomId, playerId, authorizationHeader);

        // Assert
        verify(roomService, times(1)).deletePlayerInRoom(eq(roomId), eq(playerId), eq("<player-token>"));
        verifyNoMoreInteractions(roomService);
    }
}
