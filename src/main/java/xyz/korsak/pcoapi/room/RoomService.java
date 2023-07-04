package xyz.korsak.pcoapi.room;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import xyz.korsak.pcoapi.exceptions.RoomNotFoundException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
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
            throw new UnauthorizedAccessException("Unauthorized access");
        }
    }

    public Player addPlayerToRoom(String roomId, String name) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new RoomNotFoundException("Room not found with ID: " + roomId);
        }
        String token = generateRandomString();
        String id = generateRandomString();
        Long balance = 0L;
        Player player = new Player(id, name, balance, token);
        System.out.println(player);
        room.getPlayers().add(player);
        System.out.println(room);
        roomRepository.create(room);
        return player;
    }

    public RoomGetPlayersInRoomResponse getPlayersInRoom(String roomId) {
        Room room = roomRepository.findById(roomId);
        return new RoomGetPlayersInRoomResponse(room.getPlayers());
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
                throw new UnauthorizedAccessException("Unauthorized access");
            }
        } else {
            throw new UnauthorizedAccessException("Unauthorized access");
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
                throw new UnauthorizedAccessException("Unauthorized access");
            }
        } else {
            throw new UnauthorizedAccessException("Unauthorized access");
        }

    }

    private String generateRandomString() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
