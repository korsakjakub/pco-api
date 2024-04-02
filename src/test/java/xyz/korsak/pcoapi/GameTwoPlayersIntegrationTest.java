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
import xyz.korsak.pcoapi.game.GameState;
import xyz.korsak.pcoapi.player.Player;
import xyz.korsak.pcoapi.room.Room;

import java.io.File;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameTwoPlayersIntegrationTest {

    private final RedisTemplate<String, Room> redisTemplate;
    @Autowired
    private MockMvc mockMvc;
    private Room room;

    @Autowired
    public GameTwoPlayersIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
        String key = "room:rid-test";
        File json = new File("src/test/resources/GameTwoPlayersIntegrationTest.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            room = objectMapper.readValue(json, Room.class);
            redisTemplate.opsForValue().set(key, room);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Room object", e);
        }
    }

    @Test
    public void testCallFoldNewRoundCall() throws Exception {
        String roomId = room.id();
        String roomToken = room.token();

        Player p1 = room.players().getFirst();

        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().getFirst();
        Player p2 = room.players().get(1);

        Assertions.assertEquals(GameState.WAITING, room.game().state());
        Assertions.assertEquals(GameStage.SMALL_BLIND, room.game().stage());
        Assertions.assertEquals(1020, p2.getChips());
        Assertions.assertEquals(980, p1.getChips());
        Assertions.assertEquals("Game started successfully", Utils.startGame(mockMvc, roomId, roomToken));

        room = Utils.getRoom(mockMvc, roomId);
        Assertions.assertEquals(GameState.IN_PROGRESS, room.game().state());
        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());

        p1 = room.players().getFirst();
        p2 = room.players().get(1);

        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
    }
}
