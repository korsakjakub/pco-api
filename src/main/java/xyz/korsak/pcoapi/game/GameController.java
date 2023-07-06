package xyz.korsak.pcoapi.game;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.rules.RulesDTO;

@RestController
@RequestMapping("/api/v1/game")
public class GameController {
    @PostMapping("/start")
    public ResponseEntity<String> startGame(@RequestHeader("Authorization") String authorization, @RequestParam("roomId") String roomId) {
        return ResponseEntity.ok("Game started successfully");
    }

    @PostMapping("{roomId}/rules")
    public ResponseEntity<String> setRules(@RequestHeader("Authorization") String authorization,
                                           @PathVariable("roomId") String roomId,
                                           @RequestBody RulesDTO rules) {
        return ResponseEntity.ok("Game started successfully");
    }
}
