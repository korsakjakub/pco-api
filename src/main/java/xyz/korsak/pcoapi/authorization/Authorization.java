package xyz.korsak.pcoapi.authorization;

import org.springframework.stereotype.Component;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.room.Room;
import xyz.korsak.pcoapi.room.RoomRepository;

import java.util.List;
import java.util.Optional;

@Component
public class Authorization {
    private final RoomRepository roomRepository;
    public Authorization(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    public boolean authorizeRoomOwner(String roomId, String roomToken) {
        Room room = roomRepository.findById(roomId);
        return room != null && room.token().equals(roomToken);
    }

    /*
    token can be either roomToken or playerToken. The point is that both room owner and player should be able to access
    player related info
     */
    public Player getPlayerWithAuthorization(String roomId, String playerId, String token) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        List<Player> players = room.players();

        Optional<Player> player = players.stream()
                .filter(p -> (p.getToken().equals(token) || room.token().equals(token)) && p.getId().equals(playerId))
                .findFirst();

        if (player.isEmpty()) {
            throw new UnauthorizedAccessException();
        }
        return player.get();
    }

    public Room getRoomByIdWithOwnerAuthorization(String id, String roomToken) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(id);
        if (room == null || !room.token().equals(roomToken)) {
            throw new UnauthorizedAccessException();
        }
        return room;
    }


    public boolean playerIsNotAuthorized(String roomId, String playerId, String playerToken) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        for (Player p : room.players()) {
            if (p.getId().equals(playerId) && p.getToken().equals(playerToken)) {
                return false;
            }
        }
        return true;
    }

}
