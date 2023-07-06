package xyz.korsak.pcoapi.room;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping(path = "api/v1/room")
public class RoomController extends BaseController {
    private final RoomService roomService;
    private final QueueService queueService;

    @Autowired
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
    public ResponseEntity<Room> createRoom(@RequestBody NameRequest name) {
        Room r = roomService.createRoom(name.getName());
        if (r == null) {
            return ResponseEntity.unprocessableEntity().build();
        }
        Queue queue = queueService.createQueue(r.getId());
        r.setQueueId(queue.getId());
        roomService.updateRoom(r);

        return ResponseEntity.status(HttpStatus.CREATED).body(r);
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
        return ResponseEntity.status(HttpStatus.OK).body(player);
    }

    @GetMapping("/{roomId}/players")
    public ResponseEntity<GetPlayersResponse> getPlayersInRoom(@PathVariable String roomId) {
        GetPlayersResponse players = roomService.getPlayersInRoom(roomId);
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<Player> getPlayerInRoom(@PathVariable String roomId,
                                                  @PathVariable String playerId,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        Player player = roomService.getPlayerInRoom(roomId, playerId, playerToken);
        if (player == null) {
            throw new UnauthorizedAccessException();
        }
        return ResponseEntity.ok(player);
    }

    @DeleteMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<String> deletePlayerInRoom(@PathVariable String roomId,
                                                     @PathVariable String playerId,
                                                     @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        try {
            roomService.deletePlayerInRoom(roomId, playerId, playerToken);
        } catch(UnauthorizedAccessException ex) {
            throw new UnauthorizedAccessException();
        }
        return ResponseEntity.ok("Deleted the player with Id: " + playerId);
    }
}
