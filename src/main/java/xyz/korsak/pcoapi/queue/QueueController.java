package xyz.korsak.pcoapi.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

@RestController
@RequestMapping(path = "api/v1/queue")
public class QueueController {
    QueueService queueService;

    @Autowired
    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/{queueId}/players")
    public ResponseEntity<GetPlayersResponse> getPlayersInQueue(@PathVariable String queueId) {
        GetPlayersResponse players = queueService.getPlayersInQueue(queueId);
        return ResponseEntity.ok(players);
    }
}
