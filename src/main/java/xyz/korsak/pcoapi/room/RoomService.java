package xyz.korsak.pcoapi.room;

import org.springframework.stereotype.Service;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService extends BaseService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String name) {
        String id = generateRandomString();
        String token = generateRandomString();
        Room room = new Room(id, name, new ArrayList<>(), token);
        roomRepository.create(room);
        return room;
    }

    public Room getRoomById(String id) {
        return roomRepository.findById(id);
    }

    public Room getRoomByToken(String token) {
        return roomRepository.findByToken(token);
    }

    public void deleteRoom(String id, String roomToken) {
        if (authorizeRoomOwner(id, roomToken)) {
            roomRepository.delete(id);
        } else {
            throw new UnauthorizedAccessException();
        }
    }

    public Player addPlayerToRoom(String roomId, Player player, String roomToken) {
        if (!authorizeRoomOwner(roomId, roomToken)) {
            throw new UnauthorizedAccessException();
        }
        Room room = roomRepository.findById(roomId);
        room.getPlayers().add(player);
        roomRepository.create(room);
        return player;
    }

    public GetPlayersResponse getPlayersInRoom(String roomId) {
        Room room = roomRepository.findById(roomId);
        return new GetPlayersResponse(room.getPlayers());
    }

    public Player getPlayerInRoom(String roomId, String playerId, String playerToken) throws UnauthorizedAccessException {
        return getPlayerWithAuthorization(roomId, playerId, playerToken);
    }

    public void deletePlayerInRoom(String roomId, String playerId, String token) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        Player player = getPlayerWithAuthorization(roomId, playerId, token);
        List<Player> players = room.getPlayers();
        players.remove(player);
        roomRepository.create(room);

    }

    public void updateRoom(Room room) {
        roomRepository.create(room);
    }

    protected boolean authorizeRoomOwner(String roomId, String roomToken) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);
        return room != null && room.getToken().equals(roomToken);
    }

    /*
    token can be either roomToken or playerToken. The point is that both room owner and player should be able to access
    player related info
     */
    protected Player getPlayerWithAuthorization(String roomId, String playerId, String token) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        List<Player> players = room.getPlayers();

        Optional<Player> player = players.stream()
                .filter(p -> (p.getToken().equals(token) || room.getToken().equals(token)) && p.getId().equals(playerId))
                .findFirst();

        if (player.isEmpty()) {
            throw new UnauthorizedAccessException();
        }
        return player.get();
    }
}
