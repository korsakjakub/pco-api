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

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameTwoPlayersAllInIntegrationTest {
    private final RedisTemplate<String, Room> redisTemplate;
    @Autowired
    private MockMvc mockMvc;
    private Room room;

    @Autowired
    public GameTwoPlayersAllInIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
        String key = "room:rid-test";
        File json = new File("src/test/resources/GameTwoPlayersAllInIntegrationTest.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            room = objectMapper.readValue(json, Room.class);
            redisTemplate.opsForValue().set(key, room);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Room object", e);
        }
    }

    @Test
    public void testCallRaiseAllIn() throws Exception {
        Player p1 = room.players().get(0);
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 980));

        room = Utils.getRoom(mockMvc, roomId);
        Player p2 = room.players().get(1);

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));
    }
}
