package xyz.korsak.pcoapi.player;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.queue.QueueService;
import xyz.korsak.pcoapi.requests.NameRequest;

@RestController
@RequestMapping(path = "api/v1/player")
public class PlayerController extends BaseController {
    private final QueueService queueService;

    @Autowired
    public PlayerController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/create")
    public ResponseEntity<Player> createPlayer(@RequestParam String queueId,
                                               @RequestBody NameRequest name) {
        Player player = queueService.addPlayerToQueue(queueId, name.getName());
        return ResponseEntity.ok(player);
    }
}
