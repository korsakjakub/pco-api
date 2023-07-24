package xyz.korsak.pcoapi.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.requests.IdChipsRequest;
import xyz.korsak.pcoapi.requests.IdRequest;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomService;
import xyz.korsak.pcoapi.rules.PokerRules;

@RestController
@RequestMapping("/api/v1/game")
public class GameController extends BaseController {
    private final GameService gameService;
    private final RoomService roomService;

    @Autowired
    public GameController(GameService gameService, RoomService roomService) {
        this.gameService = gameService;
        this.roomService = roomService;
    }
    @PostMapping("/start")
    public ResponseEntity<String> startGame(@RequestParam("roomId") String roomId,
                                            @RequestHeader("Authorization") String authorization) {
        String roomToken = extractBearerToken(authorization);
        gameService.start(roomId, roomToken);
        return ResponseEntity.ok("Game started successfully");
    }

    @PostMapping("/rules")
    public ResponseEntity<String> setRules(@RequestHeader("Authorization") String authorization,
                                           @RequestParam("roomId") String roomId,
                                           @RequestBody PokerRules rules) {
        String roomToken = extractBearerToken(authorization);
        gameService.setRules(roomId, roomToken, rules);
        return ResponseEntity.ok("Game started successfully");
    }

    @GetMapping("/current-player")
    public ResponseEntity<Player> getCurrentPlayer(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        Player currentPlayer = gameService.getCurrentPlayer(room, game.getCurrentTurnIndex());

        return ResponseEntity.ok(currentPlayer);
    }

    @PostMapping("/bet")
    public ResponseEntity<String> bet(@RequestParam String roomId,
                                       @RequestBody IdChipsRequest request,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        gameService.bet(roomId, request.getId(), extractBearerToken(authorizationHeader), request.getChips());
        return ResponseEntity.ok("Bet");
    }

    @PostMapping("/raise")
    public ResponseEntity<String> raise(@RequestParam String roomId,
                                      @RequestBody IdChipsRequest request,
                                      @RequestHeader("Authorization") String authorizationHeader) {
        gameService.raise(roomId, request.getId(), extractBearerToken(authorizationHeader), request.getChips());
        return ResponseEntity.ok("Raised");
    }

    @PostMapping("/call")
    public ResponseEntity<String> call(@RequestParam String roomId,
                                       @RequestBody IdRequest request,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        gameService.call(roomId, request.getId(), extractBearerToken(authorizationHeader));
        return ResponseEntity.ok("Called");
    }

    @PostMapping("/check")
    public ResponseEntity<String> check(@RequestParam String roomId,
                                        @RequestBody IdRequest request,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        gameService.check(roomId, request.getId(), extractBearerToken(authorizationHeader));
        return ResponseEntity.ok("Checked");
    }

}
