package xyz.korsak.pcoapi.queue;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdResponse;
import xyz.korsak.pcoapi.BaseController;

@RestController
@RequestMapping(path = "api/v1/queue")
public class QueueController extends BaseController {
    QueueService queueService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPlayersInQueue(@RequestParam("queueId") String queueId) {
        return queueService.streamPlayersInQueue(queueId);
    }

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
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
}
