package xyz.korsak.pcoapi.player;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.queue.QueueService;
import xyz.korsak.pcoapi.requests.NameRequest;
import xyz.korsak.pcoapi.responses.IdTokenResponse;
import xyz.korsak.pcoapi.room.RoomService;

@RestController
@RequestMapping(path = "api/v1/player")
public class PlayerController extends BaseController {
    private final QueueService queueService;
    private final RoomService roomService;

    public PlayerController(QueueService queueService, RoomService roomService) {
        this.queueService = queueService;
        this.roomService = roomService;
    }

    @PostMapping("/create")
    public ResponseEntity<IdTokenResponse> createPlayer(@RequestParam String queueId,
                                                        @RequestBody NameRequest name) {
        Player player = queueService.addPlayerToQueue(queueId, name.getName());
        IdTokenResponse r = new IdTokenResponse(player.getId(), player.getToken());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<Player> getPlayer(@RequestParam String roomId,
                                            @PathVariable String playerId,
                                            @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        Player player = roomService.getPlayerInRoom(roomId, playerId, playerToken);
        if (player == null) {
            throw new UnauthorizedAccessException();
        }
        return ResponseEntity.ok(player);
    }
}
