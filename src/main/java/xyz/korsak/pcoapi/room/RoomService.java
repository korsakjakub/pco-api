package xyz.korsak.pcoapi.room;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import xyz.korsak.pcoapi.exceptions.RoomAlreadyExistsException;
import xyz.korsak.pcoapi.exceptions.RoomNotFoundException;
import xyz.korsak.pcoapi.exceptions.UnauthorizedAccessException;
import xyz.korsak.pcoapi.player.Player;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String name) {
        if (!roomRepository.existsWithName(name)) {
            throw new RoomAlreadyExistsException("A room with the name '" + name + "' already exists.");
        }

        String id = generateToken();
        String token = generateToken();
        Room room = new Room(id, name, token, new ArrayList<>());
        roomRepository.create(room);
        return room;
    }

    public Room getRoomById(String id) {
        return roomRepository.findById(id);
    }

    public Room getRoomByToken(String token) {
        return roomRepository.findByToken(token);
    }

    public void deleteRoom(String id) {
        roomRepository.delete(id);
    }

    public String addPlayerToRoom(String roomId, Player player) {
        Room room = roomRepository.findById(roomId);
        if (room != null) {
            String token = generateToken();
            player.setToken(token);
            room.getPlayers().add(player);
            roomRepository.create(room);
            return token;
        } else {
            throw new RoomNotFoundException("Room not found with ID: " + roomId);
        }
    }

    public List<Player> getPlayersInRoom(String roomId, String token) {

        Room room = roomRepository.findById(roomId);
        if (room != null && room.getToken().equals(token)) {
            return room.getPlayers();
        } else {
            throw new UnauthorizedAccessException("Unauthorized access");
        }
    }
    private String generateToken() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
