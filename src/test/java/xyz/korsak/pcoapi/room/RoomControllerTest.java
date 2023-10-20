package xyz.korsak.pcoapi.room;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.queue.Queue;
import xyz.korsak.pcoapi.queue.QueueService;
import xyz.korsak.pcoapi.requests.NameRequest;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.RoomCreatedResponse;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    @Test
    void getRoomByToken_ReturnsRoom_WhenTokenIsValid() {
        // Arrange
        String token = "abc123";
        Room expectedRoom = new Room("123", "Test Room", new ArrayList<>(), "456");

        RoomService roomService = mock(RoomService.class);
        when(roomService.getRoomByToken(eq(token))).thenReturn(expectedRoom);

        RoomController roomController = new RoomController(roomService, null);

        // Act
        Room response = roomController.getRoomByToken(token);

        // Assert
        verify(roomService, times(1)).getRoomByToken(eq(token));
        verifyNoMoreInteractions(roomService);
        assertEquals(expectedRoom.getId(), response.getId());
        assertEquals(expectedRoom.getName(), response.getName());
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
    void createRoom_ReturnsRoom_WhenValidRequest() {
        // Arrange
        NameRequest nameRequest = new NameRequest("Test Room");
        RoomCreatedResponse expectedResponse = new RoomCreatedResponse("123", "asd", "asd", "456");
        Room expectedRoom = new Room("123", "asd", new ArrayList<>(), "456");
        Queue expectedQueue = new Queue("123", "123123");

        RoomService roomService = mock(RoomService.class);
        QueueService queueService = mock(QueueService.class);
        when(queueService.createQueue(expectedRoom.getId())).thenReturn(expectedQueue);
        when(roomService.createRoom(eq(nameRequest.getName()))).thenReturn(expectedRoom);

        RoomController roomController = new RoomController(roomService, queueService);

        // Act
        ResponseEntity<RoomCreatedResponse> response = roomController.createRoom(nameRequest);

        // Assert
        verify(roomService, times(1)).createRoom(eq(nameRequest.getName()));
        verify(queueService, times(1)).createQueue(eq(expectedRoom.getId()));
        verify(roomService, times(1)).updateRoom(eq(expectedRoom));
        verifyNoMoreInteractions(roomService, queueService);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void addPlayerToRoom_AddsPlayerToRoom_WhenAuthorizedAndPlayerInQueue() {
        // Arrange
        String roomId = "123";
        String playerId = "456";
        String authorizationHeader = "Bearer <room-token>";
        Room room = new Room("123", "Test Room", new ArrayList<>(), "<room-token>");
        Player player = new Player("456", 0);

        RoomService roomService = mock(RoomService.class);
        QueueService queueService = mock(QueueService.class);
        when(roomService.getRoomById(eq(roomId))).thenReturn(room);
        when(queueService.removePlayerFromQueue(eq(room.getQueueId()), eq(playerId))).thenReturn(player);

        RoomController roomController = new RoomController(roomService, queueService);

        // Act
        ResponseEntity<Player> response = roomController.addPlayerToRoom(roomId, playerId, authorizationHeader);

        // Assert
        verify(roomService, times(1)).getRoomById(eq(roomId));
        verify(queueService, times(1)).removePlayerFromQueue(eq(room.getQueueId()), eq(playerId));
        verify(roomService, times(1)).addPlayerToRoom(eq(roomId), eq(player), eq("<room-token>"));
        verifyNoMoreInteractions(roomService, queueService);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(player, response.getBody());
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
