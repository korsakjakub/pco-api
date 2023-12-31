package xyz.korsak.pcoapi.game;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseController;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.requests.ChipsRequest;
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

    private void notifyPlayers(String roomId) {
        gameService.pushData(roomId);
        roomService.pushData(roomId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGame(@RequestParam("roomId") String roomId) {
        SseEmitter emitter = gameService.streamGame(roomId);
        notifyPlayers(roomId);
        return emitter;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startGame(@RequestParam("roomId") String roomId,
                                            @RequestHeader("Authorization") String authorization) {
        String roomToken = extractBearerToken(authorization);
        gameService.start(roomId, roomToken);
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok("Game started successfully"));
    }

    @PostMapping("/rules")
    public ResponseEntity<String> setRules(@RequestHeader("Authorization") String authorization,
                                           @RequestParam("roomId") String roomId,
                                           @RequestBody PokerRules rules) {
        String roomToken = extractBearerToken(authorization);
        gameService.setRules(roomId, roomToken, rules);
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok("Rules set successfully"));
    }

    @GetMapping("")
    public ResponseEntity<GetGameResponse> getGameStream(@RequestParam("roomId") String roomId) {
        GetGameResponse r = gameService.getGameResponse(roomId);
        return logResponse(ResponseEntity.ok(r));
    }

    @GetMapping("/rules")
    public ResponseEntity<PokerRules> getRules(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        return logResponse(ResponseEntity.ok(game.getRules()));
    }

    @GetMapping("/state")
    public ResponseEntity<IdResponse> getState(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        IdResponse r = new IdResponse(game.getState().toString());
        return logResponse(ResponseEntity.ok(r));
    }

    @GetMapping("/stage")
    public ResponseEntity<String> getStage(@RequestParam("roomId") String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();

        return logResponse(ResponseEntity.ok(game.getStage().toString()));
    }

    public IdResponse currentPlayer(String roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        Game game = room.getGame();
        if (game == null) {
            throw new NotFoundException("There is no game for room with ID: " + roomId);
        }
        Player currentPlayer = gameService.getCurrentPlayer(room, game.getCurrentTurnIndex());
        return new IdResponse(currentPlayer.getId());
    }

    @GetMapping("/current-player")
    public ResponseEntity<IdResponse> getCurrentPlayer(@RequestParam("roomId") String roomId) {
        return logResponse(ResponseEntity.ok(currentPlayer(roomId)));
    }

    @PostMapping("/fold")
    public ResponseEntity<String> fold(@RequestParam String roomId,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        gameService.fold(roomId, extractBearerToken(authorizationHeader));
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok("Folded"));
    }

    @PostMapping("/bet")
    public ResponseEntity<String> bet(@RequestParam String roomId,
                                      @RequestBody ChipsRequest request,
                                      @RequestHeader("Authorization") String authorizationHeader) {
        gameService.bet(roomId, extractBearerToken(authorizationHeader), request.getChips());
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok("Bet"));
    }

    @PostMapping("/raise")
    public ResponseEntity<String> raise(@RequestParam String roomId,
                                        @RequestBody ChipsRequest request,
                                        @RequestHeader("Authorization") String authorizationHeader) {
        gameService.raise(roomId,  extractBearerToken(authorizationHeader), request.getChips());
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok("Raised"));
    }

    @PostMapping("/call")
    public ResponseEntity<String> call(@RequestParam String roomId,
                                       @RequestHeader("Authorization") String authorizationHeader) {
        gameService.call(roomId, extractBearerToken(authorizationHeader));
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok("Called"));
    }

    @PostMapping("/check")
    public ResponseEntity<String> check(@RequestParam String roomId,
                                        @RequestHeader("Authorization") String authorizationHeader) {
        gameService.check(roomId, extractBearerToken(authorizationHeader));
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok("Checked"));
    }

    @PostMapping("/decide-winner")
    public ResponseEntity<IdResponse> decideWinner(@RequestParam String roomId,
                                               @RequestBody IdRequest playerIdRequest,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        gameService.decideWinner(roomId, playerIdRequest.getId(), extractBearerToken(authorizationHeader));
        notifyPlayers(roomId);
        return logResponse(ResponseEntity.ok(new IdResponse(playerIdRequest.getId())));
    }

}
