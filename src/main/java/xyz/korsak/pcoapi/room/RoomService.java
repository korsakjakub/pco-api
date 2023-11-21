package xyz.korsak.pcoapi.room;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.player.PlayerBuilder;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RoomService extends BaseService {
    private final RoomRepository roomRepository;
    private final Authorization auth;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private void notifySubscribers(String roomId) {
        GetPlayersResponse r = getPlayersInRoom(roomId);
        emitters.forEach(emitter -> {
            try {
                emitter.send(r);
            } catch(IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        });
    }

    public SseEmitter streamPlayersInRoom(String roomId) {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onCompletion(() -> emitters.remove(emitter));

        notifySubscribers(roomId);
        return emitter;
    }

    public RoomService(Authorization authorization, RoomRepository roomRepository) {
        this.auth = authorization;
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String name) {
        String id = generateRandomString("rid");
        String token = generateRandomString("rtk");
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
        String token = generateRandomString("ptk");
        String id = generateRandomString("pid");
        Player player = new PlayerBuilder(id, playerName, token).build();
        room.getPlayers().add(player);
        roomRepository.create(room);
        return player;
    }

    public GetPlayersResponse getPlayersInRoom(String roomId) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        return new GetPlayersResponse(room.getPlayers());
    }

    public Player getPlayerInRoom(String roomId, String playerId, String playerToken)
            throws UnauthorizedAccessException {
        return auth.getPlayerWithAuthorization(roomId, playerId, playerToken);
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
