package xyz.korsak.pcoapi.room;

import org.springframework.stereotype.Service;
import xyz.korsak.pcoapi.BaseService;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
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
        Room room = roomRepository.findById(id);
        if (room.getToken().equals(roomToken)) {
            roomRepository.delete(id);
        } else {
            throw new UnauthorizedAccessException();
        }
    }

    public Player addPlayerToRoom(String roomId, Player player, String roomToken) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new NotFoundException("Room not found with ID: " + roomId);
        }
        if (!room.getToken().equals(roomToken)) {
            throw new UnauthorizedAccessException();
        }
        room.getPlayers().add(player);
        roomRepository.create(room);
        return player;
    }

    public GetPlayersResponse getPlayersInRoom(String roomId) {
        Room room = roomRepository.findById(roomId);
        return new GetPlayersResponse(room.getPlayers());
    }

    public Player getPlayerInRoom(String roomId, String playerId, String playerToken) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);
        
        if (room != null) {
            List<Player> players = room.getPlayers();
            
            Optional<Player> player = players.stream()
                    .filter(p -> p.getToken().equals(playerToken) && p.getId().equals(playerId))
                    .findFirst();
            
            if (player.isPresent()) {
                return player.get();
            } else {
                throw new UnauthorizedAccessException();
            }
        } else {
            throw new UnauthorizedAccessException();
        }
    }

    public void deletePlayerInRoom(String roomId, String playerId, String token) throws UnauthorizedAccessException {
        Room room = roomRepository.findById(roomId);

        if (room != null) {
            List<Player> players = room.getPlayers();

            Optional<Player> player = players.stream()
                    .filter(p -> (p.getToken().equals(token) || room.getToken().equals(token)) && p.getId().equals(playerId))
                    .findFirst();

            if (player.isPresent()) {
                players.remove(player.get());
                room.setPlayers(players);
                roomRepository.create(room);
            } else {
                throw new UnauthorizedAccessException();
            }
        } else {
            throw new UnauthorizedAccessException();
        }

    }

    public void updateRoom(Room room) {
        roomRepository.create(room);
    }
}
