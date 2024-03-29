package xyz.korsak.pcoapi.room;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.authorization.Authorization;
import xyz.korsak.pcoapi.exceptions.GameException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.game.GameState;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.responses.GetPlayersResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class RoomService extends BaseService {
    private final RoomRepository roomRepository;
    private final Authorization auth;

    public RoomService(Authorization authorization, RoomRepository roomRepository) {
        this.auth = authorization;
        this.roomRepository = roomRepository;
    }

    public void pushData(String roomId) {
        notifySubscribers(getPlayersInRoom(roomId), roomId);
    }

    public SseEmitter streamPlayersInRoom(String roomId) {
        SseEmitter emitter = newEmitter(roomId);
        pushData(roomId);
        return emitter;
    }

    public Room createRoom() {
        String id = generateRandomString("rid");
        String token = generateRandomString("rtk");
        Room room = new Room.RoomBuilder(id, new ArrayList<>(), token).build();
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
        if (auth.authorizeOwner(id, roomToken)) {
            roomRepository.delete(id);
        } else {
            throw new UnauthorizedAccessException();
        }
    }

    public void addPlayerToRoom(String roomId, Player player, String roomToken) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);
        room.players().add(player);
        roomRepository.create(room);
    }

    public Player createPlayerInRoom(String roomId, String roomToken, String playerName) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);
        String token = generateRandomString("ptk");
        String id = generateRandomString("pid");
        Player player = new Player(id, playerName, token);
        room.players().add(player);
        roomRepository.create(room);
        return player;
    }

    public GetPlayersResponse getPlayersInRoom(String roomId) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        return new GetPlayersResponse(room.players());
    }

    public Player getPlayerInRoom(String roomId, String playerId, String playerToken)
            throws UnauthorizedAccessException {
        return auth.getPlayerWithAuthorization(roomId, playerId, playerToken);
    }

    public void updatePlayerChips(String roomId, String playerId, String roomToken, int chips) {
        Room room = auth.getRoomByIdWithOwnerAuthorization(roomId, roomToken);

        room.players().forEach(player -> {
            if (Objects.equals(player.getId(), playerId)) {
                player.setChips(chips);
            }
        });

        roomRepository.create(room);
    }

    public void deletePlayerInRoom(String roomId, String playerId, String token) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new UnauthorizedAccessException();
        }
        if (room.game() == null) {
            throw new UnauthorizedAccessException();
        }
        if (room.game().state().equals(GameState.IN_PROGRESS)) {
            throw new GameException("Cannot remove player from room while game is in progress");
        }

        final Player player = auth.getPlayerWithAuthorization(roomId, playerId, token);
        final List<Player> newPlayers = room.players().stream().filter(p -> !Objects.equals(p.getId(), player.getId())).toList();

        final Room updatedRoom = room.toBuilder().players(newPlayers).build();
        roomRepository.create(updatedRoom);
    }

    public void updateRoom(Room room) {
        roomRepository.create(room);
    }
}
