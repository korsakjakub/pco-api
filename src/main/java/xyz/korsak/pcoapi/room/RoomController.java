package xyz.korsak.pcoapi.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.korsak.pcoapi.player.Player;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/room")
public class RoomController {
    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping(value = "/{id}")
    public Room getRoomById(@PathVariable String id) {
        return roomService.getRoomById(id);
    }

    @GetMapping("/token/{token}")
    public Room getRoomByToken(@PathVariable String token) {
        return roomService.getRoomByToken(token);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable String id) {
        roomService.deleteRoom(id);
    }

    @PostMapping(path = "/create")
    @ResponseBody
    public Room createRoom(@RequestBody String name) {
        return roomService.createRoom(name);
    }

    @PostMapping("/{roomId}/players")
    public String addPlayerToRoom(@PathVariable String roomId, @RequestBody Player player) {
        return roomService.addPlayerToRoom(roomId, player);
    }

    @GetMapping("/{roomId}/players")
    public List<Player> getPlayersInRoom(@PathVariable String roomId, @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);

        return getPlayersInRoom(roomId, token);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }
}
