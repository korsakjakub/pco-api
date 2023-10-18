package xyz.korsak.pcoapi.game;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.requests.IdChipsRequest;
import xyz.korsak.pcoapi.requests.IdRequest;
import xyz.korsak.pcoapi.responses.GetGameResponse;
import xyz.korsak.pcoapi.responses.IdResponse;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomService;
import xyz.korsak.pcoapi.rules.PokerRules;

@RestController
@RequestMapping("/api/v1/game")
public class GameController extends BaseController {
    private final GameService gameService;
    private final RoomService roomService;

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

    @GetMapping("")
    public ResponseEntity<GetGameResponse> getGame(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        List<Player> p = room.getPlayers();

        if (p.isEmpty()) {
            throw new NotFoundException("No players found");
        }

        GetGameResponse r = new GetGameResponse(game, room.getPlayers());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/rules")
    public ResponseEntity<PokerRules> getRules(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        return ResponseEntity.ok(game.getRules());
    }

    @GetMapping("/state")
    public ResponseEntity<IdResponse> getState(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        IdResponse r = new IdResponse(game.getState().toString());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/stage")
    public ResponseEntity<String> getStage(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();

        return ResponseEntity.ok(game.getStage().toString());
    }

    @GetMapping("/current-player")
    public ResponseEntity<IdResponse> getCurrentPlayer(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        if (game == null) {
            throw new NotFoundException("There is no game for room with ID: " + roomId);
        }
        Player currentPlayer = gameService.getCurrentPlayer(room, game.getCurrentTurnIndex());
        IdResponse r = new IdResponse(currentPlayer.getId());
        return ResponseEntity.ok(r);
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
