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
import xyz.korsak.pcoapi.responses.IdResponse;
import xyz.korsak.pcoapi.room.Room;

import java.io.File;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameAllInIntegrationTest {

    private final RedisTemplate<String, Room> redisTemplate;
    @Autowired
    private MockMvc mockMvc;
    private Room room;

    @Autowired
    public GameAllInIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
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
        Player p1 = room.players().getFirst();
        String roomId = room.id();

        Assertions.assertEquals(GameStage.RIVER, room.game().stage());
        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        Player p2 = room.players().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p2.getToken(), 490));

        room = Utils.getRoom(mockMvc, roomId);
        Player p3 = room.players().get(2);

        Assertions.assertEquals("[Fold, Call, Raise]", p3.getActions().toString());
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p3.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().getFirst();

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().getFirst();

        Assertions.assertEquals(GameStage.SHOWDOWN, room.game().stage());
        Assertions.assertEquals(new IdResponse(p1.getId()), Utils.decideWinner(mockMvc, roomId, p1.getId(), room.token()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().getFirst();
        p2 = room.players().get(1);
        p3 = room.players().get(2);

        Assertions.assertEquals(GameStage.SMALL_BLIND, room.game().stage());
        Assertions.assertEquals(1020, p1.getChips());
        Assertions.assertEquals(500, p2.getChips());
        Assertions.assertEquals(980, p3.getChips());
    }

    @Test
    public void testAllInWinnerIsShortBet() throws Exception {
        Player p1 = room.players().getFirst();
        String roomId = room.id();

        Assertions.assertEquals(GameStage.RIVER, room.game().stage());
        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p1.getToken(), 500));

        room = Utils.getRoom(mockMvc, roomId);
        Player p2 = room.players().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p2.getToken(), 690));

        room = Utils.getRoom(mockMvc, roomId);
        Player p3 = room.players().get(2);

        Assertions.assertEquals("[Fold, Call, Raise]", p3.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p3.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().getFirst();

        Assertions.assertEquals(GameStage.SHOWDOWN, room.game().stage());
        Assertions.assertEquals(new IdResponse(p1.getId()), Utils.decideWinner(mockMvc, roomId, p1.getId(), room.token()));
        Assertions.assertEquals(new IdResponse(p2.getId()), Utils.decideWinner(mockMvc, roomId, p2.getId(), room.token()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().getFirst();
        p2 = room.players().get(1);
        p3 = room.players().get(2);

        Assertions.assertEquals(GameStage.SMALL_BLIND, room.game().stage());
        Assertions.assertEquals(1500, p1.getChips());
        Assertions.assertEquals(700, p2.getChips());
        Assertions.assertEquals(300, p3.getChips());
    }
}