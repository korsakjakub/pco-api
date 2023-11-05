package xyz.korsak.pcoapi.room;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.queue.Queue;
import xyz.korsak.pcoapi.queue.QueueService;
import xyz.korsak.pcoapi.requests.NameRequest;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.RoomCreatedResponse;

@Slf4j
@RestController
@RequestMapping(path = "api/v1/room")
public class RoomController extends BaseController {
    private final RoomService roomService;
    private final QueueService queueService;

    public RoomController(RoomService roomService, QueueService queueService) {
        this.roomService = roomService;
        this.queueService = queueService;
    }

    @GetMapping("/token/{token}")
    public Room getRoomByToken(@PathVariable String token) {
        return roomService.getRoomByToken(token);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable String id,
                           @RequestHeader("Authorization") String authorizationHeader) {
        String roomToken = extractBearerToken(authorizationHeader);
        roomService.deleteRoom(id, roomToken);
    }

    @PostMapping("/create")
    public ResponseEntity<RoomCreatedResponse> createRoom(@RequestBody NameRequest name) {
        Room room = roomService.createRoom(name.getName());
        if (room == null) {
            return ResponseEntity.unprocessableEntity().build();
        }
        Queue queue = queueService.createQueue(room.getId());
        room.setQueueId(queue.getId());
        roomService.updateRoom(room);
        RoomCreatedResponse r = new RoomCreatedResponse(room.getId(), room.getName(), queue.getId(), room.getToken());

        return logResponse(ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @PostMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<Player> addPlayerToRoom(@PathVariable String roomId,
                                                  @PathVariable String playerId,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        String roomToken = extractBearerToken(authorizationHeader);
        Room room = roomService.getRoomById(roomId);
        if (room == null || !room.getToken().equals(roomToken)) {
            throw new UnauthorizedAccessException();
        }

        Player player = queueService.removePlayerFromQueue(room.getQueueId(), playerId);
        if (player == null) {
            throw new UnauthorizedAccessException();
        }
        roomService.addPlayerToRoom(roomId, player, roomToken);
        return logResponse(ResponseEntity.status(HttpStatus.OK).body(player));
    }

    @PostMapping("/{roomId}/players")
    public ResponseEntity<Player> createPlayerInRoom(@PathVariable String roomId,
                                                     @RequestBody NameRequest name,
                                                     @RequestHeader("Authorization") String authorizationHeader) {
        String roomToken = extractBearerToken(authorizationHeader);
        Room room = roomService.getRoomById(roomId);
        if (room == null || !room.getToken().equals(roomToken)) {
            throw new UnauthorizedAccessException();
        }
        Player player = roomService.createPlayerInRoom(roomId, roomToken, name.getName());
        return logResponse(ResponseEntity.status(HttpStatus.OK).body(player));
    }

    @GetMapping("/{roomId}/players")
    public ResponseEntity<GetPlayersResponse> getPlayersInRoom(@PathVariable String roomId) {
        GetPlayersResponse players = roomService.getPlayersInRoom(roomId);
        return logResponse(ResponseEntity.ok(players));
    }

    @DeleteMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<String> deletePlayerInRoom(@PathVariable String roomId,
                                                     @PathVariable String playerId,
                                                     @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        try {
            roomService.deletePlayerInRoom(roomId, playerId, playerToken);
        } catch (UnauthorizedAccessException ex) {
            throw new UnauthorizedAccessException();
        }
        return logResponse(ResponseEntity.ok("Deleted the player with Id: " + playerId));
    }
}
