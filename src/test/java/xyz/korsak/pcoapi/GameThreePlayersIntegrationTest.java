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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameThreePlayersIntegrationTest {

    private final RedisTemplate<String, Room> redisTemplate;
    @Autowired
    private MockMvc mockMvc;
    private Room room;

    @Autowired
    public GameThreePlayersIntegrationTest(RedisTemplate<String, Room> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @BeforeEach
    public void setup() {
        String key = "room:rid-test";
        File json = new File("src/test/resources/GameThreePlayersIntegrationTest.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            room = objectMapper.readValue(json, Room.class);
            redisTemplate.opsForValue().set(key, room);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to Room object", e);
        }
    }

    @Test
    public void testBetFold() throws Exception {
        Player p2 = room.players().get(1);
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p3 = room.players().get(2);

        Assertions.assertEquals("[Fold, Call, Raise]", p3.getActions().toString());
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p3.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.players().get(0);
        p2 = room.players().get(1);
        p3 = room.players().get(2);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals(900, p2.getChips());
        Assertions.assertEquals(1000, p3.getChips());
        Assertions.assertEquals(1000, p1.getChips());
        Assertions.assertEquals(100, room.game().stakedChips());
        Assertions.assertEquals(100, room.game().currentBetSize());
    }

    @Test
    public void testBetRaiseCall() throws Exception {
        Player p2 = room.players().get(1);
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p3 = room.players().get(2);

        Assertions.assertEquals("[Fold, Call, Raise]", p3.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p3.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.players().get(0);

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.players().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().get(0);
        p2 = room.players().get(1);
        p3 = room.players().get(2);
        Assertions.assertEquals(GameStage.FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p3.getActions().toString());
        Assertions.assertEquals(600, room.game().stakedChips());
        Assertions.assertEquals(0, room.game().currentBetSize());
        Assertions.assertEquals(0, p1.getStakedChips());
        Assertions.assertEquals(0, p2.getStakedChips());
        Assertions.assertEquals(0, p3.getStakedChips());
    }

    @Test
    public void testFullRoundWithFold() throws Exception {
        Player p2 = room.players().get(1);
        String roomId = room.id();

        Assertions.assertEquals(GameStage.PRE_FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        Player p3 = room.players().get(2);

        Assertions.assertEquals("[Fold, Call, Raise]", p3.getActions().toString());
        Assertions.assertEquals("Raised", Utils.raise(mockMvc, roomId, p3.getToken(), 200));

        room = Utils.getRoom(mockMvc, roomId);
        Player p1 = room.players().getFirst();

        Assertions.assertEquals("[Fold, Call, Raise]", p1.getActions().toString());
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p1.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.players().get(1);

        Assertions.assertEquals("[Fold, Call, Raise]", p2.getActions().toString());
        Assertions.assertEquals("Called", Utils.call(mockMvc, roomId, p2.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p2 = room.players().get(1);
        p3 = room.players().get(2);

        Assertions.assertEquals(GameStage.FLOP, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());

        Utils.bet(mockMvc, roomId, p3.getToken(), 100, status().isUnauthorized());
        Utils.bet(mockMvc, roomId, p2.getToken(), -100, status().isIAmATeapot());
        Assertions.assertEquals("Bet", Utils.bet(mockMvc, roomId, p2.getToken(), 100));

        room = Utils.getRoom(mockMvc, roomId);
        p3 = room.players().get(2);
        Assertions.assertEquals("Folded", Utils.fold(mockMvc, roomId, p3.getToken()));

        room = Utils.getRoom(mockMvc, roomId);
        p1 = room.players().getFirst();
        p2 = room.players().get(1);
        p3 = room.players().get(2);
        Assertions.assertEquals(GameStage.SMALL_BLIND, room.game().stage());
        Assertions.assertEquals("[Fold, Check, Bet]", p1.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p2.getActions().toString());
        Assertions.assertEquals("[Fold, Check, Bet]", p3.getActions().toString());
        Assertions.assertEquals(1000, p1.getChips());
        Assertions.assertEquals(1200, p2.getChips());
        Assertions.assertEquals(800, p3.getChips());
    }
}
