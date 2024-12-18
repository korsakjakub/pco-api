package xyz.korsak.pcoapi.player;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.queue.QueueService;
import xyz.korsak.pcoapi.requests.ChipsRequest;
import xyz.korsak.pcoapi.requests.NewPlayerRequest;
import xyz.korsak.pcoapi.responses.IdResponse;
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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPlayers(@RequestParam("roomId") String roomId) {
        return roomService.streamPlayersInRoom(roomId);
    }

    @PostMapping("/create")
    public ResponseEntity<IdTokenResponse> createPlayer(@RequestParam String queueId,
                                                        @RequestBody NewPlayerRequest request) {
        Player player = queueService.addPlayerToQueue(queueId, request.name(), request.avatar());
        queueService.pushData(queueId);
        IdTokenResponse r = new IdTokenResponse(player.getId(), player.getToken());
        return logResponse(ResponseEntity.ok(r));
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerDTO> getPlayer(@RequestParam String roomId,
                                            @PathVariable String playerId,
                                            @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        Player player = roomService.getPlayerInRoom(roomId, playerId, playerToken);
        if (player == null) {
            throw new UnauthorizedAccessException();
        }
        return logResponse(ResponseEntity.ok(new PlayerDTO(player)));
    }

    @PutMapping("/{playerId}/chips")
    public ResponseEntity<IdResponse> updatePlayerChips(@RequestParam String roomId,
                                                        @PathVariable String playerId,
                                                        @RequestHeader("Authorization") String authorizationHeader,
                                                        @RequestBody ChipsRequest chipsRequest) {
        String roomToken = extractBearerToken(authorizationHeader);
        roomService.updatePlayerChips(roomId, playerId, roomToken, chipsRequest.chips());

        roomService.pushData(roomId);
        return logResponse(ResponseEntity.ok(new IdResponse(playerId)));
    }

    @PostMapping("/{playerId}/sit-out")
    public ResponseEntity<Boolean> toggleSittingOut(@RequestParam String roomId,
                                                    @PathVariable String playerId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        roomService.toggleSittingOut(roomId, playerId, playerToken);

        roomService.pushData(roomId);
        return logResponse(ResponseEntity.ok(true));
    }

}
