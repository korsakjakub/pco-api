package xyz.korsak.pcoapi.room;

import org.springframework.stereotype.Service;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService extends BaseService {
    private final RoomRepository roomRepository;
    private final Authorization auth;

    public RoomService(Authorization authorization, RoomRepository roomRepository) {
        this.auth = authorization;
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
        if (auth.authorizeRoomOwner(id, roomToken)) {
            roomRepository.delete(id);
        } else {
            throw new UnauthorizedAccessException();
        }
    }

    public Player addPlayerToRoom(String roomId, Player player, String roomToken) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);
        room.getPlayers().add(player);
        roomRepository.create(room);
        return player;
    }

    public Player createPlayerInRoom(String roomId, String roomToken, String playerName) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);
        String token = generateRandomString();
        String id = generateRandomString();
        Player player = new Player(id, playerName, token);
        room.getPlayers().add(player);
        roomRepository.create(room);
        return player;
    }

    public GetPlayersResponse getPlayersInRoom(String roomId) {
        Room room = roomRepository.findById(roomId);
        return new GetPlayersResponse(room.getPlayers());
    }

    public Player getPlayerInRoom(String roomId, String playerId, String playerToken) throws UnauthorizedAccessException {
        Player player = auth.getPlayerWithAuthorization(roomId, playerId, playerToken);
        return player;
    }

    public void deletePlayerInRoom(String roomId, String playerId, String token) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        Player player = auth.getPlayerWithAuthorization(roomId, playerId, token);
        List<Player> players = room.getPlayers();
        players.remove(player);
        roomRepository.create(room);
    }

    public void updateRoom(Room room) {
        roomRepository.create(room);
    }
}
