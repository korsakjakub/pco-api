package xyz.korsak.pcoapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import xyz.korsak.pcoapi.game.GameStage;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.room.Room;

import java.io.File;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameAllInIntegrationTest {

    private final RedisTemplate<String, Room> redisTemplate;

    private Room room;

    @Autowired
    public GameAllInIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() throws IOException {
        String key = "room:rid-test";
        File json = new File("src/test/resources/GameAllInIntegrationTest.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            room = objectMapper.readValue(json, Room.class);
            redisTemplate.opsForValue().set(key, room);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Room object", e);
        }
    }

    @Test
    public void testAllIn() throws Exception {
    }

}
