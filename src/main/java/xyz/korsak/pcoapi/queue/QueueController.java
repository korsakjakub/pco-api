package xyz.korsak.pcoapi.queue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;
import xyz.korsak.pcoapi.responses.IdResponse;
import xyz.korsak.pcoapi.BaseController;

@RestController
@RequestMapping(path = "api/v1/queue")
public class QueueController extends BaseController {
    QueueService queueService;

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
