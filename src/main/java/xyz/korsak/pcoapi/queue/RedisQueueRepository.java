package xyz.korsak.pcoapi.queue;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import xyz.korsak.pcoapi.exceptions.NotFoundException;
import xyz.korsak.pcoapi.exceptions.RedisUnavailableException;
import xyz.korsak.pcoapi.player.Player;

@Repository
public class RedisQueueRepository implements QueueRepository {
    private static final String KEY_PREFIX = "queue:";
    private final RedisTemplate<String, Queue> redisTemplate;

    public RedisQueueRepository(RedisTemplate<String, Queue> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void create(Queue queue) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + queue.getId(), queue);
        } catch (RedisConnectionFailureException | RedisSystemException ex) {
            throw new RedisUnavailableException("Redis is not available");
        }
    }

    @Override
    public Queue findById(String id) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + id);
    }

    @Override
    public void delete(String id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }

    @Override
    public Player removePlayer(String queueId, String playerId) {
        Queue queue = redisTemplate.opsForValue().get(KEY_PREFIX + queueId);
        if (queue == null) {
            throw new NotFoundException("No queue with ID: " + queueId);
        }
        Player removedPlayer = queue.removePlayer(playerId);
        create(queue);
        return removedPlayer;
    }
}
