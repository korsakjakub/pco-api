package xyz.korsak.pcoapi.room;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import xyz.korsak.pcoapi.exceptions.RedisUnavailableException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;

@Repository
public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_KEY_PREFIX = "room:";
    private final RedisTemplate<String, Room> redisTemplate;

    public RedisRoomRepository(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void create(Room room) {
      try {
        redisTemplate.opsForValue().set(ROOM_KEY_PREFIX + room.getId(), room);
        redisTemplate.opsForValue().set(ROOM_KEY_PREFIX + room.getToken(), room);
      } catch (RedisConnectionFailureException | RedisSystemException ex) {
        throw new RedisUnavailableException("Redis is not available");
    }
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
