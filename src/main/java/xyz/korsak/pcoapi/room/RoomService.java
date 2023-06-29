package xyz.korsak.pcoapi.room;

import org.springframework.stereotype.Service;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.korsak.pcoapi.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String name) {
        String id = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
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
            // Handle room not found error
            return null;
        }
    }

    public List<Player> getPlayersInRoom(String roomId, String token) {
        Room room = roomRepository.findById(roomId);
        if (room != null && room.getPlayers().stream().anyMatch(player -> player.getToken().equals(token))) {
            return room.getPlayers();
        } else {
            // Handle room not found or unauthorized access error
            return Collections.emptyList();
        }
    }
    private String generateToken() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
