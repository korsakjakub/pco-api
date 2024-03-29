package xyz.korsak.pcoapi.queue;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdResponse;

@RestController
@RequestMapping(path = "api/v1/queue")
public class QueueController extends BaseController {
    QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPlayersInQueue(@RequestParam("queueId") String queueId) {
        return queueService.streamPlayersInQueue(queueId);
    }

    @GetMapping("/{queueId}/players")
    public ResponseEntity<GetPlayersResponse> getPlayersInQueue(@PathVariable String queueId) {
        GetPlayersResponse players = queueService.getPlayersInQueue(queueId);
        return logResponse(ResponseEntity.ok(players));
    }

    @GetMapping("/{queueId}/roomid")
    public ResponseEntity<IdResponse> getRoomId(@PathVariable String queueId) {
        IdResponse roomId = queueService.getRoomId(queueId);
        return logResponse(ResponseEntity.ok(roomId));
    }

    @DeleteMapping("/{queueId}/players/{playerId}")
    public ResponseEntity<String> deletePlayerInQueue(@PathVariable String queueId,
                                                     @PathVariable String playerId,
                                                     @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        queueService.removePlayerFromQueue(queueId, playerId, playerToken);
        queueService.pushData(queueId);
        return logResponse(ResponseEntity.ok("Deleted the player with Id: " + playerId));
    }
}
