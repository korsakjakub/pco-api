package xyz.korsak.pcoapi.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;

@RestController
@RequestMapping(path = "api/v1/room")
public class RoomController {
    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/token/{token}")
    public Room getRoomByToken(@PathVariable String token) {
        return roomService.getRoomByToken(token);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable String id, @RequestHeader("Authorization") String authorizationHeader) {
        String roomToken = extractBearerToken(authorizationHeader);
        roomService.deleteRoom(id, roomToken);
    }

    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestBody NameRequest name) {
        Room r = roomService.createRoom(name.getName());
        if (r != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(r);
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/{roomId}/players")
    public ResponseEntity<Player> addPlayerToRoom(@PathVariable String roomId, @RequestBody NameRequest name) {
        Player r = roomService.addPlayerToRoom(roomId, name.getName());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/{roomId}/players")
    public ResponseEntity<RoomGetPlayersInRoomResponse> getPlayersInRoom(@PathVariable String roomId) {
        RoomGetPlayersInRoomResponse players = roomService.getPlayersInRoom(roomId);
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<Player> getPlayerInRoom(@PathVariable String roomId, @PathVariable String playerId, @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        Player player = roomService.getPlayerInRoom(roomId, playerId, playerToken);
        if (player == null) {
            throw new UnauthorizedAccessException("Unauthorized access");
        }
        return ResponseEntity.ok(player);
    }

    @DeleteMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<String> deletePlayerInRoom(@PathVariable String roomId, @PathVariable String playerId, @RequestHeader("Authorization") String authorizationHeader) {
        String playerToken = extractBearerToken(authorizationHeader);
        try {
            roomService.deletePlayerInRoom(roomId, playerId, playerToken);
        } catch(UnauthorizedAccessException ex) {
            throw new UnauthorizedAccessException("Unauthorized access");
        }
        return ResponseEntity.ok("Deleted the player with Id: " + playerId);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }
}
