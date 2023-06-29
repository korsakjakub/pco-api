package xyz.korsak.pcoapi.room;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_KEY_PREFIX = "room:";
    private final RedisTemplate<String, Room> redisTemplate;

    public RedisRoomRepository(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void create(Room room) {
        redisTemplate.opsForValue().set(ROOM_KEY_PREFIX + room.getId(), room);
        redisTemplate.opsForValue().set(ROOM_KEY_PREFIX + room.getToken(), room);
    }

    @Override
    public Room findById(String id) {
        return redisTemplate.opsForValue().get(ROOM_KEY_PREFIX + id);
    }

    @Override
    public Room findByToken(String token) {
        return redisTemplate.opsForValue().get(ROOM_KEY_PREFIX + token);
    }

    @Override
    public void delete(String id) {
        redisTemplate.delete(ROOM_KEY_PREFIX + id);
    }
}
